package com.hqf.pojo.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@Table(name = "base_menu")
public class MenuInfo extends BaseAuditable {

    @Column(name = "menuName")
    private String menuName;
    @JsonSerialize(using = ToStringSerializer.class)
    @Column(name = "parentId")
    private Long parentId;

    @Column(name = "leval")
    private int leval;

    @Column(name = "url")
    private String url;
    @Transient
    private Long []ids;
    @Transient
    private List<MenuInfo> menuInfoList;

}
