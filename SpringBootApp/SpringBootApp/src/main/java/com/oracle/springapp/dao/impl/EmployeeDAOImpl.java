package com.oracle.springapp.dao.impl;

import java.sql.SQLRecoverableException;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.oracle.springapp.dao.EmployeeDAO;
import com.oracle.springapp.model.Employee;
import org.springframework.stereotype.Service;

/**
 * Simple Java class which uses Spring's JdbcDaoSupport class to implement
 * business logic.
 *
 */
@Service
@Repository
public class EmployeeDAOImpl extends JdbcDaoSupport
		implements EmployeeDAO
//		, InitializingBean
//		, DisposableBean
	{
	private static final Logger log = LoggerFactory.getLogger(EmployeeDAOImpl.class);

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void initialize() {
		DataSource d = this.getDataSource();
		setDataSource(dataSource);
		String name = dataSource.getClass().getCanonicalName();
		oracle.ucp.jdbc.PoolDataSourceImpl ucp = (PoolDataSourceImpl)dataSource;
		log.info("Datasource used: {}", dataSource);
	}

	@Override
	public List<Employee> getAllEmployees() {
		final String sql = "SELECT empno, ename, job, mgr, hiredate, sal, comm, SLOW_NUMBER1(deptno) as deptno FROM emp";
		return getJdbcTemplate().query(sql, 
				(rs, rowNum) -> new Employee(rs.getInt("empno"),
						rs.getString("ename"),
						rs.getString("job"),
						rs.getInt("mgr"),
						rs.getDate("hiredate"),
						rs.getInt("sal"),
						rs.getInt("comm"),
						rs.getInt("deptno")
						));
	}

	@Override
	public Employee getEmployee(Integer empno) {
		final String sql =
		"""
		SELECT empno, ename, job, mgr, hiredate, sal, comm, deptno
		FROM emp
		where empno = SLOW_NUMBER1(?)
		""";
		SqlParameterSource parameters = new MapSqlParameterSource("empno", empno);
		List<Employee> employees = getJdbcTemplate().query(sql,
				(rs, rowNum) -> new Employee(rs.getInt("empno"),
						rs.getString("ename"),
						rs.getString("job"),
						rs.getInt("mgr"),
						rs.getDate("hiredate"),
						rs.getInt("sal"),
						rs.getInt("comm"),
						rs.getInt("deptno")
				), empno/*, parameters*/);
		if(!employees.isEmpty())
			return employees.get(0);
		else
			return null;
	}

	@Override
	public void insertEmployee(Employee employee) {
		String sql = "Select max(empno) from emp"; 
	    int[] emp_no = new int[1];
	    //getJdbcTemplate().query(sql, (rs, rowNum) -> emp_no[rowNum] = rs.getInt(1));
	    getJdbcTemplate().query(sql, (resultSet, rowNumber) -> emp_no[rowNumber] = resultSet.getInt(1));	    
	    System.out.println("Max emploee number is: " + emp_no[0]);
	    
	    sql = "INSERT INTO EMP VALUES(?,?,?,?,?,?,?,?)";
	    int newEmpNo = emp_no[0]+1;
		getJdbcTemplate().update(sql, new Object[]{
				newEmpNo, 
				employee.getName()+(newEmpNo),
				employee.getJob(),
				employee.getManager(),
				employee.getHiredate(),
				employee.getSalary(),
				employee.getCommission(),
				employee.getDeptno()
		});
	}
}
