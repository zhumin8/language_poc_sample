package com.example.demo;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.spring.core.Credentials;
import com.google.cloud.spring.core.UserAgentHeaderProvider;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
  private static final Log LOGGER = LogFactory.getLog(Controller.class);
  @Autowired
  private LanguageServiceClient autoClient;

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

    String quotaProjectId = this.autoClient.getSettings().getQuotaProjectId();
    Map<String, String> headers = this.autoClient.getSettings().getHeaderProvider().getHeaders();
    Map<String, List<String>> requestMetadata = this.autoClient.getSettings().getCredentialsProvider().getCredentials().getRequestMetadata();

    LOGGER.info("quotaProjectId used in service client: " + quotaProjectId + "\n");
    LOGGER.info("this.autoClient.getSettings().getCredentialsProvider().getCredentials().getRequestMetadata();");
    requestMetadata.forEach((x, y) -> {
      LOGGER.info(x + ": " + y + "\n");
    });
    LOGGER.info("Headers");
    headers.forEach((x, y) -> {
      LOGGER.info(x + ": " + y + "\n");
    });
    InstantiatingExecutorProvider executorProvider = (InstantiatingExecutorProvider) this.autoClient.getSettings()
        .getBackgroundExecutorProvider();
    LOGGER.info("executorProvider thread count: " + executorProvider.getExecutorThreadCount());
  }

  /**
   * Usage with autoconfig bean.
   * should add settings and credentials to show difference from client library directly
   */
  @GetMapping("/autoconfig")
  void withAutoConfigClient() {

    callService(this.autoClient);
  }

  /**
   * Usage with client library directly.
   */
  @GetMapping("/nl")
  void languageClientLib() throws IOException {

    ExecutorProvider executorProvider =
        LanguageServiceSettings.defaultExecutorProviderBuilder()
            .setExecutorThreadCount(10)
            .build();

    // add some retry settings here.

    LanguageServiceSettings clientSettings =
        LanguageServiceSettings.newBuilder()
            .setQuotaProjectId("mzhu-test3")
            .setBackgroundExecutorProvider(executorProvider)
            .build();
    // Instantiates a client
    LanguageServiceClient language = LanguageServiceClient.create(clientSettings);

    callService(language);

    language.close();
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

  @GetMapping("env")
  String getEnv() {
    StringBuilder sb = new StringBuilder();
    System.getenv().forEach((k, v) -> {
      if (k.equals("GOOGLE_CLOUD_PROJECT")) {
        sb.append(k + ":" + v + "\n");
      }
      // System.out.println(k + ":" + v);
    });
    return sb.toString();
  }
}
