package com.example.demo2.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo2.dao.EmployeeDAO;
import com.example.demo2.dto.EmployeeDTO;
import com.example.demo2.repository.EmployeeRepository;
import com.example.demo2.util.ServiceUtil;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

/**
 * @className   EmployeeService
 * @mehtod      getAllEmployeeInfo, getEmployeeInfo, mergeEmployeeInfo, deleteEmployeeInfo
 *              parsingDTO, getFlaskService, postFlaskService
 * @description 아래 예제는 Employee 정보를 관리하는 서비스 Service 입니다.
 */
@Service
public class EmployeeService { 

    Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private RestTemplate restTemplate;

    @Autowired
     EmployeeRepository repository;

    @Autowired
    public EmployeeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    private Tracer tracer;

    @Autowired
	private Environment env;


    /**
     * @methodName  getAllEmployeeInfo
     * @return      EmployeeDTO.Response
     * @throws      Exception
     * @description 전체 Employeee 정보를 가져오는 Mehtod 입니다.
     *              employee table에서 가져온 정보와 Python Flask 서비스와 연동하여 가져온 employee_detail table의 정보를 종합한 전체 데이터 리턴.
     */
    public EmployeeDTO.Response getAllEmployeeInfo(HttpHeaders requestHeader) throws Exception{ 

        EmployeeDTO.Response response = new EmployeeDTO.Response();
        List<EmployeeDTO.Response> inner_list = new ArrayList<EmployeeDTO.Response>();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // employee table에서 데이터를 가져와서 for문을 돌려 employee데이터 response에 담는다.
            for(EmployeeDAO dao : repository.findAll()) {
                EmployeeDTO.Response inner_response = new EmployeeDTO.Response();
                inner_response.setId(dao.getId());
                inner_response.setName(dao.getName());
                inner_response.setEmployee_number(String.valueOf(dao.getEmployee_number()));
                if(dao.getSign_up_date() != null) inner_response.setSign_up_date(transFormat.format(dao.getSign_up_date()));
                inner_response.setPosition(dao.getPosition());
                inner_list.add(inner_response);
            }
            response.setList(inner_list);

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
            
        }
        
