package com.sso.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Cursor<Map.Entry<Object, Object>> number = redisTemplate.opsForHash().scan("number", ScanOptions.NONE);
        List<String> key1=new ArrayList<>();
        List<String> value1=new ArrayList<>();
        while(number.hasNext()){
            Map.Entry<Object, Object> entry = number.next();
                    key1.add(entry.getKey().toString());
                    value1.add(entry.getValue().toString());
        }
        System.out.println(key1);
        System.out.println(value1);


    }
}