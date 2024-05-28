package com.oracle.springapp.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oracle.springapp.model.Employee;
import com.oracle.springapp.service.EmployeeService;
import com.oracle.springapp.dao.EmployeeDAO;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	EmployeeDAO employeeDao;

	/**
	 * Displays the Employees from the EMP table
	 *
	 * @return
	 */
	@Override
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
	public Employee displayEmployee(Integer empno){
		Employee employee = employeeDao.getEmployee();

//		System.out.println(String.format("%20s %20s %20s %20s %20s %20s %20s %20s \n",
//				employees.size(), "EMPNO", "ENAME", "JOB", "MGR", "HIREDATE", "SALARY", "COMM", "DEPT"));
//		System.out.println(employee);
		return employee;
	}

	@Override
	public void insertEmployee(Employee employee) {
		employeeDao.insertEmployee(employee);	
	}
	
	
}
