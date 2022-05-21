package com.softeng.dingtalk.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class DingTalkScheduleVO {
    Integer id;
    int organizerId;
    List<Integer> attendeesIdList;
    String summary;
    LocalDateTime start;
    LocalDateTime end;
    boolean online;
    String location;

    public DingTalkScheduleVO(int organizerId, String summary, LocalDateTime start, LocalDateTime end, boolean online, String location) {
        this.organizerId = organizerId;
        this.summary = summary;
        this.start = start;
        this.end = end;
        this.online = online;
        this.location = location;
    }

}
