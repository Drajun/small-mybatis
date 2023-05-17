package cn.drajun.mybatis.executor.resultset;

import cn.drajun.mybatis.executor.Executor;
import cn.drajun.mybatis.executor.result.DefaultResultContext;
import cn.drajun.mybatis.executor.result.DefaultResultHandler;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.ResultMap;
import cn.drajun.mybatis.mapping.ResultMapping;
import cn.drajun.mybatis.reflection.MetaClass;
import cn.drajun.mybatis.reflection.MetaObject;
import cn.drajun.mybatis.reflection.factory.ObjectFactory;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.ResultHandler;
import cn.drajun.mybatis.session.RowBounds;
import cn.drajun.mybatis.type.TypeHandler;
import cn.drajun.mybatis.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 默认的Map结果处理器
 */
public class DefaultResultSetHandler implements ResultSetHandler{

    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final RowBounds rowBounds;
    private final ResultHandler resultHandler;
    private final BoundSql boundSql;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final ObjectFactory objectFactory;


    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql){
        this.configuration = mappedStatement.getConfiguration();
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
        this.resultHandler = resultHandler;
        this.objectFactory = configuration.getObjectFactory();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> handlerResultSets(Statement stmt) throws SQLException {
        final List<Object> multipleResults = new ArrayList<>();

        int resultSetCount = 0;
        ResultSetWrapper rsw = new ResultSetWrapper(stmt.getResultSet(), configuration);

        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        while (rsw != null && resultMaps.size() > resultSetCount){
            ResultMap resultMap = resultMaps.get(resultSetCount);
            handleResultSet(rsw, resultMap, multipleResults, null);
            rsw = getNextResultSet(stmt);
            resultSetCount++;
        }
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }

    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException{
        try{
            if(stmt.getConnection().getMetaData().supportsMultipleResultSets()){
                if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
                    ResultSet rs = stmt.getResultSet();
                    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
                }
            }
        }
        catch (Exception ignore){

        }
        return null;
    }

    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException{
        if(resultHandler == null){
            // 创建结果处理器
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            // 封装数据
            handleRowValuesForSimpleResultMap(rsw, resultMap, defaultResultHandler, rowBounds, null);
            // 保存结果
            multipleResults.add(defaultResultHandler.getResultList());
        }
    }

    /**
     * 封装数据
     * @param rsw
     * @param resultMap
     * @param resultHandler
     * @param rowBounds
     * @param parentMapping
     * @throws SQLException
     */
    private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException{
        DefaultResultContext resultContext = new DefaultResultContext();
        while(resultContext.getResultCount() < rowBounds.getLimit() && rsw.getResultSet().next()){
            Object rowValue = getRowValue(rsw, resultMap);
            callResultHandler(resultHandler, resultContext, rowValue);
        }
    }

    private void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue){
        resultContext.nextResultObject(rowValue);
        resultHandler.handleResult(resultContext);
    }

    // 获取一行的值
    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException{
        // 根据返回类型，实例化对象
        Object resultObject = createResultObject(rsw, resultMap, null);
        if(resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())){
            final MetaObject metaObject = configuration.newMetaObject(resultObject);
            applyAutomaticMappings(rsw, resultMap, metaObject, null);
        }
        return resultObject;
    }

    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException{
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        final List<Object> constructorArgs = new ArrayList<>();
        return createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
    }

    /**
     * 创建结果
     * @param rsw 结果集包装器
     * @param resultMap
     * @param constructorTypes
     * @param constructorArgs
     * @param columnPrefix
     * @return
     * @throws SQLException
     */
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException{
        final Class<?> resultType = resultMap.getType();
        final MetaClass metaType = MetaClass.forClass(resultType);
        if (resultType.isInterface() || metaType.hasDefaultConstructor()){
            return objectFactory.create(resultType);
        }
        throw new RuntimeException("Do not know how to create an instance of " + resultType);
    }

    private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        for(String columnName : unmappedColumnNames){
            String propertyName = columnName;
            if(columnPrefix != null && !columnPrefix.isEmpty()){
                if(columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)){
                    propertyName = columnName.substring(columnPrefix.length());
                }
                else{
                    continue;
                }
            }
            final String property = metaObject.findProperty(propertyName, false);
            if (property != null && metaObject.hasSetter(propertyName)) {
                final Class<?> propertyType = metaObject.getSetterType(propertyName);
                if(typeHandlerRegistry.hasTypeHandler(propertyType)){
                    final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
                    final Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                    if(value != null){
                        foundValues = true;
                    }
                    if(value != null || !propertyType.isPrimitive()){
                        // 通过反射工具类设置属性值
                        metaObject.setValue(propertyName, value);
                    }
                }
            }
        }
        return foundValues;
    }

}
