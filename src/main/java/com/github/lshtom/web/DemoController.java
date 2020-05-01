package com.github.lshtom.web;

import com.github.lshtom.dao.mapper.TArchivesInfoDAO;
import com.github.lshtom.dao.model.TArchivesInfo;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author lshtom
 * @version 1.0.0
 * @description
 * @date 2020/4/19
 */
@RestController
@RequestMapping("demo")
public class DemoController {

    @Autowired
    private TArchivesInfoDAO tArchivesInfoDAO;

    @GetMapping("test")
    public String test() {
        return "hello world";
    }

    @GetMapping("query")
    public List<TArchivesInfo> query() {
        return tArchivesInfoDAO.queryAll(null);
    }

    @GetMapping("pageQuery")
    public List<TArchivesInfo> pageQuery(String archivesCode, int pageIx, int pageSize) {
        TArchivesInfo param = new TArchivesInfo();
        param.setArchivesCode(archivesCode);
        RowBounds rowBounds = new RowBounds((pageIx - 1) * pageSize, pageSize);
        return tArchivesInfoDAO.pageQuery(param, rowBounds);
    }

    @GetMapping("query/{id}")
    public TArchivesInfo findById(@PathVariable Long id) {
        return tArchivesInfoDAO.selectById(id);
    }

    @GetMapping("insert")
    public int insert() {
        TArchivesInfo tArchivesInfo = new TArchivesInfo();
        tArchivesInfo.setArchivesCode(UUID.randomUUID().toString());
        tArchivesInfo.setUniqueCode(UUID.randomUUID().toString());
        tArchivesInfo.setSourceSystem("测试系统拉");
        tArchivesInfo.setBusinessGroup("H1");
        tArchivesInfo.setCreationDate(new Date());
        return tArchivesInfoDAO.insert(tArchivesInfo);
    }
}
