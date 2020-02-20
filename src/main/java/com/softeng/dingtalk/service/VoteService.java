package com.softeng.dingtalk.service;

import com.softeng.dingtalk.component.DingTalkUtils;
import com.softeng.dingtalk.entity.*;
import com.softeng.dingtalk.po.PaperinfoPO;
import com.softeng.dingtalk.repository.*;
import com.softeng.dingtalk.vo.VoteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import javax.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhanyeye
 * @description
 * @create 2/6/2020 5:30 PM
 */
@Service
@Transactional
@Slf4j
public class VoteService {
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    VoteDetailRepository voteDetailRepository;
    @Autowired
    PaperRepository paperRepository;
    @Autowired
    PaperDetailRepository paperDetailRepository;
    @Autowired
    DingTalkUtils dingTalkUtils;
    @Autowired
    AcRecordRepository acRecordRepository;


    // 创建投票并钉钉发送消息
    public void createVote(VoteVO voteVO) {
        Vote vote = new Vote(voteVO.getEndTime());
        voteRepository.save(vote);
        paperRepository.updatePaperVote(voteVO.getPaperid(), vote.getId());

        // 发送投票信息
        new Thread(()-> {
            String title = paperRepository.getPaperTitleById(voteVO.getPaperid());
            List<String> namelist = paperDetailRepository.listPaperAuthor(voteVO.getPaperid());
            dingTalkUtils.sendVoteMsg(voteVO.getPaperid(), title, vote.getEndTime().toString(), namelist);
        }).start();
    }

    // 用户投票
    public Map poll(int vid, int uid, VoteDetail voteDetail) {
        LocalDateTime now = LocalDateTime.now();
        log.debug("now" + now.toString());

        Vote vote = voteRepository.findById(vid).get();
        LocalDateTime ddl = LocalDateTime.of(vote.getStartTime(), vote.getEndTime()).plusMinutes(1);

        log.debug("ddl" + now.toString());

        if (now.isBefore(ddl)) {
            voteDetail.setUser(new User(uid)); // 标明这一票是谁投的
            voteDetailRepository.save(voteDetail);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "投票已经截止！");
        }

        // 返回当前的投票结果
        List<String> acceptlist =  voteDetailRepository.listAcceptNamelist(vid);
        List<String> rejectlist = voteDetailRepository.listRejectNamelist(vid);
        int accept = acceptlist.size();  // accept 票数
        int reject = rejectlist.size();  // reject 票数
        int total = accept + reject;     // 总投票数
        return Map.of("status", true,"accept", accept, "total", total, "reject", reject, "result", voteDetail.getResult(),"acceptnames",acceptlist,"rejectnames", rejectlist);
    }


    //投票还未结束,用户获取投票信息
    public Map  getVotingDetail(int vid, int uid){
        Boolean myresult = voteDetailRepository.getVoteDetail(vid, uid);
        if (myresult != null) { //用户已经投票，可以查看结果
            List<String> acceptlist =  voteDetailRepository.listAcceptNamelist(vid);
            List<String> rejectlist = voteDetailRepository.listRejectNamelist(vid);
            int accept = acceptlist.size();  // accept 票数
            int reject = rejectlist.size();  // reject 票数
            int total = accept + reject;     // 总投票数


            return Map.of("status", true,"accept", accept, "total", total, "reject", reject, "result", myresult, "acceptnames",acceptlist,"rejectnames", rejectlist);
        } else { //用户没有投票，不可以查看结果
            return Map.of("status", false);
        }
    }


    // 获取已经结束的投票信息
    public Map getVotedDetail(int vid, int uid) {
        List<String> acceptlist =  voteDetailRepository.listAcceptNamelist(vid);
        List<String> rejectlist = voteDetailRepository.listRejectNamelist(vid);
        int accept = acceptlist.size();  // accept 票数
        int reject = rejectlist.size();  // reject 票数
        int total = accept + reject;     // 总投票数
        Boolean myresult = voteDetailRepository.getVoteDetail(vid, uid); // 用户本人的投票情况：accept, reject, 未参与(null)
        if (myresult == null) {
            return Map.of("status", true,"accept", accept, "total", total, "reject", reject, "acceptnames",acceptlist,"rejectnames", rejectlist);
        } else {
            return Map.of("status", true,"accept", accept, "total", total, "reject", reject, "result", myresult, "acceptnames",acceptlist,"rejectnames", rejectlist);
        }

    }


    // 更新投票的最终结果，投票截止后调用
    public Vote updateVote(Vote v) {
        int accept = voteDetailRepository.getAcceptCnt(v.getId());
        int total = voteDetailRepository.getCnt(v.getId());
        voteRepository.updateStatus(v.getId(), accept, total, accept > total - accept);
        v.setAccept(accept);
        v.setTotal(total);
        v.setResult(accept > total - accept);
        return v;
    }


    // 根据论文最终结果计算投票者的ac
    public void computeVoteAc(int pid, boolean result) {
        Vote vote = paperRepository.findVoteById(pid);
        if (vote != null) {
            List<VoteDetail> voteDetails = voteDetailRepository.listByVid(vote.getId());
            List<AcRecord> acRecords = new ArrayList<>();

            List<AcRecord> oldAcRecord = Optional.ofNullable(voteDetails).map(List::stream).orElseGet(Stream::empty)
                    .filter(x -> x.getAcRecord() != null).map(x -> x.getAcRecord()).collect(Collectors.toList());
            acRecordRepository.deleteAll(oldAcRecord); // 删除旧的 acRecord

            for (VoteDetail vd : voteDetails) {
                AcRecord acRecord;
                if (vd.getResult() == result) {
                    acRecord = new AcRecord(vd.getUser(), 1, "投票预测成功");
                } else {
                    acRecord = new AcRecord(vd.getUser(), -1, "投票预测失败");
                }
                vd.setAcRecord(acRecord);
                acRecords.add(acRecord);
            }
            acRecordRepository.saveAll(acRecords);
            voteDetailRepository.saveAll(voteDetails);
        }
    }



}
