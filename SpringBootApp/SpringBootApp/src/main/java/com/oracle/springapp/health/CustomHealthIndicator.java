package com.oracle.springapp.health;

import oracle.ucp.UniversalConnectionPool;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import reactor.core.publisher.Mono;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@ConditionalOnEnabledHealthIndicator("custom")
public class CustomHealthIndicator implements ReactiveHealthIndicator {

    private boolean isHealthy = false;
    UniversalConnectionPool pool;

    @Autowired
    private DataSource dataSource;

    public CustomHealthIndicator() {

        try {
            UniversalConnectionPoolManager manager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
            manager.setJmxEnabled(true);

            MBeanServer myMBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> nameSet = myMBeanServer.queryNames(new ObjectName("oracle.ucp.admin.UniversalConnectionPoolMBean:name=UniversalConnectionPoolManager*"), null);
            Set<ObjectInstance> instSet = myMBeanServer.queryMBeans(new ObjectName("oracle.ucp.admin.UniversalConnectionPoolMBean:name=UniversalConnectionPoolManager*"), null);
//            ObjectInstance instance = (ObjectInstance) instSet.toArray()[0];
//            System.out.println("Class Name:t" + instance.getClassName());
//            System.out.println("Object Name:t" + instance.getObjectName());

        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        } catch (UniversalConnectionPoolException e) {
            throw new RuntimeException(e);
        }

        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        scheduled.schedule(() -> {
            isHealthy = true;
        }, 40, TimeUnit.SECONDS);
    }

    @Override
    public Mono<Health> health() {
        try {
            UniversalConnectionPoolManager manager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
            manager.setJmxEnabled(true);
            String[] pools = manager.getConnectionPoolNames();
            if (pools.length == 0) {
                return Mono.just(Health.down().build());
            }
            pool = manager.getConnectionPool(pools[0]);
            Integer max = pool.getMaxPoolSize();
            Integer bor = pool.getBorrowedConnectionsCount();
            if(bor+1 <= max) {
                return Mono.just(Health
                        .outOfService()
                        .withDetail("maxPoolSize", max)
                        .withDetail("borrowedConnections", bor)
                        .build());
            } else {
                return Mono.just(Health.up()
                        .withDetail("maxPoolSize", max)
                        .withDetail("borrowedConnections", bor)
                        .build());
            }
        } catch (UniversalConnectionPoolException ex) {
            throw new RuntimeException(ex);
        }
        //return isHealthy ? Health.up().build() : Health.down().build();
    }
}
