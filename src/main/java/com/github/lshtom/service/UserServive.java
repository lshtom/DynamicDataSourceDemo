package com.github.lshtom.service;

import com.github.lshtom.dao.model.User;

import java.util.List;

public interface UserServive {

    int insert(User user);

    List<User> query();

    User findById(Long id);

    List<User> doSomething(User user);
}
