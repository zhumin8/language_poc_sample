package com.example.demo;

import com.google.cloud.language.v1.LanguageServiceClient;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(LanguageTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.gcp.language.enabled", matchIfMissing = true)
public class LanguageAutoConfig {
  @Bean
  @ConditionalOnMissingBean
  public LanguageServiceClient languageServiceClient() throws IOException {
     return LanguageServiceClient.create();
  }

  @Bean
  @ConditionalOnMissingBean
  public LanguageTemplate cloudVisionTemplate(LanguageServiceClient languageServiceClient) {
    return new LanguageTemplate(languageServiceClient);
  }
}
