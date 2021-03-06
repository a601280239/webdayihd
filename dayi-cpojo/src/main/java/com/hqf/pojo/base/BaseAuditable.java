package com.hqf.pojo.base;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * @ClassName BaseAuditable
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseAuditable {
    @Column(name = "id")
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;
    @LastModifiedDate
    @Column(name="updateTime")
    Date updateTime;

    @CreatedDate
    @Column(name="createTime")
    Date createTime;
    @Version
    @Column(name = "version")
    private Long version;


}