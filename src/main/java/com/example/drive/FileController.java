package com.example.drive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController {

    @GetMapping("/file")
    public String file(){
        return "Welcome to Spring boot world!";
    }
}
