package com.softeng.dingtalk.repository;

import com.softeng.dingtalk.entity.Iteration;
import com.softeng.dingtalk.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @date 3/14/2020
 */
public interface IterationRepository extends JpaRepository<Iteration, Integer> {

    /**
     * 查询项目的所有迭代
     * @param pid
     * @return
     */
    @EntityGraph(value="iteration.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query("select it from Iteration it where it.project.id = :pid order by it.id desc")
    List<Iteration> listIterationByPid(@Param("pid") int pid);

    /**
     * 根据id查询当前迭代的连续交付次数
     * @param id
     * @return
     */
    @Query("select i.conSuccess from Iteration i where i.id = :id")
    Integer getConSucessCntById(@Param("id") int id);

    /**
     * 根据id查询迭代
     * @param id
     * @return
     */
    @EntityGraph(value="iteration.graph",type= EntityGraph.EntityGraphType.FETCH)
    @Query("select i from Iteration i where i.id = :id")
    Iteration getIterationById(@Param("id") int id);


    // 用户获取自己参与的迭代
    @Query("select p from Iteration p left join IterationDetail pd on p.id = pd.iteration.id where pd.user.id = :uid")
    List<Iteration> listunfinishIteration(@Param("uid")int uid);

//    // 审核人获取进行中的迭代
//    @EntityGraph(value="iteration.graph",type= EntityGraph.EntityGraphType.FETCH)
//    @Query("select i from Iteration i where i.auditor.id = :aid and i.status = false")
//    List<Iteration> listUnfinishIterationByAid(@Param("aid") int aid);
////
//
//    // 审核人获取结束的项目
//    @EntityGraph(value="iteration.graph",type= EntityGraph.EntityGraphType.FETCH)
//    @Query("select i from Iteration i where i.auditor.id = :aid and i.status = true")
//    List<Iteration> listfinishIterationByAid(@Param("aid") int aid);
//
//



//
//    // 根据 pid 集合查询 iteration
//    @EntityGraph(value="iteration.graph",type= EntityGraph.EntityGraphType.FETCH)
//    @Query("select p from Iteration p where p.id in :pids order by p.id desc")
//    List<Iteration> findAllById(@Param("pids") List<Integer> ids);
//

}
