package com.function;

import com.microsoft.azure.functions.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.azure.functions.*;
import okhttp3.*;

/**
 * Azure Functions with Service Bus Trigger.
 */
public class ServiceBusQueueTriggerJava {
    /**
     * This function will be invoked when a new message is received at the Service
     * Bus Queue.
     */

    public TelemetryClient telemetry = new TelemetryClient();

    @FunctionName("ServiceBusQueueTriggerJava")
    public void run(
            @ServiceBusQueueTrigger(name = "message", queueName = "myinputqueue", connection = "StorageToIndexer_SERVICEBUS") String message,
            final ExecutionContext context) {
        context.getLogger().info("Java Service Bus Queue trigger function executed.");
        context.getLogger().info(message);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String endpoint = "https://prod-18.westus2.logic.azure.com:443/workflows/faed76bb5c43460186a9ae81cac85c80/triggers/manual/paths/invoke?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=oDHW9FrpLWUMbF86kudup5ANyUbz6QFQ0HHb-nShxeI";

        RequestBody bodyFromMessage = RequestBody.create(mediaType, message);

        Request request = new Request.Builder().url(
                "https://prod-18.westus2.logic.azure.com:443/workflows/faed76bb5c43460186a9ae81cac85c80/triggers/manual/paths/invoke?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=oDHW9FrpLWUMbF86kudup5ANyUbz6QFQ0HHb-nShxeI")
                .post(bodyFromMessage).addHeader("Content-Type", "application/json").build();

        LocalDateTime startTime = LocalDateTime.now();
        context.getLogger().info(telemetry.getContext().getInstrumentationKey());

        try {
            Response response = client.newCall(request).execute();
            context.getLogger().info(response.body().source().toString());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalDateTime endTime = LocalDateTime.now();

        Map<String, Double> metrics = new HashMap<>();

        metrics.put("StartTimeMicro", (double) startTime.getLong(ChronoField.MICRO_OF_DAY));
        metrics.put("EndTimeMicro", (double) endTime.getLong(ChronoField.MICRO_OF_DAY));

        Map<String, String> properties = new HashMap<>();
        properties.put("ProcessId", UUID.randomUUID().toString());
        properties.put("StartTime", startTime.toString());
        properties.put("EndTime", endTime.toString());

        telemetry.trackEvent("MessageProcessed", properties, metrics);
    }
}
