package cn.drajun.mybatis.scripting.xmltags;

import cn.drajun.mybatis.executor.parameter.ParameterHandler;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.scripting.LanguageDriver;
import cn.drajun.mybatis.scripting.defaults.DefaultParameterHandler;
import cn.drajun.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * XML语言驱动器
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

}
