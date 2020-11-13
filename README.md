<p align="center"><img width="100" src="https://i.loli.net/2020/11/12/8pP5y6eHwX1VfLd.png" alt="logo"></p>

<p align="center">
  <a href="https://github.com/zhanyeye/dingtalk-springboot/actions?query=workflow%3AProd">
    <img src="https://github.com/zhanyeye/dingtalk-springboot/workflows/Prod/badge.svg?branch=master" alt="Prod Status">
  </a>
  <a href="https://github.com/zhanyeye/dingtalk-springboot/actions?query=workflow%3ATest">
    <img src="https://github.com/zhanyeye/dingtalk-springboot/workflows/Test/badge.svg?branch=test">
  </a>
</p>

<h1 align="center">Dingtalk Springboot</h1>

## 目标与期望

基于钉钉微应用开发的实验室绩效管理系统，实现实验室的绩效、学分、论文评审管理与钉钉对接。  
主要功能有：绩效、学分申请与审核，论文评审投票及学分管理，实验室助研金计算等。

## 涉及的技术

<table>
  <tbody>
    <tr>
      <td align="center" valign="middle">
        <a href="https://spring.io/projects/spring-boot" target="_blank">
          <img width="50px" src="https://spring.io/images/spring-initializr-4291cc0115eb104348717b82161a81de.svg">
        </a>
        <p>SpringBoot</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://spring.io/projects/spring-data-jpa#overview" target="_blank">
          <img width="50px" src="https://i.loli.net/2020/11/13/pR8OtwsSacyuDU7.png">
        </a>
        <p>JPA</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://mybatis.org/mybatis-3/" target="_blank">
          <img width="50px" src="https://avatars2.githubusercontent.com/u/1483254?s=200&v=4">
        </a>
        <p>Mybatis</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://dev.mysql.com/downloads/mysql/">
          <img width="50px" src="https://i.loli.net/2020/11/13/GQE3xMAbWd72hVc.png">
        </a>
        <p>MySQL 8</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://ding-doc.dingtalk.com/doc#/faquestions/vzbp02" target="_blank">
          <img width="50px" src="https://i.loli.net/2020/11/13/DVpc9nF2JToQyHg.png">
        </a>
        <p>Dingtalk SDK</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://docs.docker.com/" target="_blank">
          <img width="50px" src="https://i.loli.net/2020/11/13/27eyNzt698aoilM.png">
        </a>
        <p>Docker</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://docs.docker.com/compose/" target="_blank">
          <img width="50px" src="https://i.loli.net/2020/11/13/TcewOXGMWHLiNtE.jpg">
        </a>
        <p>Docker Compose</p>
      </td>
      <td align="center" valign="middle">
        <a href="https://github.com/features/actions" target="_blank">
          <img width="50px" src="https://avatars0.githubusercontent.com/u/44036562?s=200&v=4">
        </a>
        <p>Github Actions</p>
      </td>
    </tr>
  </tbody>
</table>






#### 注意

+ 使用了lombok 插件简化代码，idea 需要安装lombok 插件，否则编译过不去
+ 由于目前钉钉小程序只支持 GET/POST, 考虑到兼容性这里的接口全部为GET/POST方式


###### 2020.09.10
修复发起投票时可能出现的bug

###### 2020.08.13
使用`docker-compose`代替`docker run`

###### 2020.06.23
修复老师也出现在绩效列表中的情况

###### 2020.06.14
测试环境的CD搭建


###### 2020.05.23
添加测试分支，主要分支介绍如下
+ master 分支用于部署
+ test 分支用于测试
+ dev 分支用于开发


###### 2020.04.02
使用 Github Actions 持续集成服务 [参考](https://segmentfault.com/a/1190000021914414)

###### 2020.3.26
完成页面：钉钉鉴权登陆，绩效审核与申请，绩效汇总，AC汇总，bug管理，任务绩效管理，论文管理，内部评审投票


