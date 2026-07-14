package com.ngleanhvu.practice.spring_framework_demo.core.proxy;

import com.ngleanhvu.practice.spring_framework_demo.core.annotation.Transactional;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import java.lang.reflect.Method;

public class CglibTransactionalProxyFactory {

    private static final TransactionManager TX_MANAGER = new TransactionManager();

    public static Object createProxy(Object target) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new TransactionalMethodInterceptor(target));
        return enhancer.create();
    }

    private record TransactionalMethodInterceptor(Object target) implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            boolean hasTransaction = method.isAnnotationPresent(Transactional.class);

            if (!hasTransaction) {
                return method.invoke(target, args);
            }

            TX_MANAGER.begin();
            try {
                Object result = method.invoke(target, args);
                TX_MANAGER.commit();
                return result;
            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                TX_MANAGER.rollback(cause);
                throw cause;
            }
        }
    }
}