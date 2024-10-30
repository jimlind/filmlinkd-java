package jimlind.filmlinkd.system.letterboxd.api;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jimlind.filmlinkd.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class Client {
  @Autowired private Config config;

  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";

  public <T> ResponseEntity<T> get(String path, Class<T> inputClass) {
    String authorization = "";

    return this.request(path, authorization, inputClass);
  }

  public <T> ResponseEntity<T> getAuthorized(String path, Class<T> inputClass) {
    String key = this.config.getLetterboxdApiKey();
    String nonce = String.valueOf(java.util.UUID.randomUUID());
    String now = String.valueOf(Instant.now().getEpochSecond());
    // TODO: This feels like a terrible way to build a URL string
    String symbol = path.contains("?") ? "&" : "?";
    String uri = path + symbol + String.format("apikey=%s&nonce=%s&timestamp=%s", key, nonce, now);
    String url = BASE_URL + uri;
    String authorization = "Signature " + this.buildSignature("GET", url);

    return this.request(uri, authorization, inputClass);
  }

  private <T> ResponseEntity<T> request(String uri, String authorization, Class<T> inputClass) {
    WebClient.ResponseSpec responseSpec =
        WebClient.builder()
            .baseUrl(BASE_URL)
            .build()
            .get()
            .uri(uri)
            .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
            .header("Authorization", authorization)
            .acceptCharset(StandardCharsets.UTF_8)
            .retrieve();

    try {
      ResponseEntity<T> response =
          responseSpec.toEntity(inputClass).timeout(Duration.ofSeconds(6)).block();
      return validateResponse(response);
    } catch (Exception e) {
      log.atError()
          .setMessage("Error on WebClient GET")
          .addKeyValue("exception", e)
          .addKeyValue("uri", uri)
          .addKeyValue("classOutput", inputClass)
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

  private <T> ResponseEntity<T> validateResponse(ResponseEntity<T> response) {
    if (response == null) {
      return null;
    }
    if (response.getStatusCode().is2xxSuccessful()) {
      return response;
    }
    return null;
  }
}
