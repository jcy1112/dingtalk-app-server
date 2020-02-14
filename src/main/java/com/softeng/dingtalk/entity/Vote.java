package com.softeng.dingtalk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.softeng.dingtalk.vo.VoteVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @create 2/5/2020 5:14 PM
 */

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private boolean result;
    private int acceptCount;
    private int amount;
    private boolean status;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false, insertable = false)
    private LocalTime startTime;
    private LocalTime endTime;

    @JsonIgnoreProperties("vote")
    @OneToMany(mappedBy = "vote")
    private List<VoteDetail> voteDetails;

    public Vote(LocalTime endTime) {
        this.endTime = endTime;
    }

}
