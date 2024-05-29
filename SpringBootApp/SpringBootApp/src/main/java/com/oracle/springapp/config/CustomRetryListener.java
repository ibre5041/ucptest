package com.oracle.springapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.interceptor.MethodInvocationRetryCallback;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class CustomRetryListener extends MethodInvocationRetryListenerSupport {
    private static final Logger logger = LoggerFactory.getLogger(CustomRetryListener.class);

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        logger.info("Starting retry operation for: {}", context.getAttribute(RetryContext.NAME));
        return true;
    }

    @Override
    protected <T, E extends Throwable> boolean doOpen(RetryContext context,
                                                      MethodInvocationRetryCallback<T, E> callback) {

        logger.info("Invocation of method: " + callback.getInvocation().getMethod().toGenericString()
                + " with label: " + callback.getLabel());
        return super.doOpen(context, callback);
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        logger.info("Retry operation completed for: {}", context.getAttribute(RetryContext.NAME));
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        logger.error("Error occurred during retry operation for: {}. Attempt: {}",
                context.getAttribute(RetryContext.NAME), context.getRetryCount());
    }
}
