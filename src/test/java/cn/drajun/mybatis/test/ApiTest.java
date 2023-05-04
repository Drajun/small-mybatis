package cn.drajun.mybatis.test;

import cn.drajun.mybatis.binding.MapperProxyFactory;
import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.io.Resources;
import cn.drajun.mybatis.session.SqlSession;
import cn.drajun.mybatis.session.SqlSessionFactory;
import cn.drajun.mybatis.session.SqlSessionFactoryBuilder;
import cn.drajun.mybatis.session.defaults.DefaultSqlSessionFactory;
import cn.drajun.mybatis.test.dao.IUserDao;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory() throws IOException {
        // 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 测试
        String res = userDao.queryUserInfoById("10001");
        logger.info("测试结果：{}", res);
    }

}