        return response;
    }


    /**
     * @methodName  getEmployeeInfo
     * @param       Integer id
     * @return      EmployeeDTO.Response
     * @throws      Exception
     * @description Employeee 정보를 가져오는 Mehtod 입니다.
     *              employee table에서 가져온 정보와 Python Flask 서비스와 연동하여 가져온 employee_detail table의 정보를 종합한 데이터 리턴.
     */
    public EmployeeDTO.Response getEmployeeInfo(HttpHeaders requestHeader, Integer id) throws Exception{

        EmployeeDTO.Response response = new EmployeeDTO.Response();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject jsonObject = null;
        Span parentSpan = null;
        Span spanPhase2 = null;
        String baseURL = null;

        // request에서 id를 가지고와서 employee table에서 데이터를 조회한다.
        try {
            parentSpan = tracer.scopeManager().activeSpan();
            spanPhase2 = tracer.buildSpan("spanPhase_2").asChildOf(parentSpan).start();
            spanPhase2.log("                                                spanPhase2 log");
            EmployeeDAO dao =  repository.findById(id);

            logger.info("=====================> [EmployeeService / getEmployeeInfo] dao : " + dao);

            // Remote Service Call(service.name = serviceGW -> Springboot Call / service.name = service01 -> Flask Call)
            if("serviceGW".equals(env.getProperty("spring.application.name"))) {
                logger.info("=====================> [EmployeeService / getEmployeeInfo] spring.application.name : serviceGW");
                baseURL = "http://" + ServiceUtil.SPRING01_URI + "/" + ServiceUtil.SPRING01_GET_SERVICE + "?id=" + id.toString();

                final HttpEntity<String> httpEntity = new HttpEntity<String>(requestHeader);

                //Remote Service Call
               response = getSpringService(baseURL, httpEntity);

            } else if("service01".equals(env.getProperty("spring.application.name"))) {
                logger.info("=====================> [EmployeeService / getEmployeeInfo] spring.application.name : service01");
                baseURL = "http://" + ServiceUtil.FLASK_URI + "/" + ServiceUtil.FLASK_GET_SERVICE + "?employee_number=" + dao.getEmployee_number();

                final HttpEntity<String> httpEntity = new HttpEntity<String>(requestHeader);

                //Remote Service Call
                jsonObject = getFlaskService(baseURL, httpEntity);

                // flaskResponse의 resultCode가 200일 때 
                if("200".equals(jsonObject.get("resultCode").toString())) {   
                    logger.info("================================================================== 200");
                    response.setId(dao.getId());
                    response.setName(dao.getName());
                    response.setEmployee_number(String.valueOf(dao.getEmployee_number()));
                    if(dao.getSign_up_date() != null) response.setSign_up_date(transFormat.format(dao.getSign_up_date()));
                    response.setPosition(dao.getPosition());
                    response.setAddress((String)jsonObject.get("address"));
                    response.setEmail((String)jsonObject.get("email"));
                    response.setAge(Integer.valueOf(jsonObject.get("age").toString()));
                    response.setPhoneNum((String)jsonObject.get("phoneNum"));
                    response.setMemo((String)jsonObject.get("memo"));
                } 
                // flaskResponse의 resultCode가 200일 때 
                else if("500".equals(jsonObject.get("resultCode"))) {
                    response.setResultCode("500");
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            spanPhase2.finish();
        }

        logger.info("=====================> [EmployeeService / getEmployeeInfo] response : " + response);
        return response;
    }


    /**
     * @methodName  insertEmployeeInfo
     * @param       EmployeeDTO.Request request
     * @return      EmployeeDTO.Response
     * @throws      Exception
     * @description Employees 정보를 Insert하는 Mehtod입니다.
     *              employee table과 employe_detail(flask) 에 데이터를 모두 Insert합니다.
     */
   public EmployeeDTO.Response mergeEmployeeInfo(HttpHeaders requestHeader, EmployeeDTO.Request request) throws Exception {

       // employee table insert
       // 단순 demo용이기 때문에 방어로직은 생략
       EmployeeDTO.Response response = new EmployeeDTO.Response();
       JSONObject jsonObject = null;
       
       try {
            // employee table에 merge
            EmployeeDAO dao = new EmployeeDAO();
            parsingDTO(request, dao);

            //cudFlag에 따라 Updte, Insert
            if("C".equals(request.getCudFlag())) {
                repository.save(dao);
            }else if("U".equals(request.getCudFlag())) {
                repository.update(dao);
            }
            
            logger.info("====================> [EmployeeService / insertEmployeeInfo] jsonObject");

            // Flask Post Call
            // HttpHeaders headers = new HttpHeaders();
            // headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> req_payload = new HashMap<>();
            req_payload.put("cudFlag", request.getCudFlag());
            req_payload.put("id", request.getId());
            req_payload.put("employee_number", request.getEmployee_number());
            req_payload.put("age", request.getAge());
            req_payload.put("email", request.getEmail());
            req_payload.put("phoneNum", request.getPhoneNum());
            req_payload.put("address", request.getAddress());
            req_payload.put("memo", request.getMemo());

            HttpEntity<?> requestEntity = new HttpEntity<>(req_payload, requestHeader);

            // detail을 arg로 넣어서 flask Method를 태운다.
            String baseURL = "http://" + ServiceUtil.FLASK_URI + "/" + ServiceUtil.FLASK_POST_SERVICE;
//            jsonObject = callPostService(baseURL, requestEntity);

//            if(jsonObject != null) response.setResultCode(String.valueOf(jsonObject.get("resultCode")));
           if(jsonObject != null) response.setResultCode("200");

       }catch(Exception e) {
            e.printStackTrace(); 
            throw e;
       }

       logger.info("============================================> response : " + response);
       return response;
   }


    /**
     * @methodName  deleteEmployeeInfo
     * @param       EmployeeDTO.Request request
     * @return      EmployeeDTO.Response
     * @throws      Exception
     * @description Employees 정보를 Delete하는 Mehtod입니다.
     *              employee table과 employe_detail(flask) 에 데이터를 모두 Delete합니다.
     */
    public EmployeeDTO.Response deleteEmployeeInfo(HttpHeaders requestHeader, EmployeeDTO.Request request) throws Exception{
        // employee table delete
        EmployeeDTO.Response response = new EmployeeDTO.Response();
        JSONObject jsonObject = null;
        try {
            // employee table에서 데이터를 삭제하는 로직
            EmployeeDAO dao = new EmployeeDAO();
            parsingDTO(request, dao);
            repository.deleteEmp(dao);

            // flask post call
            // HttpHeaders headers = new HttpHeaders();
            // headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> req_payload = new HashMap<>();
            req_payload.put("cudFlag", request.getCudFlag());
            req_payload.put("employee_number", request.getEmployee_number());

            HttpEntity<?> requestEntity = new HttpEntity<>(req_payload, requestHeader);
            
            // detail을 arg로 넣어서 flask Method를 태운다.
            String baseURL = "http://" + ServiceUtil.FLASK_URI + "/" + ServiceUtil.FLASK_POST_SERVICE;
            jsonObject = callPostService(baseURL, requestEntity);

            if(jsonObject != null) response.setResultCode(String.valueOf(jsonObject.get("resultCode")));

        }catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
        
        logger.info("============================================> response : " + response);
        return response;
    }


    /**
     * @methodName  parsingDTO
     * @param       Object from, Object to
     * @throws      Exception
     * @description DTO를 parsing하는 Method입니다.
     */
    private void parsingDTO(Object from, Object to) {

        if(from instanceof EmployeeDTO.Request) {
            EmployeeDTO.Request request = (EmployeeDTO.Request) from;

            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
            if(to instanceof EmployeeDAO) {
                EmployeeDAO temp = (EmployeeDAO) to;
                try {
                    //temp = new EmployeeDAO(request.getId(), request.getName(), request.getEmployee_number(), transFormat.parse(request.getSign_up_date()), request.getPosition());
                    temp.setId(request.getId());
                    temp.setName(request.getName());
                    temp.setEmployee_number(Integer.valueOf(request.getEmployee_number()));
                    if(request.getSign_up_date() != null) temp.setSign_up_date(transFormat.parse(request.getSign_up_date()));
                    temp.setPosition(request.getPosition());
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } else if(from instanceof EmployeeDTO.Detail) {
            EmployeeDTO.Detail detail = (EmployeeDTO.Detail) from;
            if(to instanceof EmployeeDTO.Request) {
                EmployeeDTO.Request req = (EmployeeDTO.Request) to;
                req.setId(detail.getId());
                req.setEmployee_number(detail.getEmployee_number());
                req.setAge(detail.getAge());
                req.setEmail(detail.getEmail());
                req.setPhoneNum(detail.getPhoneNum());
                req.setAddress(detail.getAddress());
            }
        }
        
    }

    /**
     * @methodName  getFlaskService
     * @param       String baseURL, HttpEntity<?> requestEntity
     * @return      JSONObject
     * @throws      Exception
     * @description baseURL에 GET CALL 하는 Mehtod입니다.
     */
    public JSONObject getFlaskService(String baseURL, HttpEntity<?> requestEntity) throws Exception {
        logger.info("=====================> [EmployeeService / getFlaskService] baseURL : " + baseURL);
        JSONObject jsonObject = null;

        ResponseEntity<String> flaskResponse = restTemplate.exchange(
            baseURL, HttpMethod.GET, requestEntity, String.class);  

        if(flaskResponse != null && flaskResponse.getBody() != null) {
            logger.info(
                    "=====================> [EmployeeService / getFlaskService] flaskResponse :" + flaskResponse.getBody());
            // From String to JSONOBject
            JSONParser jsonParser = new JSONParser();
            String jsonString = String.valueOf(flaskResponse.getBody());
            jsonObject = (JSONObject)jsonParser.parse(jsonString);
        }

        return jsonObject;
    }


    /**
     * @methodName  getSpringService
     * @param       String baseURL, HttpEntity<?> requestEntity
     * @return      EmployeeDTO.Response
     * @throws      Exception
     * @description baseURL에 GET CALL 하는 Mehtod입니다.
     */
    public EmployeeDTO.Response getSpringService(String baseURL, HttpEntity<?> requestEntity) throws Exception {
        logger.info("=====================> [EmployeeService / getSpringService] baseURL : " + baseURL);

        ResponseEntity<EmployeeDTO.Response> springResponse = restTemplate.exchange(
            baseURL, HttpMethod.GET, requestEntity, EmployeeDTO.Response.class);  

        logger.info(
            "=====================> [EmployeeService / getSpringService] springResponse :" + springResponse.getBody());

        return springResponse.getBody();
    }


    /**
     * @methodName  postFlaskService
     * @param       String baseURL, HttpEntity<?> requestEntity
     * @return      JSONObject
     * @throws      Exception
     * @description baseURL에 requestEntity를 POST CALL 하는 Mehtod입니다.
     */
    public JSONObject callPostService(String baseURL, HttpEntity<?> requestEntity) throws Exception {
        logger.info(
                "=====================> [EmployeeService / getFlaskService] baseURL : " + baseURL);
        
        JSONObject jsonObject = null;
        ResponseEntity<String> restResponse = restTemplate.postForEntity(
            baseURL, requestEntity, String.class);  
            
        logger.info(
                "=====================> [EmployeeService / postFlaskService] flaskResponse :" + restResponse.getBody());

        // From String to JSONOBject
        JSONParser jsonParser = new JSONParser();
        String jsonString = String.valueOf(restResponse.getBody());
        jsonObject = (JSONObject)jsonParser.parse(jsonString);
        return jsonObject;
    }
}
