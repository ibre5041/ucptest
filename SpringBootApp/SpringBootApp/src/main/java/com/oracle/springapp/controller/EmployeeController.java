package com.oracle.springapp.controller;

import com.oracle.springapp.model.Employee;
import com.oracle.springapp.service.EmployeeService;
import oracle.jdbc.OracleDatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/all")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employee = employeeService.displayEmployees();
            return ResponseEntity.ok(employee);
        } catch (BadSqlGrammarException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (DataAccessResourceFailureException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (TransientDataAccessResourceException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//        } catch (ConcurrencyFailureException e) {
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//        } catch (RecoverableDataAccessException e) {
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (UncategorizedSQLException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (ScriptException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (QueryTimeoutException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getAllEmployee(@PathVariable Integer id) {
        try {
            Employee employee = employeeService.displayEmployee(id);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

}
