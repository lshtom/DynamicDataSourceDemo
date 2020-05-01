package com.github.lshtom.config.rw;

import com.github.lshtom.config.rw.interceptor.ReadWriteInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 主从读写分离配置
 */
@Configuration
@ConditionalOnProperty(prefix = "mydatasource.masterslave", name = "enable", havingValue = "true")
@EnableConfigurationProperties(MasterSlaveConfigProperties.class)
public class MasterSlaveConfigure {

    @Bean
    public ReadWriteInterceptor readWriteInterceptor() {
        return new ReadWriteInterceptor();
    }
}
