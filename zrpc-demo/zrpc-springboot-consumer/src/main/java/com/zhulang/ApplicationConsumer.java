package com.zhulang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Nozomi
 * @Date 2024/4/23 10:21
 */
@SpringBootApplication
@RestController
public class ApplicationConsumer {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumer.class,args);
    }

    @GetMapping("test")
    public String Hello(){
        return "hello consumer";
    }

}
