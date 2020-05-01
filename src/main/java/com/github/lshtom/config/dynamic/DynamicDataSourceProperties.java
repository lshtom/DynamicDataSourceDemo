package com.github.lshtom.config.dynamic;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author lshtom
 * @version 1.0.0
 * @description 动态多数据源配置类
 * @date 2020/4/28
 */
@ConfigurationProperties("mydatasource.dynamic")
public class DynamicDataSourceProperties {

    /**各数据源配置*/
    private Map<String, DataSourceProperties> config;
    /**主数据源名称*/
    private String mainDataSourceName;

    public Map<String, DataSourceProperties> getConfig() {
        return config;
    }

    public void setConfig(Map<String, DataSourceProperties> config) {
        this.config = config;
    }

    public String getMainDataSourceName() {
        return mainDataSourceName;
    }

    public void setMainDataSourceName(String mainDataSourceName) {
        this.mainDataSourceName = mainDataSourceName;
    }

    public static class DataSourceProperties {
        private String dirverClass;
        private String url;
        private String username;
        private String password;

        public String getDirverClass() {
            return dirverClass;
        }

        public void setDirverClass(String dirverClass) {
            this.dirverClass = dirverClass;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
