package com.softeng.dingtalk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author zhanyeye
 * @description 用户实体类
 * @date 11/13/2019
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@SQLDelete(sql = "update `user` set is_deleted = 1 where id = ?")
public class User {
    /**
     * 表示用户权限的静态常量
     */
    public static final int USER_AUTHORITY = 0;
    public static final int AUDITOR_AUTHORITY = 1;
    public static final int ADMIN_AUTHORITY = 2;

    /**
     * 表示用户学位的静态常量
     */
    public static final String OTHER = "其他";
    public static final String UNDERGRADUATE = "本科生";
    public static final String POSTGRADUATE = "硕士生";
    public static final String DOCTOR = "博士生";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * 钉钉用户userID
     */
    @Column(unique = true)
    private String userid;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户权限
     */
    private int authority = USER_AUTHORITY;

    /**
     * 用户职（学）位
     */
    private String position = User.OTHER;

    /**
     * 插入时间
     */
    @JsonIgnore
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false, insertable = false)
    private LocalDateTime insertTime;

    /**
     * 软删除标识
     */
    @Column(nullable = false, name = "is_deleted")
    private boolean deleted;


    public User(String userid, String name, String avatar, int authority, String position) {
        this.userid = userid;
        this.name = name;
        this.avatar = avatar;
        this.authority = authority;
        this.position = position;
    }

    public User(int id) {
        this.id = id;
    }
}
