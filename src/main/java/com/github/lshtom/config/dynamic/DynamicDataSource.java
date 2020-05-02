package com.github.lshtom.config.dynamic;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lshtom
 * @version 1.0.0
 * @description 动态多数据源
 * @date 2020/4/28
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSource.class);

    /***
     * 实现多数据源的本质其实是对外提供一个虚拟数据源，
     * 该虚拟数据源同样实现JDBC的DataSoruce接口，
     * 然后里面封装了各个不同的实际数据源
     */

    public DynamicDataSource(DynamicDataSourceProperties dynamicDataSourceProperties) {
        // 根据配置初始化数据源
        Map<Object, Object> dataSourceMap = initDataSourceMap(dynamicDataSourceProperties);

        // 设置数据源名称（Key）<->数据源实例映射
        super.setTargetDataSources(dataSourceMap);

        // 设置默认的数据源（也就是当没有指定所要使用的数据源时将使用该数据源）
        super.setDefaultTargetDataSource(dataSourceMap.get(dynamicDataSourceProperties.getMainDataSourceName()));
    }

    /**
     * 根据数据源配置信息初始化各数据源（使用HikariDataSource）
     */
    private Map<Object, Object> initDataSourceMap(DynamicDataSourceProperties dynamicDataSourceProperties) {
        Map<Object, Object> map = new HashMap<>();
        dynamicDataSourceProperties.getConfig().forEach((key, val) -> {
            map.put(key, buildDataSource(val));
        });
        return map;
    }

    /**
     * 根据数据源配置信息构建数据源对象实例
     */
    private DataSource buildDataSource(DynamicDataSourceProperties.DataSourceProperties dataSourceProperties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getDirverClass());
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        return dataSource;
    }

    private static final ThreadLocal<String> DATA_SOURCE_NAME_BINDING = new ThreadLocal<>();

    /**
     * 此处返回数据源映射的Key（也就是对应到前面设置到targetDataSources中的Map的Key）,
     * 一般就是所要使用的数据源的名称。
     *
     * 此方法是当getConnection被调用时回调的，而一般我们都是在Service方法上使用自定义注解来指定要使用哪个数据源的，
     * 而检测Service方法上的注解信息一般是通过一个切面来实现的，但是在这个切面中进行该注解属性读取和处理时还没有到getConnection方法的调用阶段，
     * 但是我们可以提前设置好要使用哪个数据源，等到了getConnection被调用时就可以根据前面设置好的要使用的数据源的名称来获取真正的数据源返回，
     * 这种前后有联系，且在同一个线程的，很自然的想到使用ThreadLocal来构建这种同一线程的纽带。
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String selectedDataSourceName = getSelectedDataSourceName();
        LOGGER.info("所选择的数据源为：{}", selectedDataSourceName);
        return selectedDataSourceName;
    }

    /**
     * 获取所选择的数据源名称
     */
    private static String getSelectedDataSourceName() {
        return DATA_SOURCE_NAME_BINDING.get();
    }

    /**
     * 设置所选择的数据源名称
     */
    public static void setSelectedDataSourceName(String dataSourceName) {
        DATA_SOURCE_NAME_BINDING.set(dataSourceName);
    }

    /**
     * 清除数据源绑定
     */
    public static void clearDataSourceBinding() {
        DATA_SOURCE_NAME_BINDING.remove();
    }

    /***
     * 返回Connection代理对象
     */
    @Override
    public Connection getConnection() throws SQLException {
        return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class<?>[] {ConnectionProxy.class},
            new ConnectionProxyInvocationHandler(this));
    }

    /***
     * 返回Connection代理对象
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class<?>[] {ConnectionProxy.class},
            new ConnectionProxyInvocationHandler(this, username, password));
    }

    /**
     * 获取目标数据源的连接对象（！非代理Connection）
     */
    public Connection getTargetConnection() throws SQLException {
        return super.getConnection();
    }

    /**
     * 获取目标数据源的连接对象（！非代理Connection）
     */
    public Connection getTargetConnection(String username,String password) throws SQLException {
        return super.getConnection(username, password);
    }
}
