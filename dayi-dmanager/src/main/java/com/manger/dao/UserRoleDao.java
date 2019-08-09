package com.manger.dao;

import com.hqf.pojo.entity.UserRoleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @ClassName UserRoleDao
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/8
 **/
public interface UserRoleDao extends JpaRepository<UserRoleInfo,Long> {
    @Query(value = "delete from base_user_role where userId=?1",nativeQuery = true)
    @Modifying
    public int delByUserId(Long id);
    public List<UserRoleInfo> findByUserId(Long userid);

}