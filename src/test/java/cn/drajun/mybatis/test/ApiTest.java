package cn.drajun.mybatis.test;

import cn.drajun.mybatis.binding.MapperProxyFactory;
import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.builder.xml.XMLConfigBuilder;
import cn.drajun.mybatis.datasource.pooled.PooledDataSource;
import cn.drajun.mybatis.io.Resources;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;
import cn.drajun.mybatis.session.SqlSessionFactory;
import cn.drajun.mybatis.session.SqlSessionFactoryBuilder;
import cn.drajun.mybatis.session.defaults.DefaultSqlSession;
import cn.drajun.mybatis.session.defaults.DefaultSqlSessionFactory;
import cn.drajun.mybatis.test.dao.IUserDao;
import cn.drajun.mybatis.test.po.User;
import cn.hutool.json.JSONNull;
import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException{
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_pooled() throws SQLException, InterruptedException{
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver("com.mysql.jdbc.Driver");
        pooledDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/small-mybatis?useUnicode=true");
        pooledDataSource.setUsername("root");
        pooledDataSource.setPassword("123456");
        while (true){
            Connection connection = pooledDataSource.getConnection();
            System.out.println(connection);
            Thread.sleep(1000);
            connection.close();
        }
    }

    @Test
    public void test_SqlSessionFactory() throws IOException{
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        for(int i=0;i<50;i++){
            User user = userDao.queryUserInfoById(1L);
            logger.info("{}:测试结果:{}", i, JSON.toJSONString(user));
        }
    }

    @Test
    public void test_multiThread() throws IOException {
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        ExecutorService executorService = Executors.newFixedThreadPool(15);
        for(int i=0;i<15;i++){
            executorService.execute(()->{
                User user = userDao.queryUserInfoById(1L);
                logger.info("{}:测试结果:{}", Thread.currentThread().getName(), JSON.toJSONString(user));
            });
        }
        System.in.read();
        executorService.shutdown();

    }

    @Test
    public void test_SqlSession() throws IOException{
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        User user = userDao.queryUserInfoById(1L);
        logger.info("{}:测试结果:{}", Thread.currentThread().getName(), JSON.toJSONString(user));
    }


    @Test
    public void test_queryUserInfo() throws IOException{
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        User user = userDao.queryUserInfo(new User(1L, "10001"));
        logger.info("测试结果：{}", JSON.toJSONString(user));
    }

    @Test
    public void test_insertUserInfo(){
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        User user = new User();
        user.setUserId("10002");
        user.setUserName("小白");
        user.setUserHead("1_05");
        userDao.insertUserInfo(user);

        logger.info("测试结果：{}", "Insert OK");

        // 3. 提交事务
        sqlSession.commit();
    }

    @Test
    public void test_queryUserInfoList() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 2. 测试验证：对象参数
        List<User> users = userDao.queryUserInfoList();
        logger.info("测试结果：{}", JSON.toJSONString(users));
    }

    @Test
    public void test_updateUserInfo() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 2. 测试验证
        int count = userDao.updateUserInfo(new User(1L, "10001", "Drajun"));
        logger.info("测试结果：{}", count);

        // 3. 提交事务
        sqlSession.commit();
    }

    @Test
    public void test_deleteUserInfoByUserId() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 2. 测试验证
        int count = userDao.deleteUserInfoByUserId("10002");
        logger.info("测试结果：{}", count == 1);

        // 3. 提交事务
        sqlSession.commit();
    }




}
