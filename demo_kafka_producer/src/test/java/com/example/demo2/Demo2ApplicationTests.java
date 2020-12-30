// package com.example.demo2;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.sql.SQLException;
// import java.text.SimpleDateFormat;

// import com.example.demo2.dao.EmployeeDAO;
// import com.example.demo2.repository.EmployeeRepository;
// import com.example.demo2.service.EmployeeService;
// import com.example.demo2.util.ServiceUtil;

// import org.json.simple.JSONObject;
// import org.json.simple.parser.JSONParser;
// import org.junit.jupiter.api.Test;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.client.RestTemplate;

// @SpringBootTest
// class Demo2ApplicationTests {

// 	@Autowired
// 	EmployeeRepository repository;

// 	@Autowired
// 	EmployeeService service;

// 	@Test
// 	void contextLoads() {
// 	}

// 	private RestTemplate restTemplate;
// 	@Autowired
//     public Demo2ApplicationTests(RestTemplate restTemplate) {
//         this.restTemplate = restTemplate;
//     }

// 	Logger logger = LoggerFactory.getLogger(Demo2ApplicationTests.class);

	// @Test
	// public void employeeTest() throws SQLException {

	// 	try {
	// 		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
	// 		EmployeeDAO emp = new EmployeeDAO();
			
	// 		emp.setEmployee_number(2017112);
	// 		emp.setName("SoonDoMan");
	// 		emp.setPosition("111");
	// 		emp.setSign_up_date(transFormat.parse("1234-11-03"));

	// 		Integer what = repository.update(emp);
	// 		assertEquals(0, what);
	// 	}catch(Exception e) {
	// 		e.printStackTrace(); 
	// 	}
		
	// }

	// @Test
	// public void employeeGetTest() throws Exception {
	// 	try{
	// 		// service.getEmployeeInfo(2);
	// 		// flask getMethod Call
	// 		String baseURL = "http://localhost/EmployeeInfo?id=85";
			
	// 		// Springboot Post Call
    //         HttpHeaders headers = new HttpHeaders();
    //         headers.setContentType(MediaType.APPLICATION_JSON);
    //         final HttpEntity<String> httpEntity = new HttpEntity<String>(headers);

	// 		JSONObject jsonObject = getFlaskService(baseURL, httpEntity);
			
	// 	}catch(Exception e ) {
	// 		e.printStackTrace();
	// 	}
	// }

	// /**
    //  * @methodName  getFlaskService
    //  * @return      String baseURL
    //  * @throws      Exception
    //  * @description baseURL에 GET CALL 하는 Mehtod입니다.
    //  */
    // public JSONObject getFlaskService(String baseURL, HttpEntity<?> requestEntity) throws Exception {
    //     logger.info(
    //             "=====================> [EmployeeService / getFlaskService] baseURL : " + baseURL);
        
    //     JSONObject jsonObject = null;

    //     ResponseEntity<String> flaskResponse = restTemplate.exchange(
    //         baseURL, HttpMethod.GET, requestEntity, String.class);  
        
    //     if(flaskResponse != null && flaskResponse.getBody() != null) {
    //         logger.info(
    //                 "=====================> [EmployeeService / getFlaskService] flaskResponse :" + flaskResponse.getBody());
    //         // From String to JSONOBject
    //         JSONParser jsonParser = new JSONParser();
    //         String jsonString = String.valueOf(flaskResponse.getBody());
    //         jsonObject = (JSONObject)jsonParser.parse(jsonString);
    //     }
    //     return jsonObject;
    // }

	// @Test
	// public void employeeUpdateTest() throws Exception {
	// 	try {
	// 		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
	// 		EmployeeDAO emp = new EmployeeDAO();
			
	// 		emp.setEmployee_number(2017112);
	// 		emp.setName("SoonDoMan");
	// 		emp.setPosition("111");
	// 		emp.setSign_up_date(transFormat.parse("1234-11-03"));

	// 		Integer what = repository.update(emp);
	// 		assertEquals(0, what);
	// 	}catch(Exception e) {
	// 		e.printStackTrace(); 
	// 	}
	// }

// }
