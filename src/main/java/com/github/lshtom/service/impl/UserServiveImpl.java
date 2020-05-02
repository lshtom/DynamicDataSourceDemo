package com.github.lshtom.service.impl;

import com.github.lshtom.dao.mapper.UserDAO;
import com.github.lshtom.dao.model.User;
import com.github.lshtom.service.UserServive;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    private AtomicInteger testCount = new AtomicInteger(0);
    @Override
    @Transactional
    public List<User> doSomething(User user) {
        Date curDate = new Date();
        // 写操作1
        user.setCreateDate(curDate);
        userDAO.insert(user);

        // 写操作2
        User user2 = new User();
        BeanUtils.copyProperties(user, user2);
        user2.setCreateDate(curDate);
        user2.setName(user.getName()+"test2");
        userDAO.insert(user2);

        // 模拟发生异常
        if (testCount.incrementAndGet() % 3 == 0) {
            throw new RuntimeException("模拟事务中发生异常");
        }

        // 读操作
        return userDAO.query();
    }
}
