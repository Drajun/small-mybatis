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
import cn.drajun.mybatis.transaction.Transaction;
import cn.drajun.mybatis.transaction.jdbc.JdbcTransactionFactory;
import cn.drajun.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

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

    public Configuration(){
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
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
}
