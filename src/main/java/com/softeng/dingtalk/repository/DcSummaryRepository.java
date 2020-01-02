package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.DcSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @date 12/29/2019
 */
@Repository
public interface DcSummaryRepository extends CustomizedRepository<DcSummary, Integer>, JpaRepository<DcSummary, Integer> {

    /**
     * 获取指定用户指定日期的dc汇总值
     * @param uid, yearmonth, week
     * @return java.util.List<java.lang.Integer>
     * @Date 10:58 AM 12/30/2019
     **/
    @Query(value = "select  d from DcSummary d where d.user.id = :uid and d.yearmonth = :yearmonth")
    DcSummary getDcSummary(@Param("uid") int uid, @Param("yearmonth") int yearmonth);


}
