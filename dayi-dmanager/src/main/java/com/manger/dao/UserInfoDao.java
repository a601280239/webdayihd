package com.manger.dao;

import com.hqf.pojo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @ClassName UserInfoDao
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
public interface UserInfoDao extends JpaRepository<UserInfo,Long>{
    @Query(value = "select * from base_user where loginName=?1",nativeQuery = true)
    public UserInfo getByLoginName(String loginName);
    public List<UserInfo> findByLoginName(String loginName);
    @Query(value = "select bu.* from base_user_role bur inner  join base_user bu on bur.userId=bu.id where bur.roleId=?1",nativeQuery = true)
    public List<UserInfo> forUserInfoByUserId(Long roleId);
    public List<UserInfo> findByTel(String tel);
    public List<UserInfo> findByUemail(String uemail);


}