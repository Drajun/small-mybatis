package cn.drajun.mybatis.executor.resultset;

import cn.drajun.mybatis.executor.Executor;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 默认的Map结果处理器
 */
public class DefaultResultSetHandler implements ResultSetHandler{

    private final BoundSql boundSql;
    private final MappedStatement mappedStatement;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql){
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
    }

    @Override
    public <E> List<E> handlerResultSets(Statement stmt) throws SQLException {
        ResultSet resultSet = stmt.getResultSet();
        return resultSet2Obj(resultSet, mappedStatement.getResultType());
    }

    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz){
        List<T> list = new ArrayList<>();
        try{
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while(resultSet.next()){
                T obj = (T) clazz.newInstance();
                for(int i=1;i<=columnCount;i++){
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if(value instanceof Timestamp){
                        method = clazz.getMethod(setMethod, Date.class);
                    }
                    else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                list.add(obj);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
}
