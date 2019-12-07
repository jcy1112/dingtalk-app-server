package com.softeng.dingtalk;

import com.softeng.dingtalk.repository.impl.CustomizedRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = CustomizedRepositoryImpl.class)
public class DingtalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(DingtalkApplication.class, args);
    }

}
