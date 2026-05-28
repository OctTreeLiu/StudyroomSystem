package com.studyspace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 逐光自习室管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.studyspace.mapper")
@EnableScheduling
public class StudyRoomApplication {


    public static void main(java.lang.String[] args) {
        SpringApplication.run(StudyRoomApplication.class, args);
        java.lang.System.out.println("========================================");
        java.lang.System.out.println("逐光自习室管理系统启动成功！");
        java.lang.System.out.println("访问地址: http://localhost:8080/api");
        java.lang.System.out.println("========================================");
    }
}

