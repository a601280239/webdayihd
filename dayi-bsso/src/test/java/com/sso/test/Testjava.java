package com.sso.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

/**
 * @ClassName Testjava
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/15
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class Testjava {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Test
    public void test01(){

        Set<Object> number = redisTemplate.opsForHash().keys("number");
        for (Object o : number) {
            redisTemplate.opsForHash().delete("number",o.toString());
        break;
        }



    }
}