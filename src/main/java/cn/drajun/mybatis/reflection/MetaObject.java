package cn.drajun.mybatis.reflection;

import cn.drajun.mybatis.reflection.factory.ObjectFactory;
import cn.drajun.mybatis.reflection.property.PropertyTokenizer;
import cn.drajun.mybatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 元对象
 */
public class MetaObject {
    // 元对象
    private Object originalObject;
    // 对象包装器
    private ObjectWrapper objectWrapper;
    // 对象工厂
    private ObjectFactory objectFactory;
    // 对象包装工厂
    private ObjectWrapperFactory objectWrapperFactory;

    private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory){
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;

        if(object instanceof ObjectWrapper){
            // 如果对象本身已经是ObjectWrapper型，则直接赋给objectWrapper
            this.objectWrapper = (ObjectWrapper) object;
        }
        else if(objectWrapperFactory.hasWrapperFor(object)){
            // 如果有包装器,调用ObjectWrapperFactory.getWrapperFor
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
        }
        else if(object instanceof Map){
            this.objectWrapper = new MapWrapper(this, (Map) object);
        }
        else if(object instanceof Collection){
            this.objectWrapper = new CollectionWrapper(this, (Collection) object);
        }
        else{
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }

    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory){
        if(object == null){
            return SystemMetaObject.NULL_META_OBJECT;
        }
        else{
            return new MetaObject(object, objectFactory, objectWrapperFactory);
        }
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    /* --------以下方法都是委派给 ObjectWrapper------ */
    // 查找属性
    public String findProperty(String propName, boolean useCamelCaseMapping){
        return objectWrapper.findProperty(propName, useCamelCaseMapping);
    }

    // 取得getter的名字列表
    public String[] getGetterNames(){
        return objectWrapper.getGetterNames();
    }

    // 取得setter的名字列表
    public String[] getSetterNames(){
        return objectWrapper.getSetterNames();
    }

    // 取得setter的类型列表
    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    // 取得getter的类型列表
    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    //是否有指定的setter
    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    // 是否有指定的getter
    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    // 是否是集合
    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    // 添加属性
    public void add(Object element) {
        objectWrapper.add(element);
    }

    // 添加属性
    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }

    // 获取值
    public Object getValue(String name){
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if(prop.hasNext()){
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT){
                return null;
            }
            else{
                // 递归调用
                return metaValue.getValue(prop.getChildren());
            }
        }
        else {
            return objectWrapper.get(prop);
        }
    }

    // 设置值
    public void setValue(String name, Object value){
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if(prop.hasNext()){
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            if(metaValue == SystemMetaObject.NULL_META_OBJECT){
                // 如果上层就是 null 了，还得看有没有儿子，没有那就结束
                if(value == null && prop.getChildren() != null){
                    return;
                }
                else{
                    metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
                }
            }
            metaValue.setValue(prop.getChildren(), value);
        }
        else{
            objectWrapper.set(prop, value);
        }
    }

    /**
     * 为属性生成元对象
     * @param name
     * @return
     */
    public MetaObject metaObjectForProperty(String name){
        Object value = getValue(name);
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory);
    }
}
