package cn.drajun.mybatis.scripting;

import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.session.Configuration;
import org.dom4j.Element;


/**
 * 脚本语言驱动
 */
public interface LanguageDriver {

    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

}
