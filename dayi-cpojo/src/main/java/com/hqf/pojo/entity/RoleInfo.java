package com.hqf.pojo.entity;

import com.hqf.pojo.base.BaseAuditable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * 作者: LCG
 * 日期: 2019/8/4 16:30
 * 描述:
 */
@Entity
@Data
@Table(name = "base_role")
public class RoleInfo extends BaseAuditable {

    @Column(name = "roleName")
    private String roleName;

    @Column(name = "miaoShu")
    private String miaoShu;
    @Column(name = "leval")
    private Integer leval;
    @Transient
    private List<UserInfo> userInfos;
    @Transient
    private List<MenuInfo> menuInfoList;
    @Transient
    private List<MenuInfo> menuInfos;
    @Transient
    private String ids[];
    @Transient
    private Long urid;


}
