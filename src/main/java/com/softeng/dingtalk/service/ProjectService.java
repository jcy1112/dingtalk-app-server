package com.softeng.dingtalk.service;

import com.softeng.dingtalk.entity.AcRecord;
import com.softeng.dingtalk.entity.Project;
import com.softeng.dingtalk.entity.ProjectDetail;
import com.softeng.dingtalk.entity.User;
import com.softeng.dingtalk.repository.AcRecordRepository;
import com.softeng.dingtalk.repository.DcRecordRepository;
import com.softeng.dingtalk.repository.ProjectDetailRepository;
import com.softeng.dingtalk.repository.ProjectRepository;
import com.softeng.dingtalk.vo.ProjectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhanyeye
 * @description
 * @create 2/25/2020 2:08 PM
 */
@Service
@Transactional
@Slf4j
public class ProjectService {
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectDetailRepository projectDetailRepository;
    @Autowired
    UserService userService;
    @Autowired
    DcRecordRepository dcRecordRepository;
    @Autowired
    AcRecordRepository acRecordRepository;

    // 添加任务
    public void addProject(ProjectVO projectVO) {
        Project project = new Project(projectVO.getName(), new User(projectVO.getAuditorid()), projectVO.getDates()[0], projectVO.getDates()[1]);
        LocalDate[] dates = projectVO.getDates();
        int day = (int) dates[0].until(dates[1], ChronoUnit.DAYS);
        project.setExpectedAC(day * projectVO.getDingIds().size() / 30.0);

        projectRepository.save(project);

        List<String> userids = projectVO.getDingIds(); // 获取分配者的userid;
        List<ProjectDetail> projectDetails = new ArrayList<>();
        for (String u : userids) {
            int uid = userService.getIdByUserid(u);
            ProjectDetail pd = new ProjectDetail(project, new User(uid));
            projectDetails.add(pd);
        }
        projectDetailRepository.saveAll(projectDetails);
    }

    // 更新项目信息
    public void updateProject(ProjectVO projectVO) {
        Project p = projectRepository.findById(projectVO.getId()).get();
        LocalDate[] dates = projectVO.getDates();
        p.setName(projectVO.getName());
        p.setBeginTime(dates[0]);
        p.setEndTime(dates[1]);
        int day = (int) dates[0].until(dates[1], ChronoUnit.DAYS);
        p.setExpectedAC(day * projectVO.getDingIds().size() / 30.0);

        if (projectVO.isUpdateDingIds()) {
            projectDetailRepository.deleteByProjectId(projectVO.getId());  // 删除旧的分配信息
            List<String> userids = projectVO.getDingIds(); // 获取分配者的userid;
            List<ProjectDetail> projectDetails = new ArrayList<>();
            Project project = new Project(projectVO.getId());
            for (String u : userids) {
                int uid = userService.getIdByUserid(u);
                ProjectDetail pd = new ProjectDetail(project, new User(uid));
                projectDetails.add(pd);
            }
            projectDetailRepository.saveAll(projectDetails);
        }

    }

    // 统计时间段周日的次数
    public int countSunday(LocalDate btime, LocalDate ftime) {
        int day = (int) btime.until(ftime,ChronoUnit.DAYS) + 1;
        day += (btime.getDayOfWeek().getValue()-1); // 前补

        if (ftime.getDayOfWeek().getValue() == 7) { // 后砍
            return (day - ftime.getDayOfWeek().getValue()) / 7 + 1;
        } else {
            return (day - ftime.getDayOfWeek().getValue()) / 7;
        }
    }

    /**
     * 计算ac值
     * 实际ac计算公式: 𝐴_𝑖=𝐴_𝑎∗𝐷_𝑖/(∑𝐷)∗𝐷_𝑖/0.5
     * 𝐴_𝑖 denotes individual actual reward
     * 𝐴_𝑎 denotes team acutal reward
     * 𝐷_𝑖  denotes individual average DC during the iteration
     **/
    public void autoSetProjectAc(Project project) {
        List<ProjectDetail> projectDetails = projectDetailRepository.findAllByProject(project);
        //int day = Period.between(project.getBeginTime(), project.getFinishTime()).getDays();
        int day = (int) project.getBeginTime().until(project.getFinishTime(), ChronoUnit.DAYS);
        double actualAc = day * projectDetails.size() / 30; // 总ac值 = 实际时间 * 参与人数 / 30
        double totalDc = 0;
        double[] dcList = new double[projectDetails.size()]; // 记录各参与者开发周期内的dc值
        int index = 0;
        for (ProjectDetail pd : projectDetails) {
            double dc = dcRecordRepository.getByTime(pd.getUser().getId(), project.getAuditor().getId(), project.getBeginTime(), project.getFinishTime());
            dcList[index++] = dc;
            log.debug(dc + "");
            totalDc += dc;
        }
        log.debug("totaldc:" + totalDc );

        if (totalDc == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "项目参与者的总dc值为0，可能是参与者未提交dc申请，无法计算，需人工决定");
        }

