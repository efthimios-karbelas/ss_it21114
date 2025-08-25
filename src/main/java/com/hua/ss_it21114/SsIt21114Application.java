package com.hua.ss_it21114;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SsIt21114Application {

    public static void main(String[] args) {
        SpringApplication.run(SsIt21114Application.class, args);
    }

    @RestController
    @RequestMapping("/api/test")
    public static class TestController {

        @GetMapping
        public String hello() {
            return "Hello world!";
        }
    }
}
