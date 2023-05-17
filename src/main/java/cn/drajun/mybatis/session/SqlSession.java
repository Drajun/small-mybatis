package cn.drajun.mybatis.session;

import java.util.List;

/**
 * 获取映射器，执行SQL，管理事务
 * 通常情况下，我们在应用程序中使用的Mybatis的API就是这个接口定义的方法。
 */
public interface SqlSession {

    /**
     * 根据指定的SQLid获取一条记录的封装对象
     * @param statement
     * @param <T>
     * @return Mapped object 封装之后的对象
     */
    <T> T selectOne(String statement);

    /**
     * 根据指定的SQL和参数获取一条记录的封装对象
     * @param statement
     * @param parameter
     * @param <T>
     * @return
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * 获取多条记录
     * @param statement
     * @param parameter
     * @param <E>
     * @return
     */
    <E> List<E> selectList(String statement, Object parameter);

    /**
     * 插入记录
     * @param statement
     * @param parameter
     * @return
     */
    int insert(String statement, Object parameter);

    /**
     * 更新记录
     * @param statement
     * @param parameter
     * @return
     */
    int update(String statement, Object parameter);

    /**
     * 删除记录
     * @param statement
     * @param parameter
     * @return
     */
    Object delete(String statement, Object parameter);

    /**
     * 事务控制方法，提交
     */
    void commit();

    /**
     * 获取映射器
     * @param type
     * @param <T>
     * @return
     */
    <T> T getMapper(Class<T> type);


    /**
     * 得到配置
     * @return
     */
    Configuration getConfiguration();

}
