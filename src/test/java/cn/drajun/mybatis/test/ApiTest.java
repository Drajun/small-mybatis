package cn.drajun.mybatis.test;

import cn.drajun.mybatis.binding.MapperProxyFactory;
import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.builder.xml.XMLConfigBuilder;
import cn.drajun.mybatis.io.Resources;
import cn.drajun.mybatis.session.Configuration;
import cn.drajun.mybatis.session.SqlSession;
import cn.drajun.mybatis.session.SqlSessionFactory;
import cn.drajun.mybatis.session.SqlSessionFactoryBuilder;
import cn.drajun.mybatis.session.defaults.DefaultSqlSession;
import cn.drajun.mybatis.session.defaults.DefaultSqlSessionFactory;
import cn.drajun.mybatis.test.dao.IUserDao;
import cn.drajun.mybatis.test.po.User;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 测试
        User user = userDao.queryUserInfoById(1L);
        logger.info("测试结果：{}", JSON.toJSONString(user));
    }

    @Test
    public void test_selectOne() throws IOException{
        // 解析XML
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        Configuration configuration = xmlConfigBuilder.parse();

        // 获取DefaultSqlSession
        SqlSession sqlSession = new DefaultSqlSession(configuration);

        // 执行查询
        Object[] req = {1L};
        Object res = sqlSession.selectOne("cn.drajun.mybatis.test.dao.IUserDao.queryUserInfoById", req);
        logger.info("测试结果：{}", JSON.toJSONString(res));
    }
}
