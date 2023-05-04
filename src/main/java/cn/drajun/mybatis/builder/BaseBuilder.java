package cn.drajun.mybatis.builder;

import cn.drajun.mybatis.session.Configuration;

/**
 * 构建类的基类
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration){
        this.configuration = configuration;
    }

    public Configuration getConfiguration(){
        return configuration;
    }

}
