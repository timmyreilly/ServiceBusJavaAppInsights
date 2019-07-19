package com.function;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerJava {
    /**
     * This function listens at endpoint "/api/HttpTriggerJava". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTriggerJava
     * 2. curl {your host}/api/HttpTriggerJava?name=HTTP%20Query
     */
    public TelemetryClient telemetry = new TelemetryClient(); 

    @FunctionName("HttpTriggerJava")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        LocalDateTime startTime = LocalDateTime.now();
        context.getLogger().info(telemetry.getContext().getInstrumentationKey()); 
        
        try {
            Thread.sleep(2000); 
        } catch(InterruptedException e) {
            telemetry.trackException(e);
        }

        LocalDateTime endTime = LocalDateTime.now(); 

        Map<String, Double> metrics = new HashMap<>(); 

        metrics.put("StartTimeMicro", (double) startTime.getLong(ChronoField.MICRO_OF_DAY));
        metrics.put("EndTimeMicro", (double) endTime.getLong(ChronoField.MICRO_OF_DAY)); 
        
        Map<String, String> properties = new HashMap<>(); 
        properties.put("ProcessId", UUID.randomUUID().toString()); 
        properties.put("StartTime", startTime.toString()); 
        properties.put("EndTime", endTime.toString()); 

        telemetry.trackEvent("DocProcessed", properties, metrics); 

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }
}
