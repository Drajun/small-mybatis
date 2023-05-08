package cn.drajun.mybatis.session.defaults;

import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.executor.Executor;
import cn.drajun.mybatis.mapping.Environment;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;
import cn.drajun.mybatis.session.SqlSessionFactory;
import cn.drajun.mybatis.session.TransactionIsolationLevel;
import cn.drajun.mybatis.transaction.Transaction;
import cn.drajun.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration){
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Transaction tx = null;
        try{
            final Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();
            tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);

            // 创建执行器
            final Executor executor = configuration.newExecutor(tx);
            // 创建DefaultSqlSession
            return new DefaultSqlSession(configuration, executor);
        }
        catch (Exception e){
            try {
                assert tx != null;
                tx.close();
            }
            catch (SQLException ignore){

            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }
}
