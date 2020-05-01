package com.github.lshtom.config.dynamic;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;

/**
 * 动态数据源配置切面
 */
@Aspect
public class DynamicDataSourceConfigAspect implements Ordered {

    @Pointcut(value = "@annotation(com.github.lshtom.config.dynamic.DynamicDataSourceConfig)")
    public void ponitcut() {
    }

    /**
     * 这里使用环绕增强的原因是：确保目标方法执行完后清除那个保存当前所使用的数据源名的ThreadLocal值
     */
    @Around(value = "ponitcut()")
    public Object useDynamicDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        DynamicDataSourceConfig config = signature.getMethod().getAnnotation(DynamicDataSourceConfig.class);
        String selectedDataSourceName = config.value();
        try {
            // 设定所要使用的数据源
            DynamicDataSource.setSelectedDataSourceName(selectedDataSourceName);
            // 执行原目标方法
            return joinPoint.proceed();
        } finally {
            DynamicDataSource.clearDataSourceBinding();
        }
    }

    /**
     * 数据源的切换需要在Spring事务开启前完成，所以此切面执行的优先级要高于Spring事务注解AOP的优先级才行
     */
    private static final int ORDER = 0;

    @Override
    public int getOrder() {
        return ORDER;
    }
}
