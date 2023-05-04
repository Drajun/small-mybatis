package cn.drajun.mybatis.test;

import cn.drajun.mybatis.binding.MapperProxyFactory;
import cn.drajun.mybatis.binding.MapperRegistry;
import cn.drajun.mybatis.session.SqlSession;
import cn.drajun.mybatis.session.SqlSessionFactory;
import cn.drajun.mybatis.session.defaults.DefaultSqlSessionFactory;
import cn.drajun.mybatis.test.dao.IUserDao;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ApiTest {
    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_MapperProxyFactory(){
        // 注册Mapper
        MapperRegistry registry = new MapperRegistry();
        registry.addMappers("cn.drajun.mybatis.test.dao");

        // 从SqlSession工厂获取sqlSession
        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(registry);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 获取映射器对象(代理)
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 执行查询方法
        String res = userDao.queryUserName("1001");
        logger.info("测试结果：{}", res);
    }

}
