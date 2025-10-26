package com.example.serviceb.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceBController {

    @Autowired
    private OpenTelemetry openTelemetry;

    private final Tracer tracer;

    public ServiceBController(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("service-b");
    }

    @GetMapping("/")
    public ResponseEntity<String> processRequest() {
        Span span = tracer.spanBuilder("service-b-process")
                .setAttribute("service.name", "service-b")
                .setAttribute("operation", "process-request")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Логика service-b
            span.addEvent("Processing request in service-b");
            
            // Имитация обработки
            Thread.sleep(100);
            
            span.addEvent("Request processed successfully in service-b");
            
            String response = "Service B processed request successfully";
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        } finally {
            span.end();
        }
    }
}
