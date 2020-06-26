package com.softeng.dingtalk.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author zhanyeye
 * @description
 * @create 26/06/2020 4:48 PM
 */
@Data
public class PaperInfoVO {
    private int id;
    private String title;
    private String journal;
    private LocalDate issueDate;
    private List<AuthorVO> authors;
}
