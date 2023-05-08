package cn.drajun.mybatis.executor;


import cn.drajun.mybatis.executor.statement.StatementHandler;
import cn.drajun.mybatis.mapping.BoundSql;
import cn.drajun.mybatis.mapping.MappedStatement;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.ResultHandler;
import cn.drajun.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 简单执行器
 */
public class SimpleExecutor extends BaseExecutor{

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        try{
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, resultHandler, boundSql);
            Connection connection = transaction.getConnection();
            Statement stmt = handler.prepare(connection);
            handler.parameterize(stmt);
            return handler.query(stmt, resultHandler);
        }
        catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
}
