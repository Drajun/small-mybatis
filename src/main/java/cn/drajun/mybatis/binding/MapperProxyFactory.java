package cn.drajun.mybatis.binding;

import cn.drajun.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 映射代理工厂，为接口中的方法找到匹配的sql语句，然后做具体数据库操作
 */
public class MapperProxyFactory<T> {

    // 所代理的接口
    private final Class<T> mapperInterface;

    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public  Map<Method, MapperMethod> getMethodCache(){
        return methodCache;
    }

    // 新建具体的映射器代理类
    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession){
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }


}
