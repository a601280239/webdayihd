package com.hqf.pojo.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;
    @Column(name = "userId")
    private Long userId;
    @Column(name = "roleId")
    private Long roleId;
}