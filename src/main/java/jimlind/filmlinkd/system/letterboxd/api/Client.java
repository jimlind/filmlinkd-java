package jimlind.filmlinkd.system.letterboxd.api;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.google.gson.GsonBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jimlind.filmlinkd.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Client {
  @Autowired private Config config;

  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";

  public <T> T get(String path, Class<T> inputClass) {
    return this.request(path, "", inputClass);
  }

  public <T> T getAuthorized(String path, Class<T> inputClass) {
    String key = this.config.getLetterboxdApiKey();
    String nonce = String.valueOf(java.util.UUID.randomUUID());
    String now = String.valueOf(Instant.now().getEpochSecond());
    // TODO: This feels like a terrible way to build a URL string
    // The actual JAVA URI Classes probably can do it better
    String symbol = path.contains("?") ? "&" : "?";
    String uri = path + symbol + String.format("apikey=%s&nonce=%s&timestamp=%s", key, nonce, now);
    String url = BASE_URL + uri;
    String authorization = "Signature " + this.buildSignature("GET", url);

    return this.request(uri, authorization, inputClass);
  }

  private <T> T request(String uri, String authorization, Class<T> inputClass) {
    HttpResponse<String> httpResponse;
    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
              .header("Authorization", authorization)
              .uri(new URI(BASE_URL + uri))
              .timeout(Duration.of(6, SECONDS))
              .GET()
              .build();
      httpResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception e) {
      log.atError()
          .setMessage("Error on HttpClient GET")
          .addKeyValue("exception", e)
          .addKeyValue("uri", uri)
          .log();
      return null;
    }

    if (httpResponse.statusCode() > 200 && httpResponse.statusCode() >= 300) {
      return null;
    }

    String body = httpResponse.body();
    if (body.isBlank()) {
      return null;
    }

    try {
      return new GsonBuilder().create().fromJson(body, inputClass);
    } catch (Exception e) {
      log.atError()
          .setMessage("Error on Instance Creation")
          .addKeyValue("exception", e)
          .addKeyValue("body", body)
          .log();
      return null;
    }
  }

  private String buildSignature(String method, String url) {
    method = method.toUpperCase();
    String shared = this.config.getLetterboxdApiShared();
    SecretKeySpec secretKeySpec = new SecretKeySpec(shared.getBytes(), "HmacSHA256");

    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      sha256_HMAC.init(secretKeySpec);
      String data = method + "\u0000" + url + "\u0000"; // "�"
      return bytesToHex(sha256_HMAC.doFinal(data.getBytes()));
    } catch (Exception e) {
      return "";
    }
  }

  private static String bytesToHex(byte[] in) {
    final StringBuilder builder = new StringBuilder();
    for (final byte b : in) builder.append(String.format("%02x", b));
    return builder.toString();
  }
}
