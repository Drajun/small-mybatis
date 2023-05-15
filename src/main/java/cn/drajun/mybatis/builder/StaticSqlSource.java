package cn.drajun.mybatis.builder;

import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.ParameterMapping;
import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码
 */
public class StaticSqlSource implements SqlSource {

    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql){
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings){
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
