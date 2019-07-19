
# Notes: 

Endpoint for Elastic: 
http://lb-s72r3une3gbju.westus2.cloudapp.azure.com:9200/

Get all the results out of elastic: 
http://lb-s72r3une3gbju.westus2.cloudapp.azure.com:9200/slbindex/_search/?size=1000&pretty=true 

Third Party Library for HttpClient in Azure Functions: 
https://square.github.io/okhttp/recipes/ 

Editing the host.json to provide some sort of rate limiting from the Azure Function off service bus towards the indexer process. 
https://docs.microsoft.com/en-us/azure/azure-functions/functions-bindings-service-bus#hostjson-settings 


# App Insights Queries: 
```kusto
customEvents
| extend startTime = customMeasurements.['StartTime']
| extend stopTime = customMeasurements.['EndTime']
| project startTime, stopTime, timestamp  
```

```kusto
customEvents
| extend startTime = tolong(customMeasurements.['StartTime'])
| extend stopTime = tolong(customMeasurements.['EndTime'])
| project customMeasurements, startTime, stopTime
| order by startTime asc
```

```kusto
let table3 = customEvents 
| where name == "DocProcessed"
| extend StartTime = todatetime(customDimensions.['StartTime'])
| extend StopTime = todatetime(customDimensions.['EndTime'])
| extend SessionId = customDimensions.['DocumentProcessed']
| project customMeasurements, StartTime, StopTime, SessionId, customDimensions;
table3
| mv-expand samples = range(bin(StartTime, 1s), StopTime, 1s)
| summarize count(SessionId) by bin(todatetime(samples), 1s) 
| order by samples asc
```

