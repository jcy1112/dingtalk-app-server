package com.softeng.dingtalk.service;

import com.softeng.dingtalk.entity.AcItem;
import com.softeng.dingtalk.entity.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @create 12/26/2019 3:46 PM
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AuditServiceTest {
    @Autowired
    AuditService auditService;

    @Test
    public void test_getPendingApplication() {
        auditService.getPendingApplication(1);
    }
}
