package com.oracle.springapp;

import com.oracle.springapp.model.Employee;
import com.oracle.springapp.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.Date;
import java.util.Scanner;

/**
 * SpringBoot application main class. It uses JdbcTemplate class which
 * internally uses UCP for connection check-outs and check-ins.
 *
 */
@EnableRetry
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
public class OracleJdbcCmdApplication implements CommandLineRunner {

    @Autowired
    EmployeeService employeeService;
    
	public static void main(String[] args) {
		SpringApplication.run(OracleJdbcCmdApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("List of employees");
		// Displays the list of employees from EMP table
		employeeService.displayEmployees();
		// Insert a new employee into EMP table
		employeeService.insertEmployee(new Employee(7954,"TAYLOR","MANAGER",7839, Date.valueOf("2020-03-20"),5300,0,10));
		System.out.println("List of Employees after the update");
		// Displays the list of employees after a new employee record is inserted
		employeeService.displayEmployees();

		Scanner s = new Scanner(System.in);
		System.out.println("Press enter to continue.....");
		s.nextLine();

		while(true) {
			employeeService.displayEmployees();
		}
	}

}
