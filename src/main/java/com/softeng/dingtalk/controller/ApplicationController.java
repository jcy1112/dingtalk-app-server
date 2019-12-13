package com.softeng.dingtalk.controller;

import com.softeng.dingtalk.component.DingTalkUtils;
import com.softeng.dingtalk.dto.ApplicationInfo;
import com.softeng.dingtalk.entity.AcItem;
import com.softeng.dingtalk.entity.Application;
import com.softeng.dingtalk.entity.User;
import com.softeng.dingtalk.repository.AcItemRepository;
import com.softeng.dingtalk.service.AcItemService;
import com.softeng.dingtalk.service.ApplicationService;
import com.softeng.dingtalk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

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
    AcItemService acItemService;
    @Autowired
    UserService userService;
    @Autowired
    DingTalkUtils dingTalkUtils;

    //根据uid获取周报
    @GetMapping("/report/{uid}")
    public Map getReport(@RequestAttribute int uid) {
        log.debug(uid+"");
        String userid = userService.getUserid(uid);
        return dingTalkUtils.getReport(userid);
    }

    //用户提交申请
    @PostMapping("/application")
    public void addApplication(@RequestBody ApplicationInfo applicationInfo) {
        Application application = applicationInfo.getApplication();
        List<AcItem> acItems = applicationInfo.getAcItems();                   //获取该绩效申请的ac申请
        //拼接 month, week, applicant_id, auditor_id 字段，插入flag约束字段中
        application.setFlag(application.getMonth()+ "-" + application.getWeek() + "-" + application.getApplicant().getId() + "-" + application.getAuditor().getId());
        Application a = applicationService.addApplication(application);    //持久化绩效申请
        acItemService.addAcItemList(acItems, a);                           //持久化ac申请，并将绩效申请作为外键
    }

    //用户分页查询已提交的申请
    @GetMapping("application/{uid}/page={page}")
    public Map getUserApplication(@RequestAttribute int uid, @PathVariable int page) {
        List<Application> applications = applicationService.getApplications(uid, page);
        return Map.of("applications", applications);
    }


    //审核人获取待审核的申请
    @GetMapping("/pending_audit/{uid}")
    public List<Application> getAuditApplication(@RequestAttribute int uid) {
        return applicationService.getPendingApplication(uid);
    }

    //TODO 审核人提交审核
}
