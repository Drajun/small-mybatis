package cn.drajun.mybatis.scripting.defaults;

import cn.drajun.mybatis.builder.SqlSourceBuilder;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.scripting.xmltags.DynamicContext;
import cn.drajun.mybatis.scripting.xmltags.SqlNode;
import cn.drajun.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * 原始SQL源码，比 DynamicSqlSource 动态SQL处理快
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType){
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType){
        SqlSourceBuilder sqlParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlParser.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode){
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
}
