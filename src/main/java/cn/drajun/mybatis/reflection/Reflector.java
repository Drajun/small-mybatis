package cn.drajun.mybatis.reflection;

import cn.drajun.mybatis.reflection.invoker.Invoker;
import cn.drajun.mybatis.reflection.invoker.MethodInvoker;
import cn.drajun.mybatis.reflection.invoker.SetFieldInvoker;
import cn.drajun.mybatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射器，属性get/set的映射器
 */
public class Reflector {

    private static boolean classCacheEnabled = true;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    // 线程安全的缓存
    private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();

    private Class<?> type;

    // get属性列表
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
    // set属性列表
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;
    // set方法列表
    private Map<String, Invoker> setMethods = new HashMap<>();
    // get方法列表
    private Map<String, Invoker> getMethods = new HashMap<>();
    // set类型列表
    private Map<String, Class<?>> setTypes = new HashMap<>();
    // get类型列表
    private Map<String, Class<?>> getTypes = new HashMap<>();
    // 构造函数
    private Constructor<?> defaultConstructor;

    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz){
        this.type = clazz;
        // 加入构造函数
        addDefaultConstructor(clazz);
        // 加入getter
        addGetMethods(clazz);
        // 加入setter
        addSetMethods(clazz);
        // 加入字段
        addFields(clazz);
        readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
        writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
        for(String propName : readablePropertyNames){
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for(String propName : writeablePropertyNames){
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }

    /**
     * 加入构造函数
     * @param clazz
     */
    private void addDefaultConstructor(Class<?> clazz){
        Constructor<?>[] consts = clazz.getDeclaredConstructors();
        for(Constructor<?> constructor : consts){
            if(constructor.getParameterTypes().length == 0){
                if(canAccessPrivateMethods()){
                    try{
                        constructor.setAccessible(true);
                    }
                    catch (Exception ignore){

                    }
                }
                if(constructor.isAccessible()){
                    this.defaultConstructor = constructor;
                }
            }
        }
    }

    /**
     * 加入get方法
     * @param clazz
     */
    private void addGetMethods(Class<?> clazz){
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for(Method method : methods){
            String name = method.getName();
            if(name.startsWith("get") && name.length() > 3){
                if(method.getParameterTypes().length == 0){
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
            else if(name.startsWith("is") && name.length() > 2){
                if(method.getParameterTypes().length == 0){
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
        }
        resolveGetterConflicts(conflictingGetters);
    }

    /**
     * 加入set方法
     * @param clazz
     */
    private void addSetMethods(Class<?> clazz){
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for(Method method : methods){
            String name = method.getName();
            if(name.startsWith("set") && name.length() > 3){
                if(method.getParameterTypes().length == 1){
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingSetters, name, method);
                }
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    /**
     * 通过数量、参数类型解决set方法冲突
     * @param conflictingSetters
     */
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters){
        for(String propName : conflictingSetters.keySet()){
            List<Method> setters = conflictingSetters.get(propName);
            Method firstMethod = setters.get(0);
            if(setters.size() == 1){
                addSetMethod(propName, firstMethod);
            }
            else {
                Class<?> expectedType = getTypes.get(propName);
                if(expectedType == null){
                    throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                            + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                            "specification and can cause unpredicatble results.");
                }
                else{
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    while (methods.hasNext()){
                        Method method = methods.next();
                        if(method.getParameterTypes().length == 1 && expectedType.equals(method.getParameterTypes()[0])){
                            setter = method;
                            break;
                        }
                    }
                    if(setter == null){
                        throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                                "specification and can cause unpredicatble results.");
                    }
                    addSetMethod(propName, setter);
                }
            }
        }
    }

    private void addSetMethod(String name, Method method){
        if(isValidPropertyName(name)){
            setMethods.put(name, new MethodInvoker(method));
            setTypes.put(name, method.getParameterTypes()[0]);
        }
    }

    private void addFields(Class<?> clazz){
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            if(canAccessPrivateMethods()){
                try{
                    field.setAccessible(true);
                }
                catch (Exception ignore){

                }
            }
            if(field.isAccessible()){
                if(!setMethods.containsKey(field.getName())){
                    // 处理 static final字段
                    int modifiers = field.getModifiers();
                    if(!Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)){
                        addSetField(field);
                    }
                }
                if(!getMethods.containsKey(field.getName())){
                    addGetField(field);
                }
            }
        }
        if(clazz.getSuperclass() != null){
            addFields(clazz.getSuperclass());
        }
    }

    private void addSetField(Field field){
        if(isValidPropertyName(field.getName())){
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }
    }

    private void addGetField(Field field){
        if(isValidPropertyName(field.getName())){
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }
    }

    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters){
        for(String propName : conflictingGetters.keySet()){
            List<Method> getters = conflictingGetters.get(propName);
            Iterator<Method> iterator = getters.iterator();
            Method firstMethod = iterator.next();
            if(getters.size() == 1){
                addGetMethod(propName, firstMethod);
            }
            else{
                Method getter = firstMethod;
                Class<?> getterType = firstMethod.getReturnType();
                while (iterator.hasNext()){
                    Method method = iterator.next();
                    Class<?> methodType = method.getReturnType();
                    if(methodType.equals(getterType)){
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    }
                    else if(methodType.isAssignableFrom(getterType)){

                    }
                    else if(getterType.isAssignableFrom(methodType)){
                        getter = method;
                        getterType = methodType;
                    }
                    else{
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    }
                }
                addGetMethod(propName, getter);
            }
        }
    }

    private void addGetMethod(String name, Method method){
        if(isValidPropertyName(name)){
            getMethods.put(name, new MethodInvoker(method));
            getTypes.put(name, method.getReturnType());
        }
    }

    private boolean isValidPropertyName(String name){
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method){
        List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(method);
    }

    /**
     * 获取类方法，从子类溯着父类一路往上，并且去重
     * @param cls
     * @return
     */
    private Method[] getClassMethods(Class<?> cls){
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = cls;
        while (currentClass != null){
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
            Class<?>[] interfaces = currentClass.getInterfaces();
            for(Class<?> anInterface : interfaces){
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[methods.size()]);
    }

    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods){
        for (Method currentMethod : methods){
            if(!currentMethod.isBridge()){
                // 取得签名
                String signature = getSignature(currentMethod);
                if(!uniqueMethods.containsKey(signature)){
                    if(canAccessPrivateMethods()){
                        try{
                            currentMethod.setAccessible(true);
                        }
                        catch (Exception ignore){

                        }
                    }
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    /**
     * 方法签名：返回类型#方法名称:参数类型1,参数类型2...
     * @param method
     * @return
     */
    private String getSignature(Method method){
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if(returnType != null){
            sb.append(returnType.getName()).append('#');
        }
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for(int i=0;i< parameters.length;i++){
            if(i == 0){
                sb.append(':');
            }
            else{
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    private static boolean canAccessPrivateMethods(){
        try{
            SecurityManager securityManager = System.getSecurityManager();
            if(null != securityManager){
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        }
        catch (SecurityException e){
            return false;
        }
        return true;
    }

    public Class<?> getType(){
        return type;
    }

    public Constructor<?> getDefaultConstructor(){
        if(defaultConstructor != null){
            return defaultConstructor;
        }
        else{
            throw new RuntimeException("There is no default constructor for " + type);
        }
    }

    public boolean hasDefaultConstructor(){
        return defaultConstructor != null;
    }

    public Class<?> getSetterType(String propertyName){
        Class<?> clazz = setTypes.get(propertyName);
        if(clazz == null){
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    public Invoker getGetInvoker(String propertyName){
        Invoker method = getMethods.get(propertyName);
        if(method == null){
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    public Invoker getSetInvoker(String propertyName){
        Invoker method = setMethods.get(propertyName);
        if(method == null){
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    public Class<?> getGetterType(String propertyName){
        Class<?> clazz = getTypes.get(propertyName);
        if(clazz == null){
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    // 获取对象的可读属性
    public String[] getGetablePropertyNames(){
        return readablePropertyNames;
    }

    // 获取对象的可写属性
    public String[] getSetablePropertyNames(){
        return writeablePropertyNames;
    }

    public boolean hasSetter(String propertyName){
        return setMethods.keySet().contains(propertyName);
    }

    public boolean hasGetter(String propertyName) {
        return getMethods.keySet().contains(propertyName);
    }

    public String findPropertyName(String name){
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    public static Reflector forClass(Class<?> clazz){
        if(classCacheEnabled){
            Reflector cached = REFLECTOR_MAP.get(clazz);
            if(cached == null){
                cached = new Reflector(clazz);
                REFLECTOR_MAP.put(clazz, cached);
            }
            return cached;
        }
        else{
            return new Reflector(clazz);
        }
    }

    public static void setClassCacheEnabled(boolean classCacheEnabled){
        Reflector.classCacheEnabled = classCacheEnabled;
    }

    public static boolean isClassCacheEnabled(){
        return classCacheEnabled;
    }





}
