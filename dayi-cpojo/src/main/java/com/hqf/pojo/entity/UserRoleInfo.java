package com.hqf.pojo.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @ClassName UserRoleInfo
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/8
 **/
@Data
@Entity
@Table(name = "base_user_role")
public class UserRoleInfo {
    @Column(name = "id")
    @Id
    Long id;
    @Column(name = "userId")
    private Long userId;
    @Column(name = "roleId")
    private Long roleId;
}