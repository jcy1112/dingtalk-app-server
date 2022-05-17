package com.softeng.dingtalk.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class VirtualMachine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    //项目组
    String projectTeam;
    //课题
    String subject;
    //email
    String email;
    //开始时间
    LocalDate start;
    //结束时间
    LocalDate end;
    //申请用途
    String purpose;
    //CPU核心数
    int coreNum;
    //内存大小 GB为单位
    int memory;
    //硬盘容量 GB为单位
    int capacity;
    //操作系统
    String operationSystem;
    //申请日期
    LocalDate applyDate;
}
