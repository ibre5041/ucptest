package com.oracle.springapp.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;
import org.springframework.stereotype.Component;

@Component
class MyRetryListener extends MethodInvocationRetryListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(MyRetryListener.class);

    @Override
    protected <T, E extends Throwable> boolean doOpen(RetryContext context,
                                                      MethodInvocationRetryCallback<T, E> callback) {

        log.info("Invocation of method: " + callback.getInvocation().getMethod().toGenericString()
                + " with label: " + callback.getLabel());
        return super.doOpen(context, callback);
    }

}