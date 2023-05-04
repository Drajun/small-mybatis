package cn.drajun.mybatis.binding;


import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.SqlCommandType;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * 映射器方法
 */
public class MapperMethod {

    private final SqlCommand command;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
    }


    /**
     * 根据SQL语句的类型和名称（id）执行sql
     * @param sqlSession
     * @param args
     * @return
     */
    public Object execute(SqlSession sqlSession, Object[] args){
        Object result = null;
        switch (command.getType()){
            case INSERT:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            case SELECT:
                result = sqlSession.selectOne(command.getName(), args);
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }


    /**
     * SQL语句的名称（id）和类型
     */
    public static class SqlCommand{
        private final String name;
        private final SqlCommandType type;


        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = configuration.getMappedStatement(statementName);
            name = ms.getId();
            type = ms.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }
}
