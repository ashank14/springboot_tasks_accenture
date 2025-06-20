package spring_tasks.spring_project.config;

import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public OtlpMeterRegistry otlpMeterRegistry() {
        return OtlpMeterRegistry.builder(OtlpConfig.DEFAULT).build();
    }
}
