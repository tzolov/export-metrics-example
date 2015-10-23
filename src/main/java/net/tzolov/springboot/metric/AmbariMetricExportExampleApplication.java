package net.tzolov.springboot.metric;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.ambari.AmbariMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Add the <bold>ambari-metri-writer</bold> and the big-data maven repository to your POM:
 * 
 * <code>
 *  <dependencies>
 *     <dependency>
 *       <groupId>org.springframework.boot.actuate.metrics</groupId>
 *       <artifactId>ambari-metric-writer</artifactId>
 *       <version>0.0.10</version>
 *     </dependency>
 *     ...
 *  </dependencies>
 *  ...  
 *  <repositories>
 *     <repository>
 *       <id>bintray-big-data-maven</id>
 *       <name>bintray</name>
 *       <url>http://dl.bintray.com/big-data/maven</url>
 *     </repository>
 *     ..... 
 *  </repositories>
 * </code>
 * 
 * Add {@link AmbariMetricWriter} class to your scanBasePackageClasses
 * 
 * <code>
 * SpringBootApplication(scanBasePackageClasses = { AmbariMetricExportExampleApplication.class,
 *                                               AmbariMetricWriter.class }) 
 * </code>
 * 
 * The use the {@link GaugeService} and/or {@link CounterService} to push metrics or implement the {@link PublicMetrics}
 * to expose metrics to be pulled.
 * 
 * The push and pull metrics are automatically exposed by the <bold>/metrics</bold> endpoint.
 * 
 * Out of the box only the pull metrics are exported through the ambari-metric-writer. To enable export of pull type of
 * metrics (e.g. {@link PublicMetrics}) you have to add the {@link MetricsEndpointMetricReader} bean like this:
 * 
 * <code> 
 * \@Bean 
 * public MetricsEndpointMetricReader metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint) { 
 *   return new MetricsEndpointMetricReader(metricsEndpoint); 
 * }
 * </code>
 * 
 * @author tzolov@apache.org
 *
 */
@Configuration
@SpringBootApplication(scanBasePackageClasses = { AmbariMetricExportExampleApplication.class, AmbariMetricWriter.class })
public class AmbariMetricExportExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmbariMetricExportExampleApplication.class, args);
    }

    /**
     * Exports the all endpoint metrics like those implementing {@link PublicMetrics}.
     */
    @Bean
    public MetricsEndpointMetricReader metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint) {
        return new MetricsEndpointMetricReader(metricsEndpoint);
    }
}

/**
 * Simple REST controller that injects and uses the {@link GaugeService} and {@link CounterService} to submit metric
 * values.
 */
@RestController
class HelloController {

    @Autowired
    private GaugeService gaugeService;

    @Autowired
    private CounterService counterService;

    @RequestMapping("/api/v1/hello")
    public String hello() throws IOException {

        // submit counter metric (Push metric). Note that the actual metric name will be counter.hello.service.time!
        counterService.increment("hello.service.count");

        // submit gauge metric (PUsh metric).
        gaugeService.submit("hello.service.time", System.currentTimeMillis());

        // Returns greeting.
        return "Hello";
    }
}

/**
 * Implements a custom "pull" metric.
 * 
 * The {@link PublicMetrics} automatically appear in the /metrics endpoint but are not exported automatically!. To
 * export {@link PublicMetrics} through {@link MetricWriter} like ambari-metric-writer you have to define a
 * {@link MetricsEndpointMetricReader} bean in your application. See the
 * {@link AmbariMetricExportExampleApplication#metricsEndpointMetricReader(MetricsEndpoint)} above.
 */
@Service
class CustomPullMetrics implements PublicMetrics {

    @Override
    public Collection<Metric<?>> metrics() {
        int num = System.getenv().size();
        Metric<?> metric = new Metric<Integer>("hello.service.custom.metric", num, new Date());
        HashSet<Metric<?>> set = new HashSet<Metric<?>>();
        set.add(metric);
        return set;
    }
}