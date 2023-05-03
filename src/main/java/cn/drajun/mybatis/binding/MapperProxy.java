package cn.drajun.mybatis.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 映射器代理类
 * @param <T>
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final Long serialVersionUID = 6424540398559729838L;

    // key表示想执行的方法，value表示参数
    private Map<String, String> sqlSession;

    // 所代理的接口
    private final Class<T> mapperInterface;

    public MapperProxy(Map<String, String> sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class.equals(method.getDeclaringClass())){
            return method.invoke(this, args);
        }
        else{
            return "你被代理了！" + sqlSession.get(mapperInterface.getName() + "." + method.getName());
        }
    }
}
