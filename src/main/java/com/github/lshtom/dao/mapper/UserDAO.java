package com.github.lshtom.dao.mapper;

import com.github.lshtom.dao.model.User;

import java.util.List;

public interface UserDAO {

    List<User> query();

    User selectById(Long id);

    Integer insert(User user);
}
