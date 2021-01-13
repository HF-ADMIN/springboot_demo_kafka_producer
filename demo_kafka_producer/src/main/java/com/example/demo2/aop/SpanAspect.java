package com.example.demo2.aop;

import com.example.demo2.dto.EmployeeDTO;
import com.example.demo2.kafka.Producer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import io.opentracing.Span;
import io.opentracing.Tracer;

@Aspect
@Component
public class SpanAspect {

    private final static String[] headers_to_propagate = {
        // All applications should propagate x-request-id. 
        "x-request-id",

        // Lightstep tracing header. Propagate this if you use lightstep tracing in Istio 
        "x-ot-span-context",

        // Datadog tracing header. Propagate these headers if you use Datadog tracing.
        "x-datadog-trace-id", "x-datadog-parent-id", "x-datadog-sampling-priority",

        // W3C Trace Context. Compatible with OpenCensusAgent and Stackdriver Istio configurations.
        "traceparent", "tracestate",

        // Cloud trace context. Compatible with OpenCensusAgent and Stackdriver Istio configurations.
        "x-cloud-trace-context",

        // Grpc binary trace context.
        "grpc-trace-bin",

        // b3 trace headers. Compatible with Zipkin, OpenCensusAgent, and Stackdriver Istio configurations. 
        "x-b3-traceid", "x-b3-spanid", "x-b3-parentspanid", "x-b3-sampled", "x-b3-flags",

        // Application-specific headers to forward.
        "end-user", "user-agent"};
    
    @Autowired
    private Tracer tracer;

    @Autowired
    private Environment env;

    Logger logger = LoggerFactory.getLogger(SpanAspect.class);

    private String kafkaUUID = null;

    // @Autowired
    // public SpanAspect(Tracer tracer){
    //     this.tracer = tracer;
    // }

    /**
     * @methodName  spanLogging
     * @return      Object
     * @description DemoOpentracingApplication Class에서 Bean으로 등록한 tracer를 사용하여 Span을 생성하고 종료합니다.
     *              com.example.demo2.controller.EmployeeController.*(..) 패턴으로 Around AOP를 적용합니다.
     */    
    @Around("execution(* com.example.demo2.controller.EmployeeController.*(..))")
    public Object spanLogging(ProceedingJoinPoint pjp) throws Throwable {
        logger.info("[                             spanLogging Start                                ]");
        logger.info("============================  [ AOP:spanLogging ]");

        /*
        Span span = tracer.buildSpan(pjp.getSignature().getName()).start();
        Object result = pjp.proceed();
        span.finish();
        Span span = tracer.buildSpan(pjp.getSignature().getName()).start();
        span.finish();
        */
        /*
        Span parentSpan = tracer.scopeManager().activeSpan();
        Span spanPhase1 = tracer.buildSpan("spanPhase_1").asChildOf(parentSpan).start();
        Object result = pjp.proceed();
        spanPhase1.log("                                                SpanPhase1 log");
        spanPhase1.finish();
        */
        
        Span parentSpan = tracer.scopeManager().activeSpan();
        Span spanPhase1 = tracer.buildSpan(pjp.getSignature().getName()).asChildOf(parentSpan).start();
        spanPhase1.log("                                                SpanPhase1 log");
        Object result = pjp.proceed();
        spanPhase1.finish();
        
        logger.info("[                             spanLogging End                                ]");
        return result;
    }

    /**
     * @methodName  makeHttpHeaders
     * @return      org.springframework.http.HttpHeaders
     * @description headers_to_propagate Array에 명시된 Headers 정보를 가지고 Jaeger Trace Propagation을 위해서 HttpHeaders를 Exchange합니다.
     */
    public HttpHeaders makeHttpHeaders(HttpHeaders httpHeaders) {
        logger.info("[                             makeHttpHeaders                                ]");
        final HttpHeaders headers = new HttpHeaders();
        for (String header : headers_to_propagate) {
            if (httpHeaders.containsKey(header)) {
                logger.info("==========================  [ AOP:makeHttpHeaders ] KEY : " + header + ", VALUE : "
                        + httpHeaders.get(header).get(0));
                headers.add(header, httpHeaders.get(header).get(0));
            }
        }

        return headers;
    }

    /**
     * @methodName  headerExchange
     * @return      Object
     * @description ProceedingJoinPoint에서 Args로 넘어온 HttpHeaders를 가져와서 makeHttpHeaders Method를 사용하여 Http Header 정보를 Exchange한다.
     *              com.example.demo2.controller.EmployeeController.*EmployeesInfo*(..) 패턴으로 Around AOP를 적용합니다.
     */    
    @Around("execution(* com.example.demo2.controller.EmployeeController.get*EmployeesInfo(..))")
    public Object headerExchangeGet(ProceedingJoinPoint pjp) throws Throwable {
        logger.info("[                             headerExchangeGet Start                                ]");
        logger.info("============================  [ AOP:headerExchange ]");
        HttpHeaders headers = null;
        Integer id = null;
        Object result = null;

        // getMehtod의 Type : all / single
        String getType = null;
        for(Object obj : pjp.getArgs()) {
            logger.info("==========================  [ PJP Args ] " + obj.toString());
            if(obj instanceof HttpHeaders) {
                logger.info("==========================  [ PJP Args : HttpHeaders ] ");
                headers = makeHttpHeaders((HttpHeaders) obj);
            }

            getType = "all";

            if(obj instanceof Integer) {
                id = (Integer) obj;
                getType = "single";
            }
        }
        // Span span = tracer.buildSpan(pjp.getSignature().getName()).start();
        if("all".equals(getType)) {
            Object[] args = {headers};
            result = pjp.proceed(args);
        }else if("single".equals(getType)) {
            Object[] args = {headers, id};
            result = pjp.proceed(args);
        }
        // span.finish();

        logger.info("[                             headerExchangeGet End                               ]");
        ResponseEntity<EmployeeDTO.Response> entity = (ResponseEntity<EmployeeDTO.Response>) result;
        headers = makeHttpHeaders(entity.getHeaders());
        return ResponseEntity.ok().headers(headers).body(entity.getBody());
    }



