package com.softeng.dingtalk.service;

import com.softeng.dingtalk.entity.*;
import com.softeng.dingtalk.repository.*;
import com.softeng.dingtalk.vo.CheckedVO;
import com.softeng.dingtalk.vo.ToCheckVO;
import com.softeng.dingtalk.vo.CheckVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    UserService userService;
    @Autowired
    PerformanceService performanceService;
    @Autowired
    NotifyService notifyService;


    /**
     * 更新审核结果
     * @param checkVO 审核人提交的审核结果
     * @return
     */
    public DcRecord updateAuditResult(CheckVO checkVO) {
        DcRecord dc = dcRecordRepository.findById(checkVO.getId()).get();
        if (dc.isStatus()) {
            // status为真，表示之前审核过，此次提交为更新, 删除旧的AcItems， 同时级联删除相关AcRecord
            acItemRepository.deleteByDcRecord(dc);
        }
        // 更新 cvalue, dc, ac
        dc.update(checkVO.getCvalue(), checkVO.getDc(), checkVO.getAc());
        checkVO.getAcItems().forEach(acItem -> {
            // 前端传来的没有dcRecord属性, 手动添加
            acItem.setDcRecord(dc);
            if (acItem.isStatus()) {
                // ac申请被同意
                AcRecord acRecord = acRecordRepository.save(new AcRecord(dc, acItem, dc.getInsertTime()));
                acItem.setAcRecord(acRecord);
            }
        });
        acItemRepository.saveAll(checkVO.getAcItems());
        return dc;
    }

    /**
     * 当用户某条周绩效申请被审核时，需要更新DcSummary数据
     * @param uid 用户id
     * @param yearmonth 所在年月
     * @param week 所在周
     */
    public void updateDcSummary(int uid, int yearmonth, int week) {
        // 某一周指定用户的dc之和
        Double dcSum = dcRecordRepository.getUserWeekTotalDc(uid, yearmonth, week);
        DcSummary dcSummary = dcSummaryRepository.getDcSummary(uid, yearmonth);
        if (dcSummary == null) {
            dcSummary = new DcSummary(uid, yearmonth);
        }
        dcSummary.updateWeek(week, dcSum);
        dcSummaryRepository.save(dcSummary);
        //重新计算助研金
        performanceService.computeSalary(uid, yearmonth);
    }


    /**
     * 审核人分页获取已审核申请
     * @param uid
     * @param page
     * @param size
     * @return
     */
    public Map listCheckedVO(int uid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<CheckedVO> pages = dcRecordRepository.listChecked(uid, pageable);
        List<CheckedVO> contents = pages.getContent();
        contents.forEach(checked -> {
            checked.setAcItems(acItemRepository.findAllByDcRecordID(checked.getId()));
        });
        return Map.of("content", contents, "total", pages.getTotalElements());
    }


    /**
     * 审核人根据时间筛选已审核申请
     * @param uid
     * @param yearmonth
     * @param week
     * @return
     */
    public List<CheckedVO> listCheckedByDate(int uid, int yearmonth, int week) {
        List<CheckedVO> checkedVOS = dcRecordRepository.listCheckedByDate(uid, yearmonth, week);
        checkedVOS.forEach(vo -> {
            vo.setAcItems(acItemRepository.findAllByDcRecordID(vo.getId()));
        });
        return checkedVOS;
    }


    /**
     * 审核人查看待审核的申请
     * @param uid 审核人的id
     * @return
     */
    public List<ToCheckVO> getPendingApplication(int uid) {
        List<ToCheckVO> toCheckVOList = dcRecordRepository.listToCheckVO(uid);
        toCheckVOList.forEach(toCheckVO -> {
            toCheckVO.setAcItems(acItemRepository.findAllByDcRecordID(toCheckVO.getId()));
        });
        return toCheckVOList;
    }


    /**
     * 查询审核人未审核数
     * @param aid 审核人id
     * @return
     */
    public int getUnCheckCnt(int aid) {
        return dcRecordRepository.getUnCheckCntByAid(aid);
    }


}
