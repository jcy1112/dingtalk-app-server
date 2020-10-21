package com.softeng.dingtalk.component;

import com.softeng.dingtalk.entity.InternalVote;
import com.softeng.dingtalk.repository.PaperRepository;
import com.softeng.dingtalk.repository.InternalVoteRepository;
import com.softeng.dingtalk.service.InitService;
import com.softeng.dingtalk.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author zhanyeye
 * @description 定时器
 * @create 2/9/2020 12:30 PM
 */

@Slf4j
@Component
@Transactional
public class Timer {

    @Autowired
    InternalVoteRepository internalVoteRepository;
    @Autowired
    VoteService voteService;
    @Autowired
    PaperRepository paperRepository;
    @Autowired
    DingTalkUtils dingTalkUtils;
    @Autowired
    InitService initService;


    @Scheduled(cron = "0 * * * * ?")
    public void checkVote() {
        //拿到没有结束的投票
        List<InternalVote> internalVotes = voteService.listUnderwayInternalVote();

        if (internalVotes.size() != 0) {
            LocalDateTime now = LocalDateTime.now();
            log.debug("定时器执行：" + now.toString());
            for (InternalVote v : internalVotes) {
                if (v.getDeadline().isBefore(now)) {
                    //更新
                    v = voteService.updateInternalVote(v);
                    log.debug("钉钉发送消息");
                    Map map = paperRepository.getPaperInfo(v.getId());
                    if (map.size() != 0) {
                        int pid = (int)map.get("id");
                        String title = map.get("title").toString();
                        dingTalkUtils.sendVoteResult(pid, title, v.getResult(), v.getAccept(), v.getTotal());
                    }
                }
            }
        }
    }


    @Scheduled(cron = "0 0 3 1 * ?")
    public void initMonthlyDcSummary() {
        initService.initDcSummary();
    }


}
