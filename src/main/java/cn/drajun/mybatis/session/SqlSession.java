package cn.drajun.mybatis.session;

/**
 * 获取映射器，执行SQL，管理事务
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
