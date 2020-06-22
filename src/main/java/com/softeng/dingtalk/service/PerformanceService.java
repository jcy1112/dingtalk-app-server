package com.softeng.dingtalk.service;

import com.softeng.dingtalk.entity.DcSummary;
import com.softeng.dingtalk.entity.User;
import com.softeng.dingtalk.enums.Position;
import com.softeng.dingtalk.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author zhanyeye
 * @description
 * @create 2/6/2020 9:48 AM
 */
@Service
@Transactional
@Slf4j
public class PerformanceService {
    @Autowired
    DcSummaryRepository dcSummaryRepository;
    @Autowired
    AcRecordRepository acRecordRepository;
    @Autowired
    TopupRepository topupRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BugRepository bugRepository;
    @Autowired
    SystemService systemService;


    /**
     * 计算助研金 Gain = Base * DC * (1+ (AC/50)) + Topup
     * @param uid
     * @param yearmonth
     */
    public void computeSalary(int uid, int yearmonth) {
        double dc = dcSummaryRepository.getDcTotal(uid, yearmonth);
        // 获取到目前为止用户的AC总和
        double ac = acRecordRepository.getUserAcByDate(uid, yearmonth);
        // 获取用户的 Topup
        double topup = dcSummaryRepository.findTopup(uid, yearmonth);
        Position position = userRepository.getUserPosition(uid);
        double base = systemService.getSubsidy(position);
        log.debug("base subsidy : " + base);
        double salary = Math.round(base * dc * (1 + (ac/50)) + topup);
        dcSummaryRepository.updateSalary(uid, ac, topup, salary);
        log.debug(salary + "");
    }

    /**
     * 更新指定用户指定月份的topup
     * @param uid
     * @param yearmonth
     * @param topup
     */
    public void updateTopup(int uid, int yearmonth, double topup) {
        DcSummary dcSummary = dcSummaryRepository.getDcSummary(uid, yearmonth);
        if (dcSummary == null) {
            dcSummary = new DcSummary(uid, yearmonth);
        }
        dcSummary.setTotal(topup);
        dcSummaryRepository.save(dcSummary);
    }


    /**
     * 拿到用户所有 AC 日志
     * @param uid
     * @return
     */
    public List<Map<String, Object>> listUserAc(int uid) {
        return acRecordRepository.listUserAc(uid);
    }


    /**
     * 实验室所有人的 DC 汇总（指定月份）
     * @param date
     * @return
     */
    public List<Map<String, Object>> listDcSummaryVO(LocalDate date) {
        int yearmonth = date.getYear() * 100 + date.getMonthValue();
        return dcSummaryRepository.listDcSummary(yearmonth);
    }


    /**
     * 实验室所有人的 AC 汇总
     * @return
     */
    public List<Map<String, Object>> listAcSummary() {
        return acRecordRepository.listAcSummary();
    }


    /**
     * 拿到最近10条 AC 变动
     * @return
     */
    public List<Map<String, Object>> listLastAc() {
        return acRecordRepository.listLastAc();
    }


    /**
     * 用首页显示的绩效信息
     * @param uid
     * @return
     */
    public Map getUserPerformance(int uid) {
        LocalDate date  = LocalDate.now();
        int yearmonth = date.getYear() * 100 + date.getMonthValue();
        double acTotal = acRecordRepository.getUserAcSum(uid);
        DcSummary dc = dcSummaryRepository.findByUserIdAndYearmonth(uid, yearmonth);
        if (dc == null) {
            return Map.of("acTotal", acTotal, "dcTotal", 0, "w1", 0, "w2",0, "w3", 0, "w4", 0, "w5", 0);
        } else {
            return Map.of("acTotal", acTotal, "dcTotal", dc.getTotal(), "w1", dc.getWeek1(), "w2", dc.getWeek2(), "w3", dc.getWeek3(), "w4", dc.getWeek4(), "w5",dc.getWeek5());
        }
    }


}
