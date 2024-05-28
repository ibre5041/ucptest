package com.oracle.springapp.health;

import oracle.ucp.UniversalConnectionPool;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class CustomHealthIndicator implements HealthIndicator {

    private boolean isHealthy = false;
    UniversalConnectionPool pool;

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
    public Health health() {
        try {
            UniversalConnectionPoolManager manager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
            manager.setJmxEnabled(true);
            String[] pools = manager.getConnectionPoolNames();
            if (pools.length == 0) {
                return Health.down().build();
            }
            pool = manager.getConnectionPool(pools[0]);
            Integer max = pool.getMaxPoolSize();
            Integer bor = pool.getBorrowedConnectionsCount();
            if(bor+1 >= max) {
                return Health.down().build();
            } else {
                return Health.up().build();
            }
        } catch (UniversalConnectionPoolException ex) {
            throw new RuntimeException(ex);
        }
        //return isHealthy ? Health.up().build() : Health.down().build();
    }
}
