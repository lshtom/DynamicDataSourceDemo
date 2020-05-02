package com.github.lshtom.config.rw.context;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lshtom
 * @version 1.0.0
 * @description 读／写连接对象上下文
 * @date 2020/5/1
 */
public class ReadWriteConnectionContext {

    private static final ThreadLocal<Map<String, Connection>> CONNECTION_CONTEXT = new ThreadLocal<Map<String, Connection>>() {
        @Override
        protected Map<String, Connection> initialValue() {
            return new HashMap<>();
        }
    };
    private static final String READ_CONNECTION_KEY = "READ_CONNECTION";
    private static final String WRITE_CONNECTION_KEY = "WRITE_CONNECTION";

    /**
     * 当前线程中是否绑定了读连接
     */
    public static boolean hasReadConnection() {
        if (CONNECTION_CONTEXT.get().get(READ_CONNECTION_KEY) != null) {
            return true;
        }
        return false;
    }

    /**
     * 为当前线程绑定读连接
     */
    public static void bindReadConnection(Connection connection) {
        CONNECTION_CONTEXT.get().put(READ_CONNECTION_KEY, connection);
    }

    /**
     * 获取当前线程绑定的读连接
     */
    public static Connection getReadConnection() {
        return CONNECTION_CONTEXT.get().get(READ_CONNECTION_KEY);
    }

    /**
     * 当前线程中是否绑定了写连接
     */
    public static boolean hasWriteConnection() {
        if (CONNECTION_CONTEXT.get().get(WRITE_CONNECTION_KEY) != null) {
            return true;
        }
        return false;
    }

    /**
     * 为当前线程绑定写连接
     */
    public static void bindWriteConnection(Connection connection) {
        CONNECTION_CONTEXT.get().put(WRITE_CONNECTION_KEY, connection);
    }

    /**
     * 获取当前线程绑定的写连接
     */
    public static Connection getWriteConnection() {
        return CONNECTION_CONTEXT.get().get(WRITE_CONNECTION_KEY);
    }

    /**
     * 移除绑定到当前线程的读写连接
     */
    public static void clearBindings() {
        CONNECTION_CONTEXT.remove();
    }
}
