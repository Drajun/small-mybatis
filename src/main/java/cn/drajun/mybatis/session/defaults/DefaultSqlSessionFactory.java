package cn.drajun.mybatis.session.defaults;

import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.session.SqlSession;
import cn.drajun.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final MapperRegistry mapperRegistry;

    public DefaultSqlSessionFactory(MapperRegistry mapperRegistry){
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(mapperRegistry);
    }
}
