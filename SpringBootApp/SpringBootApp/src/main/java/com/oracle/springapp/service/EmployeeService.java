package com.oracle.springapp.service;

import com.oracle.springapp.model.Employee;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

public interface EmployeeService {
	/**
	 * Display all employees.
	 *
	 * @return List<Employee>
	 */
//	@Retryable(
//			retryFor = {ConcurrencyFailureException.class},
//			maxAttempts = 6, // Retry up to x times
//			backoff = @Backoff(delay = 5000)) // Wait 5 second between retries
	public List<Employee> displayEmployees();

//	@Retryable(
//			retryFor = {ConcurrencyFailureException.class},
//			maxAttempts = 6, // Retry up to x times
//			backoff = @Backoff(delay = 5000)) // Wait 5 second between retries
	public Employee displayEmployee(Integer empno);

//	@Recover
	public Employee displayEmployeeFallback(ConcurrencyFailureException e, Integer empno);

	/**
	 * Create a new employee record
	 */
//	@Retryable(
//			retryFor = {ConcurrencyFailureException.class},
//			maxAttempts = 6, // Retry up to x times
//			backoff = @Backoff(delay = 5000)) // Wait 5 second between retries
	public void insertEmployee(Employee employee);


}
