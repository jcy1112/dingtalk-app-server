package com.softeng.dingtalk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.softeng.dingtalk.vo.ApplingVO;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhanyeye
 * @description 周DC值记录（DC日志）
 * @date 12/5/2019
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@NamedEntityGraph(name="dcRecord.graph",attributeNodes={@NamedAttributeNode("acItems"), @NamedAttributeNode("auditor")})
public class DcRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition="DECIMAL(10,3)")
    private double dvalue;  // Dedication Value
    @Column(columnDefinition="DECIMAL(10,3)")
    private double cvalue;  // Contribution Value
    @Column(columnDefinition="DECIMAL(10,3)")
    private double dc;
    @Column(columnDefinition="DECIMAL(10,3)")
    private double ac;


    private boolean status;   // 是否被审核
    private int yearmonth;     // 表示申请所属 年、月
    private int week;          // 申请所属周
    private LocalDate weekdate; //所属周的一天
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false, insertable = false)
    private LocalDateTime insertTime;
    @JsonIgnore
    @Version
    private int version;
    @JsonIgnore
    private int dateCode;

    @ManyToOne(fetch = FetchType.LAZY) //设置many端对one端延时加载，仅需要其ID
    private User applicant;
    @ManyToOne(fetch = FetchType.LAZY)
    private User auditor;

    @JsonIgnoreProperties("dcRecord")
    @OneToMany(mappedBy = "dcRecord")
    private List<AcItem> acItems;



    public void update(double cvalue, double dc, double ac) {
        this.cvalue = cvalue;
        this.dc = dc;
        this.ac = ac;
        this.status = true;

    }

    public void reApply(int authorid, double dvalue, LocalDate weekdate, int yearmonth, int week, int dateCode) {
        this.auditor = new User(authorid);
        this.dvalue = dvalue;
        this.weekdate = weekdate;
        this.status = false;
        this.yearmonth = yearmonth;
        this.week = week;
        this.dateCode = dateCode;
    }




}
