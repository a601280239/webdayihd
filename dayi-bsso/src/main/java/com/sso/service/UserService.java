package com.sso.service;

import com.hqf.pojo.entity.UserInfo;
import com.sso.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@Component
public class UserService {
    @Autowired
    private UserDao userDao;
    public UserInfo getUserByLogin(String lohinName){
        return userDao.findByLoginName(lohinName);
    }
}