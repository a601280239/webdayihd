package com.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName SsoServer
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@SpringBootApplication
@EnableJpaAuditing
@RestController
@EntityScan(basePackages = {"com.hqf.pojo.**"})
public class SsoServer {
    public static void main(String[] args) {
        SpringApplication.run(SsoServer.class,args);
    }
    @RequestMapping("health")
    public void  health(){
        System.out.println("==========SSO-SERVER  ok!==========");

}
}