    /**
     * @methodName  headerExchange
     * @return      Object
     * @description ProceedingJoinPoint에서 Args로 넘어온 HttpHeaders를 가져와서 makeHttpHeaders Method를 사용하여 Http Header 정보를 Exchange한다.
     *              com.example.demo2.controller.EmployeeController.*EmployeesInfo*(..) 패턴으로 Around AOP를 적용합니다.
     */    
    @Around("execution(* com.example.demo2.controller.EmployeeController.postEmployeesInfo(..))")
    public Object headerExchangePost(ProceedingJoinPoint pjp) throws Throwable {
        logger.info("[                             headerExchangePost Start                                ]");
        logger.info("============================  [ AOP:headerExchange ]");
        HttpHeaders headers = null;
        EmployeeDTO.Request request = null;


        for(Object obj : pjp.getArgs()) {
            logger.info("==========================  [ PJP Args ] " + obj.toString());
            if(obj instanceof HttpHeaders) {
                logger.info("==========================  [ PJP Args : HttpHeaders ] ");
                headers = makeHttpHeaders((HttpHeaders) obj);
            }

            if(obj instanceof EmployeeDTO.Request) {
                request = (EmployeeDTO.Request) obj;
            }
        }
        // Span span = tracer.buildSpan(pjp.getSignature().getName()).start();
        Object[] args = {headers, request};
        Object result = pjp.proceed(args);
        // span.finish();

        logger.info("[                             headerExchangePost End                               ]");
        ResponseEntity<EmployeeDTO.Response> entity = (ResponseEntity<EmployeeDTO.Response>) result;
        headers = makeHttpHeaders(entity.getHeaders());
        return ResponseEntity.ok().headers(headers).body(entity.getBody());
    }


    /**
     * @methodName  messageBeforeQueueLogging
     * @return      null
     * @description http request header, body 값으로 kafka msg produce
     *              com.example.demo2.controller.EmployeeController.*EmployeesInfo*(..) 패턴으로 Before AOP를 적용합니다.
     */
    @Before("execution(* com.example.demo2.controller.EmployeeController.postEmployeesInfo(..))")
    public void messageBeforeQueueLogging(JoinPoint pjp) throws Throwable {
        logger.info("[                             Kafka Before Logging Start                                ]");
        logger.info("============================  [ AOP:messageBeforeQueueLogging ]");
        logger.info(kafkaUUID);

        kafkaUUID = UUID.randomUUID().toString();
        String Msg = "";
        String headersString = "";
        String bodyString = "";
        String timeStamp = "";
        LocalDateTime currentDateTime = LocalDateTime.now();
        ObjectMapper mapper = new ObjectMapper();

        timeStamp = "[ "+ currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + " ]|"
                  + "[ " + env.getProperty("kafka.service.code") + " ]|"
                  + "[ " + kafkaUUID + " ]|";


        for(Object obj : pjp.getArgs()) {
            if(obj instanceof HttpHeaders) {
                headersString = timeStamp + obj.toString();
            }

            if(obj instanceof EmployeeDTO.Request) {
                bodyString = mapper.writeValueAsString(obj);
            }
        }

        Msg = headersString +" "+ bodyString;
        logger.info(Msg);
        Producer.produce(env.getProperty("kafka.brokers"), env.getProperty("kafka.topic"), Msg);

        logger.info("[                             Kafka Before Logging END                                ]");

    }


    /**
     * @methodName  messageAfterQueueLogging
     * @return      null
     * @description http request header, body 값으로 kafka msg produce
     *              com.example.demo2.controller.EmployeeController.*EmployeesInfo*(..) 패턴으로 Before AOP를 적용합니다.
     */
    @After("execution(* com.example.demo2.controller.EmployeeController.postEmployeesInfo(..))")
    public void messageAfterQueueLogging(JoinPoint pjp) throws Throwable {
        logger.info("[                             Kafka After Logging Start                                ]");
        logger.info("============================  [ AOP:messageAfterQueueLogging ]");
        logger.info(kafkaUUID);

        String Msg = "";
        String headersString = "";
        String bodyString = "";
        String timeStamp = "";
        LocalDateTime currentDateTime = LocalDateTime.now();
        ObjectMapper mapper = new ObjectMapper();

        timeStamp = "[ "+ currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + " ]|"
                  + "[ " + env.getProperty("kafka.service.code") + " ]|"
                  + "[ " + kafkaUUID + " ]|";


        for(Object obj : pjp.getArgs()) {
            if(obj instanceof HttpHeaders) {
                headersString = timeStamp + obj.toString();
            }

            if(obj instanceof EmployeeDTO.Request) {
                bodyString = mapper.writeValueAsString(obj);
            }
        }

        Msg = headersString +" "+ bodyString;
        logger.info(Msg);
        Producer.produce(env.getProperty("kafka.brokers"), env.getProperty("kafka.topic"), Msg);

        logger.info("[                             Kafka After Logging END                                ]");

    }
}