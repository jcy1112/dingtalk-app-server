package com.softeng.dingtalk.aspect;


import com.softeng.dingtalk.entity.AcRecord;
import com.softeng.dingtalk.repository.AcRecordRepository;
import com.softeng.dingtalk.service.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Aspect
@Component
@Slf4j
public class ACBlockchainAspect {
    @Autowired
    SystemService systemService;
    @Autowired
    AcRecordRepository acRecordRepository;
    @Pointcut("execution(* com.softeng.dingtalk.repository.AcRecordRepository.save(..))")
    public void saveRecord(){
    }
    public static List timeCostList=new ArrayList<>();
    @Before("saveRecord()")
    public void beforeAction(JoinPoint point){
        AcRecord param=(AcRecord) point.getArgs()[0];
        if(param.getId()==null){
            //AcRecord acRecord=acRecordRepository.findById(acRecordId).get();
            log.info("before:"+param.toString());
        }else{
            String value=param.toString();
        }
    }

    @After("saveRecord()")
    public void afterAction(JoinPoint point){
        AcRecord param=(AcRecord) point.getArgs()[0];
        log.info("after:"+param.toString());
    }

}
