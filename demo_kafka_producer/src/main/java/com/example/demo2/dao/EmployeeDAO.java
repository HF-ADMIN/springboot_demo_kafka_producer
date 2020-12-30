package com.example.demo2.dao;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="employee")
public class EmployeeDAO {  
    @Id
    @Column(name="id", nullable=false)
    @GeneratedValue
    private Integer id;

    @Column(name="name", nullable=false, length=20)
    private String name;

    @Column(name="employee_number", nullable=false)
    private Integer employee_number;

    @Column(name="sign_up_date", nullable=false)
    private Date sign_up_date;

    @Column(name="position", nullable=false, length=20)
    private String position;
}
