package cn.drajun.mybatis.session.defaults;

import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.Environment;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration  configuration){
        this.configuration = configuration;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你的操作被代理了！" + statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        try{
            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            Environment environment = configuration.getEnvironment();

            Connection connection = environment.getDataSource().getConnection();

            // 获取SQL语句，设置参数，执行SQL
            BoundSql boundSql = mappedStatement.getBoundSql();
            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
            preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
            return objList.get(0);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将sql查询的结果集转为java对象
     * @param resultSet
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz){
        List<T> list = new ArrayList<>();
        try{
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while(resultSet.next()){
                T obj = (T) clazz.newInstance();
                for(int i=1; i <= columnCount; i++){
                    // clazz中的set方法
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);

                    // 设置clazz的值
                    Object value = resultSet.getObject(i);
                    Method method = null;
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

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
