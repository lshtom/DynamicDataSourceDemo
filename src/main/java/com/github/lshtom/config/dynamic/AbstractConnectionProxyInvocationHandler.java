package com.github.lshtom.config.dynamic;

import com.github.lshtom.config.rw.interceptor.ReadWriteInterceptor;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author lshtom
 * @version 1.0.0
 * @description Connection代理抽象实现逻辑
 * @date 2020/5/2
 */
public abstract class AbstractConnectionProxyInvocationHandler implements InvocationHandler {

    private DynamicDataSource dataSource;
    private String username;
    private String password;

    /**
     * Connection最为基本的参数设置
     */
    private boolean readOnly;
    private boolean autoCommit;
    private int isolationLevel;

    // 连接关闭状态标志
    private boolean closed;

    private static final boolean DEFAULT_READ_ONLY = false;
    private static final boolean DEFAULT_AUTO_COMMIT = true;
    private static final int DEFAULT_ISOLATION_LEVEL = TransactionIsolationLevel.READ_COMMITTED.getLevel();
    private static final boolean DEFAULT_CLOSED = false;

    protected AbstractConnectionProxyInvocationHandler(DynamicDataSource dataSource) {
        this.dataSource = dataSource;
        initConnectionDefaultVal();
    }

    protected AbstractConnectionProxyInvocationHandler(DynamicDataSource dataSource, String username, String password) {
        this.dataSource = dataSource;
        this.username = username;
        this.password = password;
        initConnectionDefaultVal();
    }

    /**
     * Connection核心参数初始化
     */
    private void initConnectionDefaultVal() {
        this.readOnly = DEFAULT_READ_ONLY;
        this.isolationLevel = DEFAULT_ISOLATION_LEVEL;
        this.autoCommit = DEFAULT_AUTO_COMMIT;
        this.closed = DEFAULT_CLOSED;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // 根据所调用的目标方法进行处理
        switch (methodName) {
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            case "unwrap":
                if (((Class<?>) args[0]).isInstance(proxy)) {
                    return proxy;
                }
                break;
            case "isWrapperFor":
                if (((Class<?>) args[0]).isInstance(proxy)) {
                    return true;
                }
                break;
            case "toString":
                return this.toString();
            case "getTargetConnection":
                return getTargetConnection();
            case "setTransactionIsolation":
                if (args != null) {
                    doSetTransactionIsolation((Integer) args[0]);
                    isolationLevel = (Integer) args[0];
                }
                break;
            case "setAutoCommit":
                if (args != null) {
                    doSetAutoCommit((Boolean) args[0]);
                    this.autoCommit = (Boolean) args[0];
                }
                break;
            case "getAutoCommit":
                return this.autoCommit;
            case "setReadOnly":
                if (args != null) {
                    doSetReadOnly((Boolean) args[0]);
                    this.readOnly = (Boolean) args[0];
                }
                break;
            case "isReadOnly":
                return this.readOnly;
            case "isClosed":
                return closed;
            case "commit":
                doCommit();
                break;
            case "rollback":
                doRollback(args);
                break;
            case "close":
                doClose();
                closed = true;
                break;
            default:
                // 对于Connection的其他目标方法的调用，直接获取相应的目标数据源的Connection对象进行调用
                return doMethodInvoke(method, args);
        }

        return null;
    }

    /**
     * 执行readOnly设置操作
     */
    protected abstract void doSetReadOnly(boolean readOnly) throws SQLException;

    /**
     * 执行transactionIsolation设置操作
     */
    protected abstract void doSetTransactionIsolation(int isolationLevel) throws SQLException;

    /**
     * 执行AutoCommit设置操作
     */
    protected abstract void doSetAutoCommit(boolean autoCommit) throws SQLException;

    /**
     * 进行提交操作
     */
    protected abstract void doCommit() throws SQLException;

    /**
     * 进行回滚操作
     */
    protected abstract void doRollback(Object[] args) throws SQLException;

    /**
     * 进行关闭操作
     */
    protected abstract void doClose() throws SQLException;

    /**
     * 利用目标Connection进行实际方法调用
     */
    protected abstract Object doMethodInvoke(Method method, Object[] args) throws Throwable;

    /**
     * 获取目标数据源的Connection对象并进行相关参数设置
     * @description 注意：此处的逻辑只负责获取目标数据源的目标Connection对象，不负责与当前线程进行绑定；绑定操作是在读写分离插件中做的
     */
    private Connection getTargetConnection() throws SQLException {
        // 从目标数据源中获取目标连接对象
        Connection targetConnection;
        if (StringUtils.hasText(username)) {
            targetConnection = dataSource.getTargetConnection(username, password);
        } else {
            targetConnection = dataSource.getTargetConnection();
        }

        // 设置Connection相关参数
        if (ReadWriteInterceptor.READ_OPERATE.equals(ReadWriteInterceptor.getCurOperateType())) {
            // 读操作还是自动提交，不由事务管理器控制
            targetConnection.setAutoCommit(true);
        } else {
            // 设置写连接的AutoCommit参数
            targetConnection.setAutoCommit(autoCommit);
        }
        targetConnection.setTransactionIsolation(isolationLevel);
        targetConnection.setReadOnly(readOnly);

        // 返回
        return targetConnection;
    }
}
