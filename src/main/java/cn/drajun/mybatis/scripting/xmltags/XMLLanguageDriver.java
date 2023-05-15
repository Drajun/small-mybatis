package cn.drajun.mybatis.scripting.xmltags;

import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.scripting.LanguageDriver;
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

}
