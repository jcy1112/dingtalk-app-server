package com.softeng.dingtalk.controller;

import com.softeng.dingtalk.component.DingTalkUtils;
import com.softeng.dingtalk.dto.ApplicationInfo;
import com.softeng.dingtalk.entity.AcItem;
import com.softeng.dingtalk.entity.DcRecord;
import com.softeng.dingtalk.service.ApplicationService;
import com.softeng.dingtalk.service.AuditService;
import com.softeng.dingtalk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * @author zhanyeye
 * @description
 * @create 12/11/2019 1:59 PM
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApplicationController {
    @Autowired
    ApplicationService applicationService;
    @Autowired
    AuditService auditService;
    @Autowired
    UserService userService;
    @Autowired
    DingTalkUtils dingTalkUtils;


    /**
     * @Description 根据uid获取周报
     * @Param [uid]
     * @return java.util.Map
     * @Date 7:01 PM 12/27/2019
     **/
    @GetMapping("/report/{uid}")
    public Map getReport(@RequestAttribute int uid) {
        log.debug("根据uid获取周报,uid" + uid);
        String userid = userService.getUserid(uid);
        return dingTalkUtils.getReport(userid);
    }



    /**
     * @Description 用户提交申请
     * @Param [applicationInfo]
     * @return void
     * @Date 7:01 PM 12/27/2019
     **/
    @PostMapping("/application")
    public void addApplication(@RequestBody ApplicationInfo applicationInfo) {
        DcRecord dcRecord = applicationInfo.getDcRecord();       //获取DC申请信息
        List<AcItem> acItems = applicationInfo.getAcItems();     //获取该绩效申请的ac申请
        int uid = dcRecord.getApplicant().getId();
        int aid = dcRecord.getAuditor().getId();
        int week = dcRecord.getWeek();
        if (applicationService.isExist(uid, aid, week) == false) {
            applicationService.addApplication(dcRecord, acItems);    //持久化绩效申请
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "！！！！");
        }

    }


    /**
     * @Description 用户分页查询已提交的申请
     * @Param [uid, page]
     * @return java.util.Map
     * @Date 7:00 PM 12/27/2019
     **/
    @GetMapping("application/{uid}/page={page}")
    public Map getUserApplication(@RequestAttribute int uid, @PathVariable int page) {
        List<DcRecord> dcRecords = applicationService.getDcRecord(uid, page);
        return Map.of("dcRecords", dcRecords);
        //
    }



}
