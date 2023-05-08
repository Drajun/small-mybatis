package cn.drajun.mybatis.executor;

import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.session.ResultHandler;
import cn.drajun.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器接口
 */
public interface Executor {
    ResultHandler NO_RESULT_HANDLER = null;

    <E>List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);

    Transaction getTransaction();

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollBack);
}
