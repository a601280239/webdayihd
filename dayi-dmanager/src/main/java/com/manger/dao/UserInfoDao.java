package com.manger.dao;

import com.hqf.pojo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @ClassName UserInfoDao
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
public interface UserInfoDao extends JpaRepository<UserInfo,Long>{

    public List<UserInfo> findByLoginName(String loginName);


}