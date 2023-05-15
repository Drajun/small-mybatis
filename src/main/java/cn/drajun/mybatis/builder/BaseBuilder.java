package cn.drajun.mybatis.builder;

import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.type.TypeAliasRegistry;
import cn.drajun.mybatis.type.TypeHandlerRegistry;

/**
 * 构建类的基类
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration){
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration(){
        return configuration;
    }

    protected Class<?> resolveAlias(String alias){
        return typeAliasRegistry.resolveAlias(alias);
    }

}
