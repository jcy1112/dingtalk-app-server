package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.Bug;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @date 3/12/2020
 */
@Repository
public interface BugRepository extends JpaRepository<Bug, Integer> {
    @EntityGraph(value="bug.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query("select b from Bug b where b.project.id = :pid order by b.id desc")
    List<Bug> findAllByProjectId(@Param("pid") int pid);

    @EntityGraph(value="bug.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query("select b from Bug b where b.project.auditor.id = :aid order by b.id desc")
    List<Bug> listBugByAuditor(@Param("aid") int aid);

    @Modifying
    @Query("update Bug set status = :status where id = :id")
    void updateBugStatus(@Param("id") int id, @Param("status") boolean status);



}
