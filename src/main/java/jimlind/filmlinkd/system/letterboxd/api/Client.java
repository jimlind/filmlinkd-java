package jimlind.filmlinkd.system.letterboxd.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jimlind.filmlinkd.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

@Component
@Slf4j
public class Client {
  @Autowired private Config config;

  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";

  public <T> ResponseEntity<T> get(Function<UriBuilder, URI> uriFunction, Class<T> inputClass) {
    try {
      ResponseEntity<T> response =
          this.buildClient()
              .get()
              .uri(uriFunction)
              .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
              .acceptCharset(StandardCharsets.UTF_8)
              .retrieve()
              .toEntity(inputClass)
              .timeout(Duration.ofSeconds(6))
              .block();

      return validateResponse(response);
    } catch (Exception e) {
      UriBuilder b = new DefaultUriBuilderFactory().builder();
      log.atError()
          .setMessage("Error on WebClient GET")
          .addKeyValue("exception", e)
          .addKeyValue("uri", uriFunction.apply(b))
          .addKeyValue("classOutput", inputClass)
          .log();

      return null;
    }
  }

  public <T> ResponseEntity<T> getAuthorized(String path, Class<T> inputClass) {
    String key = this.config.getLetterboxdApiKey();
    String nonce = String.valueOf(java.util.UUID.randomUUID());
    String now = String.valueOf(Instant.now().getEpochSecond());
    String uri = path + String.format("?apikey=%s&nonce=%s&timestamp=%s", key, nonce, now);
    String url = BASE_URL + uri;
    String signature = this.buildSignature("GET", url);

    ResponseEntity<T> response =
        this.buildClient()
            .get()
            .uri(uri)
            .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
            .header("Authorization", "Signature " + signature)
            .acceptCharset(StandardCharsets.UTF_8)
            .retrieve()
            .toEntity(inputClass)
            .timeout(Duration.ofSeconds(6))
            .block();

    return validateResponse(response);
  }

  private WebClient buildClient() {
    return WebClient.builder().baseUrl(BASE_URL).build();
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

  private String buildSignature(String method, String url) {
    try {
      String shared = this.config.getLetterboxdApiShared();
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec = new SecretKeySpec(shared.getBytes(), "HmacSHA256");
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
