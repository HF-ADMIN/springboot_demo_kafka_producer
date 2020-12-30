package com.example.demo2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.opentracing.Span;
import io.opentracing.Tracer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import com.example.demo2.dto.EmployeeDTO;
import com.example.demo2.service.EmployeeService;

/**
 * @className EmployeeController
 * @description 아래 예제는 Employee 정보를 관리하는 서비스 Controller입니다.
 *              GET Method와 POST Method를 가지고 있습니다.
 */
@CrossOrigin(origins="*")
@RestController
public class EmployeeController{
    Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService service;

    @Autowired
    private Tracer tracer;

    /**
     * @methodName getAllEmployeesInfo
     * @throws     Exception
     * @description GET Request를 받아서 전체 Employee의 정보를 조회하는 메소드
     */
    @RequestMapping(value="/AllEmployeeInfo", method=RequestMethod.GET)
    public ResponseEntity<EmployeeDTO.Response> getAllEmployeesInfo(@RequestHeader HttpHeaders requestHeader) throws Exception{
        EmployeeDTO.Response responseBody = null;
        try {
            responseBody = service.getAllEmployeeInfo(requestHeader);
        }catch(Exception e) {
            throw e;
        }
        return ResponseEntity.ok().body(responseBody);
    }

    /**
     * @methodName getEmployeesInfo
     * @param      String
     * @return     ResponseEntity
     * @throws     Exception
     * @description GET Request를 받아서 Employee의 정보를 조회하는 메소드
     */
    @RequestMapping(value="/EmployeeInfo", method=RequestMethod.GET)
    public ResponseEntity<EmployeeDTO.Response> getEmployeesInfo(@RequestHeader HttpHeaders requestHeader, @RequestParam Integer id) throws Exception{
        EmployeeDTO.Response  responseBody = null;
        Span parentSpan = null;
        Span spanPhase1 = null;
        try {
            parentSpan = tracer.scopeManager().activeSpan();
            spanPhase1 = tracer.buildSpan("spanPhase_1").asChildOf(parentSpan).start();
            spanPhase1.log("                                                SpanPhase1 log");
            responseBody = service.getEmployeeInfo(requestHeader, id);
        }catch(Exception e) {
            throw e;
        }finally {
            spanPhase1.finish();
        }
        return ResponseEntity.ok().body(responseBody);
    }

    /**
     * @methodName postEmployeesInfo
     * @throws     Exception
     * @description GET Request를 받아서 Employee의 정보를 조회하는 메소드
     */
    @RequestMapping(value="/EmployeeInfo", method=RequestMethod.POST)
    public ResponseEntity<EmployeeDTO.Response> postEmployeesInfo(@RequestHeader HttpHeaders requestHeader, @RequestBody EmployeeDTO.Request request) throws Exception{
        // cudFlag 값에 의해 분기처리
        EmployeeDTO.Response response = null;
        try {
            if("C".equals(request.getCudFlag()) || "U".equals(request.getCudFlag())) {
                response = service.mergeEmployeeInfo(requestHeader, request);
            } else if("D".equals(request.getCudFlag())) {
                response = service.deleteEmployeeInfo(requestHeader, request);
            }
        }catch(Exception e) {
            throw e;
        }
        return ResponseEntity.ok().body(response);
    }
}
