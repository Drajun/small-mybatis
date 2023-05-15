package cn.drajun.mybatis.session;

import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.datasource.druid.DruidDataSourceFactory;
import cn.drajun.mybatis.datasource.pooled.PooledDataSourceFactory;
import cn.drajun.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import cn.drajun.mybatis.executor.Executor;
import cn.drajun.mybatis.executor.SimpleExecutor;
import cn.drajun.mybatis.executor.resultset.DefaultResultSetHandler;
import cn.drajun.mybatis.executor.resultset.ResultSetHandler;
import cn.drajun.mybatis.executor.statement.PreparedStatementHandler;
import cn.drajun.mybatis.executor.statement.StatementHandler;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.Environment;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.reflection.MetaObject;
import cn.drajun.mybatis.reflection.factory.DefaultObjectFactory;
import cn.drajun.mybatis.reflection.factory.ObjectFactory;
import cn.drajun.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.drajun.mybatis.reflection.wrapper.ObjectWrapperFactory;
import cn.drajun.mybatis.scripting.LanguageDriverRegistry;
import cn.drajun.mybatis.scripting.xmltags.XMLLanguageDriver;
import cn.drajun.mybatis.transaction.Transaction;
import cn.drajun.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.drajun.mybatis.type.TypeAliasRegistry;
import cn.drajun.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 配置项
 * 串联Mybatis的流程
 */
public class Configuration {

    /**
     * 环境
     */
    protected Environment environment;

    /**
     * 映射注册机
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 映射的语句，存在Map里
     * key是接口中的方法名，value是sql的封装对象
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 类型别名注册机
     */
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    /**
     * 类型处理器注册机
     */
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    /**
     * 对象工厂和对象包装器工厂
     */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    /**
     * 已经加载（解析）的资源
     */
    protected final Set<String> loadedResources = new HashSet<>();

    protected String databaseId;

    public Configuration(){
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getDatabaseId(){
        return databaseId;
    }

    /**
     * 创建结果集处理器
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql){
        return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction){
        return new SimpleExecutor(this, transaction);
    }

    /**
     * 创建语句处理器
     * @param executor
     * @param mappedStatement
     * @param parameter
     * @param resultHandler
     * @param boundSql
     * @return
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql){
        return new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, boundSql);
    }

    /**
     * 创建元对象
     * @param object
     * @return
     */
    public MetaObject newMetaObject(Object object){
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    public TypeHandlerRegistry getTypeHandlerRegistry(){
        return typeHandlerRegistry;
    }

    public boolean isResourceLoaded(String resource){
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource){
        loadedResources.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }
}
