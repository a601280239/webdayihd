package com.manger.dao;

import com.hqf.pojo.entity.RoleMenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @ClassName RoleMenuDao
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/9
 **/
public interface RoleMenuDao extends JpaRepository<RoleMenuInfo,Long> {
    @Query(value = "delete from base_role_menu where roleId=?1",nativeQuery = true)
    @Modifying
    public int delByRoleID(Long id);
    public int deleteByRoleId(Long id);
    public List<RoleMenuInfo> findByRoleId(long id);
    public List<RoleMenuInfo> findByMenuId(Long id);
    @Modifying
    @Query(value = "insert into base_role_menu(id,roleId,menuId) values(?1,?2,?3)",nativeQuery = true)
    public int insertRoleMenuInfo(Long id,Long rid,Long mid);
}