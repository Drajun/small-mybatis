package cn.drajun.mybatis.builder.annotation;

import cn.drajun.mybatis.annotations.Delete;
import cn.drajun.mybatis.annotations.Insert;
import cn.drajun.mybatis.annotations.Select;
import cn.drajun.mybatis.annotations.Update;
import cn.drajun.mybatis.binding.MapperMethod;
import cn.drajun.mybatis.builder.MapperBuilderAssistant;
import cn.drajun.mybatis.mapping.SqlCommandType;
import cn.drajun.mybatis.mapping.SqlSource;
import cn.drajun.mybatis.scripting.LanguageDriver;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.ResultHandler;
import cn.drajun.mybatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 注解配置构造器
 */
public class MapperAnnotationBuilder {

    Logger logger = LoggerFactory.getLogger(MapperAnnotationBuilder.class);

    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();

    private Configuration configuration;
    private MapperBuilderAssistant assistant;
    private Class<?> type;

    /**
     *
     * @param configuration
     * @param type 配置文件中的Dao接口，该接口声明了增删改查操作方法和注解
     */
    public MapperAnnotationBuilder(Configuration configuration, Class<?> type){
        String resource = type.getName().replace(".", "/") + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;

        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);
    }

    public void parse(){
        String resource = type.toString();
        if(!configuration.isResourceLoaded(resource)){
            assistant.setCurrentNamespace(type.getName());

            Method[] methods = type.getMethods();
            for(Method method:methods){
                if(!method.isBridge()){
                    parseStatement(method);
                }
            }
        }
    }

    /**
     * 解析语句
     * @param method
     */
    private void parseStatement(Method method){
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = getLanguageDriver(method);
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);

        if(sqlSource != null){
            final String mappedStatementId = type.getName() + "." + method.getName();
            SqlCommandType sqlCommandType = getSqlCommandType(method);
            boolean isSelect = sqlCommandType == sqlCommandType.SELECT;

            String resultMapId = null;
            if(isSelect){
                resultMapId = parseResultMap(method);
            }

            // 调用助手类
            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    sqlCommandType,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    languageDriver
            );
        }
    }

    /**
     * DAO 方法的返回类型，如果为 List 则需要获取集合中的对象类型
     * @param method
     * @return
     */
    private Class<?> getReturnType(Method method){
        Class<?> returnType = method.getReturnType();
        if(Collection.class.isAssignableFrom(returnType)){
            Type returnTypeParameter = method.getGenericReturnType();
            if(returnTypeParameter instanceof ParameterizedType){
                Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
                if(actualTypeArguments != null && actualTypeArguments.length == 1){
                    returnTypeParameter = actualTypeArguments[0];
                    if(returnTypeParameter instanceof Class){
                        returnType = (Class<?>) returnTypeParameter;
                    }
                    else if(returnTypeParameter instanceof ParameterizedType){
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                    else if(returnTypeParameter instanceof GenericArrayType){
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }
        return returnType;
    }

    private String parseResultMap(Method method){
        StringBuilder suffix = new StringBuilder();
        for(Class<?> c : method.getParameterTypes()){
            suffix.append(".");
            suffix.append(c.getSimpleName());
        }
        if(suffix.length() < 1){
            suffix.append("-void");
        }
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        // 添加ResultType
        Class<?> returnType = getReturnType(method);
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>());
        return resultMapId;
    }

    /**
     * 判断是哪种SQL命令（增删改查）
     * @param method
     * @return
     */
    private SqlCommandType getSqlCommandType(Method method){
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if(type == null){
            return SqlCommandType.UNKNOWN;
        }
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver){
        try{
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            if(sqlAnnotationType != null){
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        }
        catch (Exception e){
            throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e);
        }
    }

    /**
     * 拼接注解上的sql、参数
     * @param strings
     * @param parameterTypeClass
     * @param languageDriver
     * @return
     */
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver){
        final StringBuilder sql = new StringBuilder();
        for(String fragment : strings){
            sql.append(fragment);
            sql.append(" ");
        }
        logger.info("SQL语句为{}", sql.toString());
        return languageDriver.createSqlSource(configuration, sql.toString(), parameterTypeClass);
    }

    /**
     * 注解的类型（增删改查）
     * @param method
     * @return
     */
    private Class<? extends Annotation> getSqlAnnotationType(Method method){
        for(Class<? extends Annotation> type: sqlAnnotationTypes){
            Annotation annotation = method.getAnnotation(type);
            if(annotation != null) return type;
        }
        return null;
    }

    private LanguageDriver getLanguageDriver(Method method){
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        return configuration.getLanguageRegistry().getDriver(langClass);
    }

    private Class<?> getParameterType(Method method){
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(Class<?> clazz : parameterTypes){
            if(!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)){
                if(parameterType == null){
                    parameterType = clazz;
                }
                else{
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

}
