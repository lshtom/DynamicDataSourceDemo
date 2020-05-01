package com.github.lshtom.service.impl;

import com.github.lshtom.dao.mapper.UserDAO;
import com.github.lshtom.dao.model.User;
import com.github.lshtom.service.UserServive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author lshtom
 * @version 1.0.0
 * @description
 * @date 2020/4/30
 */
@Service
public class UserServiveImpl implements UserServive {

    @Autowired
    private UserDAO userDAO;

    @Override
//    @DynamicDataSourceConfig("ds2")
//    @Transactional
    public int insert(User user) {
        user.setCreateDate(new Date());
        return userDAO.insert(user);
    }

    @Override
//    @DynamicDataSourceConfig("ds0")
    @Transactional
    public List<User> query() {
        return userDAO.query();
    }

    @Override
//    @DynamicDataSourceConfig("ds1")
    public User findById(Long id) {
        return userDAO.selectById(id);
    }
}
