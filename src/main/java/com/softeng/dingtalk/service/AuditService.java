package com.softeng.dingtalk.service;

import com.softeng.dingtalk.component.DingTalkUtils;
import com.softeng.dingtalk.entity.*;
import com.softeng.dingtalk.repository.*;
import com.softeng.dingtalk.vo.CheckedVO;
import com.softeng.dingtalk.vo.ToCheckVO;
import com.softeng.dingtalk.vo.CheckVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;


/**
 * @author zhanyeye
 * @description 周绩效审核业务逻辑
 * @create 12/26/2019 3:34 PM
 */
@Service
@Transactional
@Slf4j
public class AuditService {
    @Autowired
    AcItemRepository acItemRepository;
    @Autowired
    DcRecordRepository dcRecordRepository;
    @Autowired
    AcRecordRepository acRecordRepository;
    @Autowired
    DcSummaryRepository dcSummaryRepository;
    @Autowired
    DingTalkUtils dingTalkUtils;
    @Autowired
    UserService userService;
    @Autowired
    PerformanceService performanceService;
    @Autowired
    NotifyService notifyService;


    // 审核人更新绩效申请
    public DcRecord updateAudit(CheckVO checkVO) {
        DcRecord dc = dcRecordRepository.findById(checkVO.getId()).get();
        dc.update(checkVO.getCvalue(), checkVO.getDc(), checkVO.getAc()); // 更新 cvalue, dc, ac
        dcRecordRepository.save(dc); //持久化新的 dcRecord
        acItemRepository.deleteByDcRecord(dc); //删除旧数据
        for (AcItem acItem : checkVO.getAcItems()) {
            acItem.setDcRecord(dc);
            if (acItem.isStatus()) {
                AcRecord acRecord = acRecordRepository.save(new AcRecord(dc, acItem));
                acItem.setAcRecord(acRecord);
            }
        }
        acItemRepository.saveAll(checkVO.getAcItems());
        return dc;
    }


    // 审核人提交的审核结果
    public DcRecord addAuditResult(CheckVO checkVO) {
        DcRecord dc = dcRecordRepository.findById(checkVO.getId()).get();
        dc.update(checkVO.getCvalue(), checkVO.getDc(), checkVO.getAc());
        dcRecordRepository.save(dc);
        for (AcItem a : checkVO.getAcItems()) {
            a.setDcRecord(dc); //前端传来的没有dcRecord属性
            if (a.isStatus()) {
                AcRecord acRecord = acRecordRepository.save(new AcRecord(dc, a));
                a.setAcRecord(acRecord);
            }
        }
        acItemRepository.saveAll(checkVO.getAcItems());
        return dc;
    }


    //更新DcSummary数据
    public void updateDcSummary(DcRecord dc) {
        log.debug("update DcSummary" + dc.getId());
        Double dcSum = dcRecordRepository.getUserWeekTotalDc(dc.getApplicant().getId(), dc.getYearmonth(), dc.getWeek());
        DcSummary dcSummary = dcSummaryRepository.getDcSummary(dc.getApplicant().getId(), dc.getYearmonth());
        if (dcSummary == null) {
            dcSummary = new DcSummary(dc.getApplicant(), dc.getYearmonth());
        }
        dcSummary.updateWeek(dc.getWeek(), dcSum);
        dcSummaryRepository.save(dcSummary);
        notifyService.updateDcMessage(dc);  // 发送消息
        performanceService.computeSalary(dc.getApplicant().getId(), dc.getYearmonth());  //重新计算助研金
    }


    // 审核人获取已审核申请
    public List<CheckedVO> listCheckVO(int uid) {
        List<CheckedVO> checkedVOS = dcRecordRepository.listChecked(uid);
        for (CheckedVO checked : checkedVOS) {
            checked.setAcItems(acItemRepository.findAllByDcRecordID(checked.getId()));
        }
        return checkedVOS;
    }

    // 审核人根据时间筛选已审核申请
    public List<CheckedVO> listCheckedByDate(int uid, int yearmonth, int week) {
        List<CheckedVO> checkedVOS = dcRecordRepository.listCheckedByDate(uid, yearmonth, week);
        for (CheckedVO checked : checkedVOS) {
            checked.setAcItems(acItemRepository.findAllByDcRecordID(checked.getId()));
        }
        return checkedVOS;
    }


    // 审核人查看待审核的申请
    public List<ToCheckVO> getPendingApplication(int uid) {
        List<ToCheckVO> toCheckVOList = dcRecordRepository.listToCheckVO(uid);
        for (ToCheckVO toCheckVO : toCheckVOList) {
            toCheckVO.setAcItems(acItemRepository.findAllByDcRecordID(toCheckVO.getId()));
        }
        return toCheckVOList;
    }


    // 查询审核人未审核数
    public int getUnCheckCnt(int aid) {
        return dcRecordRepository.getUnCheckCntByAid(aid);
    }


}
