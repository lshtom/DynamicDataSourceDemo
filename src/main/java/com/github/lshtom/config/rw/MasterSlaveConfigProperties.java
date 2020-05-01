package com.github.lshtom.config.rw;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lshtom
 * @version 1.0.0
 * @description 主库从库配置
 * @date 2020/4/30
 */
@ConfigurationProperties("mydatasource.masterslave")
public class MasterSlaveConfigProperties {

    /**主库的数据源名称*/
    private String masterDataSource;
    /**从库的数据源名称*/
    private String[] slaveDataSource;

    public String getMasterDataSource() {
        return masterDataSource;
    }

    public void setMasterDataSource(String masterDataSource) {
        this.masterDataSource = masterDataSource;
    }

    public String[] getSlaveDataSource() {
        return slaveDataSource;
    }

    public void setSlaveDataSource(String[] slaveDataSource) {
        this.slaveDataSource = slaveDataSource;
    }
}
