package cn.drajun.mybatis.reflection;

import cn.drajun.mybatis.reflection.factory.DefaultObjectFactory;
import cn.drajun.mybatis.reflection.factory.ObjectFactory;
import cn.drajun.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.drajun.mybatis.reflection.wrapper.ObjectWrapperFactory;

public class SystemMetaObject {

    // 对象工厂：创建对象
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject(){

    }

    /**
     * 空对象
     */
    private static class NullObject{

    }

    public static MetaObject forObject(Object object){
        /**
         * 最底层利用Reflector拆解类，提取出其中的get、set、属性、构造器等信息
         *
         */
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }
}
