package com.oracle.springapp.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.retry.support.RetryTemplate;

import com.oracle.springapp.model.Employee;
import com.oracle.springapp.service.EmployeeService;
import com.oracle.springapp.dao.EmployeeDAO;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	EmployeeDAO employeeDao;

	@Autowired
	private RetryTemplate retryTemplate;

	/**
	 * Displays the Employees from the EMP table
	 *
	 * @return
	 */
	@Override
	@Retryable(/*interceptor = "retryInterceptor",*/ retryFor = {ConcurrencyFailureException.class}
	)
	public List<Employee> displayEmployees(){
		List<Employee> employees = employeeDao.getAllEmployees();
		return employees;
	}

	/**
	 * Displays the Employees from the EMP table
	 *
	 * @return
	 */
	@Override
	@Retryable(/*interceptor = "retryInterceptor",*/ retryFor = {ConcurrencyFailureException.class}
	)
	public Employee displayEmployee(Integer empno){
		Employee employee = employeeDao.getEmployee(empno);

//		System.out.println(String.format("%20s %20s %20s %20s %20s %20s %20s %20s \n",
//				employees.size(), "EMPNO", "ENAME", "JOB", "MGR", "HIREDATE", "SALARY", "COMM", "DEPT"));
//		System.out.println(employee);
		return employee;
	}

	@Override
	@Recover
	public Employee displayEmployeeFallback(ConcurrencyFailureException e, Integer empno) {
		return null;
	}

	@Override
	@Retryable(retryFor = {ConcurrencyFailureException.class})
	public void insertEmployee(Employee employee) {
		employeeDao.insertEmployee(employee);	
	}

}
