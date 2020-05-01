package com.github.lshtom.web;

import com.github.lshtom.dao.model.User;
import com.github.lshtom.service.UserServive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lshtom
 * @version 1.0.0
 * @description
 * @date 2020/4/28
 */
@RestController
@RequestMapping("demo2")
public class DemoController2 {

    @Autowired
    private UserServive userServive;

    @GetMapping("user/insert")
    public int insert(User user) {
        return userServive.insert(user);
    }

    @GetMapping("user/query")
    public List<User> query() {
        return userServive.query();
    }

    @GetMapping("user/{id}")
    public User findById(@PathVariable Long id) {
        return userServive.findById(id);
    }
}
