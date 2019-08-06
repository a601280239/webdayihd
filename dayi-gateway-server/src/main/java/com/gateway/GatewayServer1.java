package com.gateway;

import com.gateway.config.MyReslover;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName GatewayServer1
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@SpringBootApplication
@RestController
public class GatewayServer1 {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServer1.class,args);
    }
    @RequestMapping("serverhealth")
    public void serverhealth(){
        System.out.println("=========gateway server check health is ok! ^_^ ========");

    }
    @Bean(name="myAddrReslover")
    public MyReslover getMyReslover(){
        return new MyReslover();
    }
}