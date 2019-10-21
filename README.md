# Spring Boot Custom Metric Definition and Export

Example application that shows how to define custom `push` and/or `pull` metrics and how to export them to external  repositories like Ambari Metric System. 
Additional information how to use the [ambari-metric-writer](https://github.com/tzolov/ambari-metric-writer).

## 1. Implement custom `Push` and `Pull` metrics.

#### Push Metrics
The use the `GaugeService` and/or `CounterService` to `push` metrics
```java
@RequestMapping("/api/v1/hello")
public String hello() throws IOException {
  counterService.increment("hello.service.count");
  gaugeService.submit("hello.service.time", System.currentTimeMillis());

  return "Hello";
}
```
#### Pull Metrics
Implement `PublicMetrics` to expose `pull` metrics.
```java
@Service
class CustomPullMetrics implements PublicMetrics {
    public Collection<Metric<?>> metrics() {
        Metric<?> metric = ...
        HashSet<Metric<?>> set = new HashSet<Metric<?>>();
        set.add(metric);
        return set;
    }
}
```
The `push` and `pull` metrics are automatically exposed via the `/metrics` endpoint. But out of the box only the `pull` metrics are exported automatically through `MetricWriter`s. To export the `pull` metrics (e.g. `PublicMetrics`) you have to define a `MetricsEndpointMetricReader` bean like this:
```java
@Bean 
public MetricsEndpointMetricReader metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint) { 
  return new MetricsEndpointMetricReader(metricsEndpoint); 
}
```

#### Build
```
mvn clean install
```

#### Test though the `/metrics` endpoint
Start the application
```
java -jar java -jar target/export-metrics-example-0.0.1-SNAPSHOT.jar
```
Open [http://localhost:8080/hellow](http://localhost:8080/hellow) to generate metric data and then open [http://localhost:8080/metrics](http://localhost:8080/metrics) and check `gauge.hello.service.time`, `counter.hello.service.count` and `hello.service.custom.metric` metric values.

## 2. Export the metrics to Ambari Metric System
Deatil information how to use the [ambari-metric-writer](https://github.com/tzolov/ambari-metric-writer).

#### Add the `ambari-metri-writer` and the big-data maven repository to your POM:
```xml
<dependencies>
   <dependency>
     <groupId>org.springframework.boot.actuate.metrics</groupId>
     <artifactId>ambari-metric-writer</artifactId>
     <version>0.0.10</version>
   </dependency>
   ...
</dependencies>
...  
<repositories>
   <repository>
     <id>bintray-big-data-maven</id>
     <name>bintray</name>
     <url>http://dl.bintray.com/big-data/maven</url>
   </repository>
   ..... 
</repositories>
```

#### Add `AmbariMetricWriter` class to your `scanBasePackageClasses`
```java
@Configuration
@SpringBootApplication(scanBasePackageClasses = { AmbariMetricExportExampleApplication.class, AmbariMetricWriter.class })
public class AmbariMetricExportExampleApplication {...}
```

#### Test the exported metrics
Use the `dummy` mode to print the exported metrics instead fo sending them to the Ambari Collector Server
```
java -jar target/export-metrics-example-0.0.1-SNAPSHOT.jar --spring.metrics.export.ambari.metrics-collector-host=localhost --spring.metrics.export.ambari.metrics-buffer-size=0 --spring.metrics.export.ambari.writer-type=dummy --spring.metrics.export.triggers.ambariMetricExporter.includes=*hello.service.*
```
The `spring.metrics.export.ambari.metrics-collector-host` property has to be set or activate the exporter. The `--spring.metrics.export.triggers.ambariMetricExporter.includes=*hello.service.*` property filters in only the custom metrics. The `--spring.metrics.export.ambari.metrics-buffer-size=0` sets the export batch size to 0.

On the screen log you should see json messages like this:
```json
{
  "metrics" : [ {
    "metricname" : "hello.service.custom.metric",
    "hostname" : "hostname",
    "timestamp" : 1445615822846,
    "appid" : "application.9f5ba1b3973368d3d5dea242af344d04",
    "instanceid" : "",
    "starttime" : 1445615822846,
    "type" : "Integer",
    "metrics" : {
      "1445615822846" : 32.0
    }
  } ]
}
```


Additional information how to use the [ambari-metric-writer](https://github.com/tzolov/ambari-metric-writer).
