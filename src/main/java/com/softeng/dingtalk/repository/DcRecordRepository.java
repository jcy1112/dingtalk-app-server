package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.DcRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

/**
 * @author zhanyeye
 * @description  操作 DcRecord 实体类的接口
 * @date 12/12/2019
 */
@Repository
public interface DcRecordRepository extends CustomizedRepository<DcRecord, Integer>, JpaRepository<DcRecord, Integer> {

    /**
     * 查询是否存在某条记录，
     * @param uid, aid, yearmonth, week
     * @return java.lang.Integer
     * @Date 7:47 PM 12/30/2019
     **/
    @Query(value =
            "SELECT IfNULL((SELECT id FROM dc_record WHERE applicant_id = :uid and auditor_id = :aid and timeflag = :timeflag LIMIT 1), 0)",
            nativeQuery = true)
    Integer isExist(@Param("uid") int uid,@Param("aid") int aid, @Param("timeflag") int timeflag);

    /**
     * 用于分页显示申请历史 ->  根据uid(用户)，获取用户提交的申请，实现分页
     * @param uid 申请人ID
     * @param pageable
     * @return java.util.List<com.softeng.dingtalk.entity.DcRecord>
     * @Date 4:28 PM 12/30/2019
     **/
    @Query("select d from DcRecord d where d.applicant.id = :uid")
    List<DcRecord> listByUid(@Param("uid") int uid, Pageable pageable);

    /**
     * 审核人查看待审核的申请  ->  根据uid(审核人)，获得待审核的申请
     * @param uid  审核人id
     * @return java.util.List<com.softeng.dingtalk.entity.DcRecord>
     * @Date 4:36 PM 12/30/2019
     **/
    @Query("select d from DcRecord d where d.auditor.id = :uid and d.ischeck = false")
    List<DcRecord> listPendingReview(@Param("uid") int uid);

    /**
     * 更新申请状态为已审核
     * @param id  申请id
     * @return void
     * @Date 4:40 PM 12/30/2019
     **/
    @Modifying
    @Query("update DcRecord d set d.ischeck = true where d.id = :id")
    void updateCheckStatus(@Param("id") int id);

    /**
     * 计算指定用户，指定周的，在各组的dc之和
     * @param uid 用户ID
     * @param timeflag 所在年月周
     * @return java.lang.Integer
     * @Date 9:48 PM 12/29/2019
     **/
    @Query(value = "select sum(dc) from dc_record where user_id = :uid and timeflag = :timeflag", nativeQuery = true)
    Double getUserWeekTotalDc(@Param("uid") int uid, @Param("timeflag") int timeflag);
}