        index = 0;
        int week = countSunday(project.getBeginTime(), project.getFinishTime());
        for (ProjectDetail pd : projectDetails) {
            // 计算实际AC
            double ac = actualAc * dcList[index] / totalDc * dcList[index] / week * 2;
            index++;
            log.debug("个人实际ac: " + ac);
            AcRecord acRecord = new AcRecord(pd.getUser(), project.getAuditor(), ac, project.getName());
            // 实例化ac记录
            acRecordRepository.save(acRecord);
            pd.setAcRecord(acRecord);
            projectDetailRepository.save(pd);
        }
    }

    public void manualSetProjectAc() {

    }


    public Map ComputeProjectAc(int pid, LocalDate finishTime) {
        Project p =  projectRepository.findById(pid).get();
        List<ProjectDetail> projectDetails = projectDetailRepository.findAllByProject(p);
        int day = (int) p.getBeginTime().until(finishTime, ChronoUnit.DAYS);
        double actualAc = day * projectDetails.size() / 30; // 总ac值 = 实际时间 * 参与人数 / 30
        double totalDc = 0;
        double[] dcList = new double[projectDetails.size()]; // 记录各参与者开发周期内的dc值
        int index = 0;
        for (ProjectDetail pd : projectDetails) {
            double dc = dcRecordRepository.getByTime(pd.getUser().getId(), p.getAuditor().getId(), p.getBeginTime(), finishTime);
            dcList[index++] = dc;
            log.debug(dc + "");
            totalDc += dc;
        }

        if (totalDc == 0) {
            return Map.of("valid", false);
        }

        index = 0;
        int week = countSunday(p.getBeginTime(), finishTime);

        List<Map<String, Object>> res = new ArrayList<>();

        for (ProjectDetail pd : projectDetails) {
            // 计算实际AC
            double ac = actualAc * dcList[index] / totalDc * dcList[index] / week * 2;
            ac = (double) (Math.round(ac * 100)/100.0);

            res.add(Map.of("name", pd.getUser().getName(), "ac", ac));
            index++;
        }

        return Map.of("valid", true, "res", res, "actualAc", actualAc, "week", week);
    }


    // 查询项目期间的dc值
    public Object getProjectDc(int pid) {
        Project p =  projectRepository.findById(pid).get();
        List<Map<String, String>> dclist = projectDetailRepository.getProjectDc(pid, p.getAuditor().getId(), p.getBeginTime(), p.getEndTime());
        Map<String, List<Map<String, String>>> maplist = dclist.stream()
                .collect(Collectors.groupingBy(map -> map.get("name"),
                        Collectors.mapping(map -> {
                            Map<String, String> temp = new HashMap<String, String>(map);
                            temp.remove("name");
                            return temp;
                        }, Collectors.toList())));

        List<Map<String, Object>> res = new ArrayList<>();

        List<User> users = projectDetailRepository.findUserByProjectId(pid);

        for (User u : users) {
            double dctotal = dcRecordRepository.getByTime(u.getId(), p.getAuditor().getId(), p.getBeginTime(), p.getEndTime());
            if (maplist.containsKey(u.getName())) {
                res.add(Map.of("name", u.getName(), "values", maplist.get(u.getName()), "dctotal", dctotal));
            } else {
                res.add(Map.of("name", u.getName(), "values", new ArrayList(), "dctotal", dctotal));
            }
        }

        return res;
    }


    // 审核人查询进行中的项目
    public List<Project> listUnfinishProjectByAuditor(int aid) {
        return projectRepository.listUnfinishProjectByAid(aid);
    }


    // 审核人已经结束的进行中的项目
    public List<Project> listfinishProjectByAuditor(int aid) {
        return projectRepository.listfinishProjectByAid(aid);
    }


    // 用户获取正在进行的项目
    public List<Project>  listProjectByUid(int uid) {
        return projectDetailRepository.listProjectByUid(uid);
    }


    // 删除项目
    public void delete(int id) {
        projectRepository.deleteById(id);
    }



}
