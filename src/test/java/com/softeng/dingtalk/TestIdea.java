package com.softeng.dingtalk;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

/**
 * @author zhanyeye
 * @description
 * @create 12/29/2019 10:49 AM
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TestIdea {
    @Test
    public void test() {
        LocalDateTime localDateTime = LocalDateTime.of(2019,9,1,1,1);
        log.debug(localDateTime.toString());
        log.debug(localDateTime.toString().substring(0, 7));
    }
}
