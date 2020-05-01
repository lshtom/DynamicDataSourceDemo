package com.github.lshtom.config.dynamic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.sql.DataSource;

/**
 * @author lshtom
 * @version 1.0.0
 * @description 动态多数据源配置类
 * @date 2020/4/28
 */
@Configuration
@ConditionalOnProperty(prefix = "mydatasource.dynamic", name = "enable", havingValue = "true")
@EnableAspectJAutoProxy
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceConfigure {

    @Autowired
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    @Bean
    public DataSource DynamicDataSource(DynamicDataSourceProperties dynamicDataSourceProperties) {
        return new DynamicDataSource(dynamicDataSourceProperties);
    }

    @Bean
    public DynamicDataSourceConfigAspect dynamicDataSourceConfigAspect() {
        return new DynamicDataSourceConfigAspect();
    }
}
