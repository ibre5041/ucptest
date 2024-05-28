package com.oracle.springapp.dao;

import java.util.List;

import com.oracle.springapp.model.Employee;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Simple DAO interface for EMP table.
 *
 */
public interface EmployeeDAO {
	public List<Employee> getAllEmployees();
	public Employee getEmployee(Integer empno);

	void insertEmployee(Employee employee);
}
