package com.example.servicea.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ServiceAController {

    @Autowired
    private OpenTelemetry openTelemetry;

    private final Tracer tracer;
    private final RestTemplate restTemplate = new RestTemplate();

    public ServiceAController(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("service-a");
    }

    @GetMapping("/")
    public ResponseEntity<String> processRequest() {
        Span span = tracer.spanBuilder("service-a-process")
                .setAttribute("service.name", "service-a")
                .setAttribute("operation", "process-request")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Логика service-a
            span.addEvent("Processing request in service-a");
            
            // Вызов service-b
            String serviceBResponse = callServiceB();
            
            span.addEvent("Received response from service-b: " + serviceBResponse);
            
            String response = "Service A processed request. Service B response: " + serviceBResponse;
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        } finally {
            span.end();
        }
    }

    private String callServiceB() {
        Span span = tracer.spanBuilder("call-service-b")
                .setAttribute("service.name", "service-a")
                .setAttribute("operation", "http-call")
                .setAttribute("http.method", "GET")
                .setAttribute("http.url", "http://service-b:8080/")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.addEvent("Calling service-b");
            String response = restTemplate.getForObject("http://service-b:8080/", String.class);
            span.addEvent("Service-b responded successfully");
            return response;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
