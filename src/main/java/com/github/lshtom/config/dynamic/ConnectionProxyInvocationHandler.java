package com.github.lshtom.config.dynamic;

import com.github.lshtom.config.rw.context.ReadWriteConnectionContext;
import com.github.lshtom.config.rw.interceptor.ReadWriteInterceptor;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @author lshtom
 * @version 1.0.0
 * @description Connection代理实现逻辑
 * @date 2020/5/1
 */
public class ConnectionProxyInvocationHandler extends AbstractConnectionProxyInvocationHandler {

    public ConnectionProxyInvocationHandler(DynamicDataSource dataSource) {
        super(dataSource);
    }

    public ConnectionProxyInvocationHandler(DynamicDataSource dataSource, String username, String password) {
        super(dataSource, username, password);
    }

    @Override
    protected void doSetReadOnly(boolean readOnly) throws SQLException {
        if (ReadWriteConnectionContext.hasWriteConnection()) {
            ReadWriteConnectionContext.getWriteConnection().setReadOnly(readOnly);
        }
        if (ReadWriteConnectionContext.hasReadConnection()) {
            ReadWriteConnectionContext.getReadConnection().setReadOnly(readOnly);
        }
    }

    @Override
    protected void doSetTransactionIsolation(int isolationLevel) throws SQLException {
        if (ReadWriteConnectionContext.hasWriteConnection()) {
            ReadWriteConnectionContext.getWriteConnection().setTransactionIsolation(isolationLevel);
        }
        if (ReadWriteConnectionContext.hasReadConnection()) {
            ReadWriteConnectionContext.getReadConnection().setTransactionIsolation(isolationLevel);
        }
    }

    @Override
    protected void doSetAutoCommit(boolean autoCommit) throws SQLException {
        if (ReadWriteConnectionContext.hasWriteConnection()) {
            ReadWriteConnectionContext.getWriteConnection().setAutoCommit(autoCommit);
        }
        if (ReadWriteConnectionContext.hasReadConnection()) {
            ReadWriteConnectionContext.getReadConnection().setAutoCommit(autoCommit);
        }
    }

    @Override
    protected void doCommit() throws SQLException {
        // 只处理写连接（读连接时自动提交，无需在此处进行手动提交）
        if (ReadWriteConnectionContext.hasWriteConnection()) {
            ReadWriteConnectionContext.getWriteConnection().commit();
        }
    }

    @Override
    protected void doRollback(Object[] args) throws SQLException {
        // 只处理写连接（读连接为自动提交，无需手动处理）
        if (ReadWriteConnectionContext.hasWriteConnection()) {
            Connection writeConnection = ReadWriteConnectionContext.getWriteConnection();
            if (args != null && args[0] instanceof Savepoint) {
                writeConnection.rollback((Savepoint) args[0]);
            }
            writeConnection.rollback();
        }
    }

    @Override
    protected void doClose() throws SQLException {
        // 获取目标读写连接对象进行关闭操作
        if (ReadWriteConnectionContext.hasWriteConnection()) {
            ReadWriteConnectionContext.getWriteConnection().close();
        }
        if (ReadWriteConnectionContext.hasReadConnection()) {
            ReadWriteConnectionContext.getReadConnection().close();
        }

        // 解除与当前线程绑定的相关参数设置
        ReadWriteConnectionContext.clearBindings();
        ReadWriteInterceptor.clearCurOperateType();
    }

    @Override
    protected Object doMethodInvoke(Method method, Object[] args) throws Throwable {
        Connection targetConnection = null;
        if (ReadWriteInterceptor.READ_OPERATE.equals(ReadWriteInterceptor.getCurOperateType())
            && ReadWriteConnectionContext.hasReadConnection()) {
            // 当前的SQL操作为读操作且当前线程已经绑定了读连接，则获取该连接对象
            targetConnection = ReadWriteConnectionContext.getReadConnection();
        } else if (ReadWriteInterceptor.WRITE_OPERATE.equals(ReadWriteInterceptor.getCurOperateType())
            && ReadWriteConnectionContext.hasWriteConnection()) {
            // 当前的SQL操作为写操作且当前线程已经绑定了写连接，则获取该连接对象
            targetConnection = ReadWriteConnectionContext.getWriteConnection();
        }

        if (targetConnection != null) {
            return method.invoke(targetConnection, args);
        }

        return null;
    }
}
