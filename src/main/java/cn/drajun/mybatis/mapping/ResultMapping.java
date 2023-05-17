package cn.drajun.mybatis.mapping;

import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.type.JdbcType;
import cn.drajun.mybatis.type.TypeHandler;

/**
 * 结果映射
 */
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;

    ResultMapping(){

    }

    public static class Builder{
        private ResultMapping resultMapping = new ResultMapping();
    }
}
