package cn.drajun.mybatis.binding;

import cn.drajun.mybatis.session.SqlSession;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 映射代理工厂，为接口中的方法找到匹配的sql语句，然后做具体数据库操作
 */
public class MapperProxyFactory<T> {

    // 所代理的接口
    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    // 新建具体的映射器代理类
    public T newInstance(SqlSession sqlSession){
        MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }


}
