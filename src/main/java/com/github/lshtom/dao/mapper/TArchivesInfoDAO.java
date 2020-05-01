package com.github.lshtom.dao.mapper;

import com.github.lshtom.dao.model.TArchivesInfo;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface TArchivesInfoDAO {

    List<TArchivesInfo> queryAll(TArchivesInfo param);


    List<TArchivesInfo> pageQuery(TArchivesInfo param, RowBounds page);

    TArchivesInfo selectById(Long id);

    int insert(TArchivesInfo tArchivesInfo);
}
