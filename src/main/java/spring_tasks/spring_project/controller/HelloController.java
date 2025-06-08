package spring_tasks.spring_project.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String entry(){
        return "Welcome to SpringBoot API tasks";
    }
    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }

}
