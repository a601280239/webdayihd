package com.hqf.pojo.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName RoleMenuInfo
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/9
 **/
@Data
@Entity
@Table(name = "base_role_menu")
public class RoleMenuInfo {
    @Column(name = "id")
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;
    @Column(name = "menuId")
    private Long menuId;
    @Column(name = "roleId")
    private Long roleId;
}