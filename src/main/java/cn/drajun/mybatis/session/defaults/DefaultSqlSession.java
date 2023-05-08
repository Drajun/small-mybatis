package cn.drajun.mybatis.session.defaults;

import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.executor.Executor;
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
    private Executor executor;

    public DefaultSqlSession(Configuration  configuration, Executor executor){
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statement);
        List<T> list = executor.query(ms, parameter, Executor.NO_RESULT_HANDLER, ms.getBoundSql());
        return list.get(0);
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
