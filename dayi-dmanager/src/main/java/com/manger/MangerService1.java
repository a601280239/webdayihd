package com.manger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MangerService1
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@SpringBootApplication
@RestController
public class MangerService1 {
    public static void main(String[] args) {
        SpringApplication.run(MangerService1.class,args);
    }
    @RequestMapping("serverhealth")
    public void serverhealth(){
        System.out.println("=========MangerService1 check health is ok! ^_^ ========");

    }
}