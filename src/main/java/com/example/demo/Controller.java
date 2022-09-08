package com.example.demo;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
  private static final Log LOGGER = LogFactory.getLog(Controller.class);
  @Autowired
  private LanguageServiceClient autoClient;

  private LanguageServiceClient languageClient;
  // The text to analyze
  private String[] texts = {"I love this!", "I hate this!",
      "Google, headquartered in Mountain View (1600 Amphitheatre Pkwy, Mountain View, CA 940430),"
          + " unveiled the new Android phone for $799 at the Consumer Electronic Show. "
          + "Sundar Pichai said in his keynote that users love their new Android phones."};

  @GetMapping("/")
  String sanityCheck() {
    return "hello world!!";
  }

  @GetMapping("/printsettings")
  void printAutoconfigSettings() throws IOException {

    LOGGER.info("===========Spring auto-configuration Client Settings: ===========");
    logSettingsForServiceClient(this.autoClient);

    LOGGER.info("===========Client Library Direct Setup Client Settings: ===========");
    logSettingsForServiceClient(this.languageClient);
  }

  /**
   * Usage with autoconfig bean.
   * should add settings and credentials to show difference from client library directly
   */
  @GetMapping("/autoconfig")
  void withAutoConfigClient() {

    LOGGER.info("===========Calling Client Library with Spring auto-configuration.===========");

    callService(this.autoClient);
  }

  /**
   * Usage with client library directly.
   */
  @GetMapping("/nl")
  void languageClientLib() throws IOException {
    LOGGER.info("===========Calling Client Library directly.===============");

    ExecutorProvider executorProvider =
        LanguageServiceSettings.defaultExecutorProviderBuilder()
            .setExecutorThreadCount(10)
            .build();

    // set quota-project-id and executor-thread-count
    LanguageServiceSettings.Builder clientSettingsBuilder =
        LanguageServiceSettings.newBuilder()
            // .setQuotaProjectId("project-id")
            // for service account, alternatively:
            // export GOOGLE_APPLICATION_CREDENTIALS=/path/to/my/key.json
            .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(
                new FileInputStream("/path/to/key.json")))
            .setBackgroundExecutorProvider(executorProvider);
    // add some retry settings here.
    RetrySettings retrySettings = clientSettingsBuilder
        .analyzeSentimentSettings()
        .getRetrySettings()
        .toBuilder()
        .setRetryDelayMultiplier(3.0)
        .build();
    clientSettingsBuilder
        .analyzeSentimentSettings()
        .setRetrySettings(retrySettings);
    // Instantiates a client
    this.languageClient = LanguageServiceClient.create(clientSettingsBuilder.build());

    callService(this.languageClient);

    this.languageClient.close();
  }

  private void callService(LanguageServiceClient serviceClient) {
    for (String text : this.texts) {
      Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
      // Detects the sentiment of the text
      Sentiment sentiment = serviceClient.analyzeSentiment(doc).getDocumentSentiment();
      // for (int i = 0; i < 20 ; i++) {
      //  this.autoClient.analyzeSentiment(doc).getDocumentSentiment();
      //   }
      LOGGER.info(String.format("Text: \"%s\"%n", text));
      LOGGER.info(String.format(
          "Sentiment: score = %s, magnitude = %s%n",
          sentiment.getScore(), sentiment.getMagnitude()));

      List<Entity> entities = serviceClient.analyzeEntities(doc).getEntitiesList();
      LOGGER.info("Analyze Entities: ");
      entities.forEach(x -> LOGGER.info("Entity: " + x.getName() +
          ", Salience: " + x.getSalience() +
          ", Sentiment: " + x.getSentiment() +
          ", Mention counts: " + x.getMentionsCount()));
    }
  }

  private void logSettingsForServiceClient(LanguageServiceClient serviceClient) throws IOException {
    String quotaProjectId = serviceClient.getSettings().getQuotaProjectId();
    Map<String, String> headers = serviceClient.getSettings().getHeaderProvider().getHeaders();
    Credentials credentials = serviceClient.getSettings().getCredentialsProvider().getCredentials();
    // Map<String, List<String>> requestMetadata = serviceClient.getSettings().getCredentialsProvider().getCredentials().getRequestMetadata();

    if (credentials instanceof UserCredentials) {
      LOGGER.info("User credentials are being used.");
      LOGGER.info("Client Id: " + ((UserCredentials) credentials).getClientId());
      LOGGER.info("Quota Project Id: " + ((UserCredentials) credentials).getQuotaProjectId());
    }
    if (credentials instanceof ServiceAccountCredentials) {
      LOGGER.info("Service account credentials are being used.");
      LOGGER.info("Client Email: " + ((ServiceAccountCredentials) credentials).getClientEmail());
      LOGGER.info("Project Id: " + ((ServiceAccountCredentials) credentials).getProjectId());

    }
    LOGGER.info("quotaProjectId used in service client: " + quotaProjectId + "\n");
    // LOGGER.info("this.autoClient.getSettings().getCredentialsProvider().getCredentials().getRequestMetadata();");
    // requestMetadata.forEach((x, y) -> {
    //   LOGGER.info(x + ": " + y + "\n");
    // });
    InstantiatingExecutorProvider executorProvider = (InstantiatingExecutorProvider) serviceClient.getSettings()
        .getBackgroundExecutorProvider();
    LOGGER.info("executorProvider thread count: " + executorProvider.getExecutorThreadCount());

    RetrySettings retrySettings = serviceClient.getSettings().analyzeSentimentSettings()
        .getRetrySettings();
    LOGGER.info("analyzeSentimentSettings - RetryDelayMultiplier: " + retrySettings.getRetryDelayMultiplier());

    LOGGER.info("Headers");
    headers.forEach((x, y) -> {
      LOGGER.info(x + ": " + y + "\n");
    });
  }

  @GetMapping("env")
  String getEnv() {
    StringBuilder sb = new StringBuilder();
    System.getenv().forEach((k, v) -> {
      if (k.equals("GOOGLE_CLOUD_PROJECT") || k.equals("GOOGLE_APPLICATION_CREDENTIALS")) {
        sb.append(k + ":" + v + "\n");
      }
      // System.out.println(k + ":" + v);
    });
    return sb.toString();
  }
}
