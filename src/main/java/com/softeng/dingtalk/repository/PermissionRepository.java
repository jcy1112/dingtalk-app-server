package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.Permission;
import org.springframework.stereotype.Repository;

/**
 * @author LiXiaoKang
 * @description 操作Permission实体类的接口
 * @date 2/5/2023
 */

@Repository
public interface PermissionRepository extends CustomizedRepository<Permission, Integer>{

    Permission findById(int id);
}