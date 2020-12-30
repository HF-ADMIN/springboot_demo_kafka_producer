package com.example.demo2.repository;

import com.example.demo2.dao.EmployeeDAO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface EmployeeRepository extends JpaRepository<EmployeeDAO, Long> {
    @Query(nativeQuery = true, value = "select * from employee where id = ?1")
    EmployeeDAO findById(Integer id);

    // @Query(nativeQuery = true, value = "update employee set name = ?2, position = ?3, sign_up_date = to_date(?4, 'YYYY-MM-DD') where employee_number = ?1")
    // EmployeeDAO updateEmp(Integer employee_number, String name, String postion, String sign_up_date);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value= "update employee set name = :#{#emp.name}, position = :#{#emp.position}, sign_up_date = :#{#emp.sign_up_date} where employee_number = :#{#emp.employee_number}")
    Integer update(@Param("emp") EmployeeDAO emp);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value= "delete from employee where employee_number = :#{#emp.employee_number}")
    Integer deleteEmp(@Param("emp") EmployeeDAO emp);
}
