package com.github.lshtom.config.rw.interceptor;

import com.github.lshtom.config.dynamic.DynamicDataSource;
import com.github.lshtom.config.rw.MasterSlaveConfigProperties;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 利用MyBatis插件实现读写分离验证
 */
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class}),
})
public class ReadWriteInterceptor implements Interceptor {

    @Autowired
    private MasterSlaveConfigProperties masterSlaveConfigProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // 不能通过对StatementHandler中的任何方法进行拦截来设定数据源，
        // 因为在Executor的相关方法中已经调用了getConnection方法,
        // 所以只能对Executor的相关方法进行拦截，并在该拦截处理逻辑中进行数据源的设定。
        try {
            bindingDataSource(sqlCommandType);
            return invocation.proceed();
        } finally {
            DynamicDataSource.clearDataSourceBinding();
        }
    }

    /**
     * 根据SQL的类型绑定相应的数据源
     */
    private void bindingDataSource(SqlCommandType sqlCommandType) {
        // 根据SQL类型设置相应的数据源
        switch (sqlCommandType) {
            // 查询读从库（对于多个从库需要进行负载均衡）
            case SELECT:
                DynamicDataSource.setSelectedDataSourceName(getReadDataSourceName());
                break;
            // 增删改写入主库
            case INSERT:
            case UPDATE:
            case DELETE:
                DynamicDataSource.setSelectedDataSourceName(masterSlaveConfigProperties.getMasterDataSource());
                break;
            default:
                break;
        }
    }

    /**
     * 选择读数据源（此处采用轮询的方式来进行负载均衡）
     */
    private AtomicInteger count = new AtomicInteger(0);
    private String getReadDataSourceName() {
        // TODO: 此处可以抽象出策略接口，使用策略模式来指定所要使用的负载均衡策略
        String[] slaveDataSourceNames = masterSlaveConfigProperties.getSlaveDataSource();
        int size = slaveDataSourceNames.length;
        return slaveDataSourceNames[count.incrementAndGet() % size];
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
