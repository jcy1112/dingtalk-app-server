package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.Paper;
import com.softeng.dingtalk.entity.InternalVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author zhanyeye
 * @description
 * @date 2/5/2020
 */
@Repository
public interface PaperRepository extends CustomizedRepository<Paper, Integer> {

    /**
     * paper select in 会默认升序排序
     * @param ids
     * @return
     */
    @EntityGraph(value="paper.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query("select p from Paper p where p.id in :ids order by p.id desc")
    List<Paper> findAllById(@Param("ids") List <Integer> ids);


    /**
     * 分页获取id
     * @param pageable
     * @return
     */
    @Query("select p.id from Paper p")
    Page<Integer> listAllId(Pageable pageable);


    /**
     * 根据id 获取论文title
     * @param id
     * @return
     */
    @Query("select p.title from Paper p where p.id = :id")
    String getPaperTitleById(@Param("id") int id);


    @Query(value = "select p.id, p.title from paper p where p.vote_id = :vid", nativeQuery = true)
    Map<String, Object> getPaperInfo(@Param("vid") int vid);


    @EntityGraph(value="paper.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query(value = "select * from paper", nativeQuery = true)
    List<Map<String, Object>> test();


    @Modifying
    @Query(value = "update paper set vote_id = :vid where id = :id", nativeQuery = true)
    void updatePaperVote(@Param("id") int id, @Param("vid") int vid);


    @Query("select p.vote.id from Paper p where p.id = :id")
    Integer findVidById(@Param("id") int id);


    @Query("select p.vote from  Paper p where p.id = :id")
    InternalVote findVoteById(@Param("id") int id);


    @Modifying
    @Query("update Paper set result = :result where id = :id")
    void updatePaperResult(int id, int result);




}
