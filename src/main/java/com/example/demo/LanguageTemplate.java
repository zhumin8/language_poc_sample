package com.example.demo;

import com.google.cloud.language.v1.LanguageServiceClient;

public class LanguageTemplate {

  private LanguageServiceClient languageServiceClient;

  public LanguageTemplate(LanguageServiceClient languageServiceClient) {
    this.languageServiceClient = languageServiceClient;
  }

  public LanguageServiceClient getLanguageServiceClient() {
    return languageServiceClient;
  }
}
