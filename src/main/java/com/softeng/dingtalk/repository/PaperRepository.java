package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.Paper;
import com.softeng.dingtalk.entity.Vote;
import com.softeng.dingtalk.po.PaperinfoPO;
import com.softeng.dingtalk.projection.PaperProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * @date 2/5/2020
 */
@Repository
public interface PaperRepository extends JpaRepository<Paper, Integer> {
    @Modifying
    @Query("update Paper p set p.result = :result where p.id = :id")
    void updatePaperResult(@Param("id") int id, @Param("result")int result);

    // paper select in 会默认升序排序
    @EntityGraph(value="paper.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query("select p from Paper p where p.id in :ids order by p.id desc")
    List<Paper> findAllById(@Param("ids") List <Integer> ids);

    // 分页获取id
    @Query("select p.id from Paper p")
    Page<Integer> listAllId(Pageable pageable);


    @Query("select p.title from Paper p where p.id = :id")
    String getPaperTitleById(@Param("id") int id);


    @Query("select p.title from Paper p where p.vote.id = :vid")
    String getPaperTitleByVid(@Param("vid") int vid);


    // todo delete
    @EntityGraph(value="paper.graph",type= EntityGraph.EntityGraphType.FETCH)
    List<PaperProjection> findPaperBy();

    @Modifying
    @Query(value = "update paper set vote_id = :vid where id = :id", nativeQuery = true)
    void updatePaperVote(@Param("id") int id, @Param("vid") int vid);

    @Query("select p.vote.id from Paper p where p.id = :id")
    Integer findVidById(@Param("id") int id);

    @Query("select p.vote from  Paper p where p.id = :id")
    Vote findVoteById(@Param("id") int id);






}
