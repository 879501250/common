package com.qq.utils.Redis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class RedisUtilTest {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

        RedisUtil redisUtil = (RedisUtil) context.getBean("redisUtil");

        //=====================testString======================
        redisUtil.set("name", "how2java");
        System.out.println(redisUtil.get("name"));
        redisUtil.del("name");
        System.out.println(redisUtil.get("name"));

        //=====================testNumber======================
        long incr = redisUtil.incr("number", 1);
        System.out.println(incr);
        incr = redisUtil.incr("number", 1);
        System.out.println(incr);

        //=====================testMap======================
        Map<String, Object> map = new HashMap<>();
        map.put("name", "meepo");
        map.put("pwd", "password");
        redisUtil.hmset("user", map);
        System.out.println(redisUtil.hget("user", "name"));
    }
}
