package com.manger.dao;

import com.hqf.pojo.entity.MenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuInfoDao extends JpaRepository<MenuInfo,Long> {
    @Query(value = "select * from base_role_menu brm inner join base_menu bm on brm.menuId=bm.id where brm.roleId=?1 and bm.parentId=?2",nativeQuery = true)
    public List<MenuInfo> getFirstMenuInfo(Long roleId, Long parentId);
}
