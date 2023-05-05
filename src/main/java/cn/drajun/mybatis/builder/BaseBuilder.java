package cn.drajun.mybatis.builder;

import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.type.TypeAliasRegistry;

/**
 * 构建类的基类
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration){
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration(){
        return configuration;
    }

}
