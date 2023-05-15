package cn.drajun.mybatis.binding;


import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.SqlCommandType;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 映射器方法
 */
public class MapperMethod {

    private final SqlCommand command;
    private final MethodSignature method;

    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.method = new MethodSignature(configuration, method);
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
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
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

    /**
     * 方法签名
     */
    public static class MethodSignature{
        private final SortedMap<Integer, String> params;

        public MethodSignature(Configuration configuration, Method method){
            this.params = Collections.unmodifiableSortedMap(getParams(method));
        }

        public Object convertArgsToSqlCommandParam(Object[] args){
            final int paramCount = params.size();
            if(args == null || paramCount == 0){
                return null;
            }
            else if(paramCount == 1){
                return args[params.keySet().iterator().next().intValue()];
            }
            else{
                // 否则，返回一个ParamMap，修改参数名，参数名就是其位置
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                for(Map.Entry<Integer, String> entry : params.entrySet()){
                    // 1.先加一个#{0},#{1},#{2}...参数
                    param.put(entry.getValue(), args[entry.getKey().intValue()]);
                    final String genericParamName = "param" + (i+1);
                    if(!param.containsKey(genericParamName)){
                        param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        private SortedMap<Integer, String> getParams(Method method){
            // 用一个TreeMap，这样就保证还是按参数的先后顺序
            final SortedMap<Integer, String> params = new TreeMap<>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for(int i=0;i<argTypes.length;i++){
                String paramName = String.valueOf(argTypes[i]);
                params.put(i, paramName);
            }
            return params;
        }
    }

    public static class ParamMap<V> extends HashMap<String, V>{

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }
    }
}
