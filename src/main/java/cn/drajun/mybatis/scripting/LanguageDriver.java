package cn.drajun.mybatis.scripting;

import cn.drajun.mybatis.executor.parameter.ParameterHandler;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.session.Configuration;
import org.dom4j.Element;


/**
 * 脚本语言驱动
 */
public interface LanguageDriver {

    /**
     * 创建SQL源码
     * @param configuration
     * @param script
     * @param parameterType
     * @return
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建SQL源码(annotation 注解方式)
     */
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

    /**
     * 创建参数处理器
     * @param mappedStatement 映射语句类，描述一条SQL语句
     * @param parameterObject 参数
     * @param boundSql
     * @return
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

}
