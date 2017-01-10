package com.symphony;

import com.symphony.example.authentication.TokenGenerator;
import com.symphony.example.pods.PodDirectory;
import com.symphony.example.pods.WebhookConfiguration;
import com.symphony.symphony.client.HttpClientBuilder;
import com.symphony.symphony.client.SymphonyClientConfiguration;

import com.symphony.symphony.client.SymphonyClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({SymphonyClientConfiguration.class, WebhookConfiguration.class})
public class AppAuthExampleApplication {

    private final SymphonyClientConfiguration symphonyClientConfiguration;
    private final WebhookConfiguration webhookConfiguration;

    @Autowired
    public AppAuthExampleApplication(SymphonyClientConfiguration symphonyClientConfiguration, WebhookConfiguration webhookConfiguration) {
        this.symphonyClientConfiguration = symphonyClientConfiguration;
        this.webhookConfiguration = webhookConfiguration;
    }

    @Bean
    public PodDirectory getPodDirectory() {
        return new PodDirectory();
    }

    @Bean
    SymphonyClientFactory getSymphonyClientFactory() {
        return new SymphonyClientFactory(getHttpClientBuilder(), getPodDirectory());
    }

    @Bean
    HttpClientBuilder getHttpClientBuilder() {
        return new HttpClientBuilder(symphonyClientConfiguration);
    }

    @Bean
    TokenGenerator getTokenGenerator() {
        return new TokenGenerator();
    }

    public static void main(String[] args) {
        SpringApplication.run(AppAuthExampleApplication.class, args);
    }
}
