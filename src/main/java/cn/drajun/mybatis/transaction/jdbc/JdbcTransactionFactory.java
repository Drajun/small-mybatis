package cn.drajun.mybatis.transaction.jdbc;

import cn.drajun.mybatis.session.TransactionIsolationLevel;
import cn.drajun.mybatis.transaction.Transaction;
import cn.drajun.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * JdbcTransaction 工厂
 */
public class JdbcTransactionFactory implements TransactionFactory {
    @Override
    public Transaction newTransaction(Connection connection) {
        return new JdbcTransaction(connection);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
