package br.com.test.graintransport.grain_transport_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Habilita o @Scheduled do StabilizationService
}
