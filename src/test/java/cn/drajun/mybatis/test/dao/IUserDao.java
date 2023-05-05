package cn.drajun.mybatis.test.dao;

import cn.drajun.mybatis.test.po.User;

public interface IUserDao {

    String queryUserName(String uId);

    Integer queryUserAge(String uId);

    User queryUserInfoById(Long uId);

}
