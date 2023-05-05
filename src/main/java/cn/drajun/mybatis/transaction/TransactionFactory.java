package cn.drajun.mybatis.transaction;

import cn.drajun.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 事务工厂
 */
public interface TransactionFactory {

    /**
     * 根据Connection创建Transaction
     * @param connection
     * @return
     */
    Transaction newTransaction(Connection connection);

    /**
     * 根据数据源和事务隔离级别创建Transaction
     * @param dataSource
     * @param level
     * @param autoCommit
     * @return
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
