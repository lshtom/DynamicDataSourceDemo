package com.github.lshtom.config.rw.interceptor;

import com.github.lshtom.config.dynamic.DynamicDataSource;
import com.github.lshtom.config.rw.MasterSlaveConfigProperties;
import com.github.lshtom.config.rw.context.ReadWriteConnectionContext;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.ConnectionProxy;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 利用MyBatis插件实现读写分离
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class ReadWriteInterceptor implements Interceptor {

    @Autowired
    private MasterSlaveConfigProperties masterSlaveConfigProperties;

    // 用于记录当前的操作类型
    private static final ThreadLocal<String> CURRENT_OPT_TYPE = new ThreadLocal<>();
    public static final String READ_OPERATE = "READ";
    public static final String WRITE_OPERATE = "WRITE";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        Connection connection =  (Connection) invocation.getArgs()[0];
        MetaObject statementHandlerMetaObj = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement)statementHandlerMetaObj.getValue("delegate.mappedStatement");
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // 根据SQL的类型来选择数据源
        bindingConnection(sqlCommandType, connection);
        return invocation.proceed();
    }

    /**
     * 根据SQL的类型绑定相应的连接对象绑定到当前线程中
     * @description 涉及到了数据源路由和连接对象的获取及与当前线程绑定
     */
    private void bindingConnection(SqlCommandType sqlCommandType, Connection connection) throws SQLException {
        // 先判断当前StatementHandler的prepare方法的连接对象是否为代理连接，如果是，那就是我们所要处理的
        ConnectionProxy connToUse;
        if (connection instanceof ConnectionProxy) {
            connToUse = (ConnectionProxy) connection;
        } else if (Proxy.getInvocationHandler(connection) instanceof ConnectionLogger) {
            // 此处考虑到开启了MyBatis SQL日志输出时，Connection对象被再次代理了一层，
            // 故要拿到这层日志代理内的真实的Connection，也就是我们动态数据源所创建的Connection代理对象（实现了ConnectionProxy接口）。
            ConnectionLogger connLogger = (ConnectionLogger) Proxy.getInvocationHandler(connection);
            if (connLogger.getConnection() instanceof ConnectionProxy) {
                connToUse = (ConnectionProxy) connLogger.getConnection();
            } else {
                return;
            }
        }else {
            return;
        }

        // 根据SQL类型设置相应的数据源并获取相应的连接对象绑定到当前线程中
        switch (sqlCommandType) {
            // 查询读从库（对于多个从库需要进行负载均衡）
            case SELECT:
                // 保存当前的操作类型
                CURRENT_OPT_TYPE.set(READ_OPERATE);
                // 如果当前线程还没有绑定读连接则设定所使用的数据源，然后获取该连接并绑定到当前线程
                if (!ReadWriteConnectionContext.hasReadConnection()) {
                    // TODO：如果一个事务中是写->读，那么读操作也将走主库，是没有问题的，
                    // TODO：但是如果是读->写->读，那么后面的读操作将使用第一个读操作的连接对象，走的是从库，主从延迟是可能带来问题的，
                    // TODO：但也不能简单的在此处将与当前线程绑定的读连接切换成主库连接就完事了，因为事务结束后进行一系列恢复设置同样应该设置到之前的那个读连接中，
                    // TODO：这块逻辑后续要待完善。
                    if (ReadWriteConnectionContext.hasWriteConnection()) {
                        // 如果已经绑定了写连接，则表明之前有写操作，则读操作也使用该写连接
                        Connection targetReadConn = ReadWriteConnectionContext.getWriteConnection();
                        ReadWriteConnectionContext.bindReadConnection(targetReadConn);
                    } else {
                        DynamicDataSource.setSelectedDataSourceName(getReadDataSourceName());
                        Connection targetReadConn = connToUse.getTargetConnection();
                        ReadWriteConnectionContext.bindReadConnection(targetReadConn);
                    }
                }
                break;
            // 增删改写入主库
            case INSERT:
            case UPDATE:
            case DELETE:
                // 保存当前的操作类型
                CURRENT_OPT_TYPE.set(WRITE_OPERATE);
                // 如果当前线程还没有绑定写连接则设定所使用的数据源，然后获取该连接并绑定到当前线程
                if (!ReadWriteConnectionContext.hasWriteConnection()) {
                    DynamicDataSource.setSelectedDataSourceName(masterSlaveConfigProperties.getMasterDataSource());
                    Connection targetWriteConn = connToUse.getTargetConnection();
                    ReadWriteConnectionContext.bindWriteConnection(targetWriteConn);
                }
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
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 获取当前是读还是写操作
     */
    public static String getCurOperateType() {
        return CURRENT_OPT_TYPE.get();
    }

    /**
     * 移除当前线程所绑定的操作类型
     */
    public static void clearCurOperateType() {
        CURRENT_OPT_TYPE.remove();
    }
}
