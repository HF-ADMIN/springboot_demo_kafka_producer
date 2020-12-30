package com.example.demo2.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class EmployeeDTO {
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {
        private String cudFlag;
        private Integer id;
        private String employee_number;
        private Integer age;
        private String email;
        private String phoneNum;
        private String address;
        private String resultCode;
        private String memo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String cudFlag;
        private Integer id;
        private String name;
        private String employee_number;
        private Integer age;
        private String email;
        private String phoneNum;
        private String address;
        private String sign_up_date;
        private String position;
        private String memo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Integer id;
        private String name;
        private String employee_number; 
        private Integer age;
        private String email;
        private String phoneNum;
        private String address;
        private String sign_up_date;
        private String position;
        private String resultCode;
        private String memo;
        private List<EmployeeDTO.Response> list;
    }
}
