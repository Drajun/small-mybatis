package cn.drajun.mybatis.binding;

import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;
import cn.hutool.core.lang.ClassScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 映射器注册机
 */
public class MapperRegistry {

    private Configuration configuration;
    public MapperRegistry(Configuration config){
        this.configuration = config;
    }

    // 将已添加的映射器代理工厂加入到map中
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    // 根据接口的类型返回映射器代理工厂
    public <T> T getMapper(Class<T> type, SqlSession sqlSession){
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if(mapperProxyFactory == null){
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        try{
            return mapperProxyFactory.newInstance(sqlSession);
        }
        catch (Exception e){
            throw new RuntimeException("Error getting mapper instance. Cause "+ e, e);
        }
    }

    public <T> void addMapper(Class<T> type){
        if(type.isInterface()){
            if (hasMapper(type)) {
                // 不能重复添加
                throw new RuntimeException("Type "+ type + "is already known to the MapperRegistry.");
            }
            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));
        }
    }

    public <T> boolean hasMapper(Class<T> type){
        return knownMappers.containsKey(type);
    }

    // 通过包名扫描接口，并创建对应的映射器工厂
    public void addMappers(String packageName){
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for(Class<?> mapperClass : mapperSet){
            addMapper(mapperClass);
        }
    }
}
