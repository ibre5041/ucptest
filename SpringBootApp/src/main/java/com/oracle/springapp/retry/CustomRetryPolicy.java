package com.oracle.springapp.retry;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;

public class CustomRetryPolicy implements RetryPolicy {

    private int maxAttempts;

    public CustomRetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean canRetry(RetryContext context) {
        int attempts = context.getRetryCount();
        return attempts < maxAttempts;
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {
        // Do nothing
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        RetryContextSupport retryContext = (RetryContextSupport) context;
        retryContext.registerThrowable(throwable);
    }

    @Override
    public String toString() {
        return "CustomRetryPolicy [maxAttempts=" + maxAttempts + "]";
    }
}
