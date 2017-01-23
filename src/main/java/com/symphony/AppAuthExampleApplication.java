/*
 * Copyright 2016-2017 Symphony Application Authentication - Symphony LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
