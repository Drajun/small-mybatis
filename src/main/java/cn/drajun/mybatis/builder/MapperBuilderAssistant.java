package cn.drajun.mybatis.builder;

import cn.drajun.mybatis.mapping.*;
import cn.drajun.mybatis.scripting.LanguageDriver;
import cn.drajun.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 映射构建器助手
 */
public class MapperBuilderAssistant extends BaseBuilder{

    private String currentNamespace;
    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    // 为方法名加上命名空间
    public String applyCurrentNamespace(String base, boolean isReference){
        if(base == null){
            return null;
        }
        if (isReference) {
            if (base.contains(".")) return base;
        } else {
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }

    /**
     * 添加映射语句
     * @param id
     * @param sqlSource
     * @param sqlCommandType
     * @param parameterType
     * @param resultMap
     * @param resultType
     * @param lang
     * @return
     */
    public MappedStatement addMappedStatement(String id, SqlSource sqlSource, SqlCommandType sqlCommandType, Class<?> parameterType, String resultMap, Class<?> resultType, LanguageDriver lang){
        // 给id加上namespace前缀：cn.drajun.mybatis.test.dao.IUserDao.queryUserInfoById
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);

        // 结果映射，给MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement statement = statementBuilder.build();
        // 映射语句信息，建造完存放到配置项中
        configuration.addMappedStatement(statement);

        return statement;
    }

    /**
     * 设置语句的结果映射
     * @param resultMap
     * @param resultType
     * @param statementBuilder
     */
    private void setStatementResultMap(String resultMap, Class<?> resultType, MappedStatement.Builder statementBuilder){
        // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
        resultMap = applyCurrentNamespace(resultMap, true);
        List<ResultMap> resultMaps = new ArrayList<>();
        if(resultMap != null){
            String[] resultMapNames = resultMap.split(",");
            for(String resultMapName : resultMapNames){
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        }
        else if(resultType != null){
            /*
             * 通常使用 resultType 即可满足大部分场景
             * <select id="queryUserInfoById" resultType="cn.bugstack.mybatis.test.po.User">
             * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
             */
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, statementBuilder.id() + "-Inline", resultType, new ArrayList<>());
            resultMaps.add(inlineResultMapBuilder.build());
        }
        statementBuilder.resultMaps(resultMaps);
    }

    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings){
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, id, type, resultMappings);

        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

}
