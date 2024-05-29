package com.oracle.springapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import com.oracle.springapp.service.EmployeeService;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * SpringBoot application main class. It uses JdbcTemplate class which
 * internally uses UCP for connection check-outs and check-ins.
 *
 */
@EnableRetry
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
public class OracleJdbcApplication /*implements CommandLineRunner*/ {
	public static void main(String[] args) {
		SpringApplication.run(OracleJdbcApplication.class, args);
	}
}
