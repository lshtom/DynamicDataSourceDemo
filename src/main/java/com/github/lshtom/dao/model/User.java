package com.github.lshtom.dao.model;

import java.util.Date;

/**
 * @author lshtom
 * @version 1.0.0
 * @description 用户实体
 * @date 2020/4/28
 */
public class User {

    private Integer id;
    private String name;
    private Integer age;
    private String sex;
    private Date createDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
