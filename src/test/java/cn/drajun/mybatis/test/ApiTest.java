package cn.drajun.mybatis.test;

import cn.drajun.mybatis.binding.MapperProxyFactory;
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
        MapperProxyFactory<IUserDao> factory = new MapperProxyFactory<>(IUserDao.class);

        Map<String, String> sqlSession = new HashMap<>();
        sqlSession.put("cn.drajun.mybatis.test.dao.IUserDao.queryUserName", "模拟执行 Mapper.xml中的查询用户名sql语句");

        IUserDao userDao = factory.newInstance(sqlSession);
        String result = userDao.queryUserName("1");
        logger.info("测试结果:{}", JSON.toJSONString(result));

    }

}
