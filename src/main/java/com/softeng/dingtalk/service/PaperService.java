package com.softeng.dingtalk.service;

import com.softeng.dingtalk.entity.AcItem;
import com.softeng.dingtalk.entity.AcRecord;
import com.softeng.dingtalk.entity.Paper;
import com.softeng.dingtalk.entity.PaperDetail;
import com.softeng.dingtalk.repository.AcRecordRepository;
import com.softeng.dingtalk.repository.PaperDetailRepository;

import com.softeng.dingtalk.repository.PaperLevelRepository;
import com.softeng.dingtalk.repository.PaperRepository;
import com.softeng.dingtalk.vo.PaperVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @create 2/5/2020 9:15 PM
 */
@Service
@Transactional
@Slf4j
public class PaperService {
    @Autowired
    PaperRepository paperRepository;
    @Autowired
    PaperDetailRepository paperDetailRepository;
    @Autowired
    PaperLevelRepository paperLevelRepository;
    @Autowired
    AcRecordRepository acRecordRepository;

    // 添加论文记录
    public void addPaper(PaperVO papervo) {
        Paper paper = new Paper(papervo);
        paperRepository.save(paper);
        for (PaperDetail pd : papervo.getPaperDetails()) {
            pd.setPaper(paper);
        }
        paperDetailRepository.saveAll(papervo.getPaperDetails());
    }

    // 更新论文记录
    public void updatePaper(PaperVO paperVO) {
        Paper paper = paperRepository.findById(paperVO.getId()).get();
        paper.update(paperVO);
        paperRepository.save(paper); //更新
        paperDetailRepository.deleteByPaper(paper);
        for (PaperDetail pd : paperVO.getPaperDetails()) {
            pd.setPaper(paper);
        }
        paperDetailRepository.saveAll(paperVO.getPaperDetails());
    }

    // 删除论文
    public void deletePaper(int id) {
        paperDetailRepository.deleteByPaperid(id);
        paperRepository.deleteById(id);
    }


    public void  updateResult(int id, int result) {
        paperRepository.updatePaperResult(id, result); //更新指定 论文的结果
        Paper paper = paperRepository.findById(id).get();

        double sum = paperLevelRepository.getvalue(paper.getLevel()); //获取论文奖励总AC
        String reason = paper.getTitle();
        if (result == Paper.REJECT) { //如果被拒绝则扣分
            sum *= -0.5;
            reason += " Reject";
        } else {
            reason += " Accept";
        }

        List<PaperDetail> paperDetails = paperDetailRepository.findByPaper(new Paper(id)); //获取论文参与者



        double[] rate = new double[]{0.5, 0.25, 0.15, 0.1};
        int i = 0;
        for (PaperDetail pd : paperDetails) {
            if (i == 4) break;
            if (pd.getAcRecord() != null) {
                acRecordRepository.delete(pd.getAcRecord());
            }
            double ac = sum * rate[pd.getNum() - 1];
            AcRecord acRecord = new AcRecord(pd.getUser(), null, ac, reason);
            pd.setAc(ac);
            pd.setAcRecord(acRecord);
            acRecordRepository.save(acRecord);
            i++;
        }
        paperDetailRepository.saveAll(paperDetails);


        //todo 调用投票计算
    }


    public List<Paper> listPaper() {
        return paperRepository.listPaperlist();
    }




}