package com.manger.dao;

import com.hqf.pojo.entity.RoleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleInfoDao extends JpaRepository<RoleInfo,Long> {
    @Query(value = "select br.* from base_user_role bur inner  join base_role br on bur.roleId=br.id where bur.userId=?1",nativeQuery = true)
    public RoleInfo forRoleInfoByUserId(Long userId);
}
