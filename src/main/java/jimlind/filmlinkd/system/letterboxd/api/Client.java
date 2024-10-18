package jimlind.filmlinkd.system.letterboxd.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

@Component
@Slf4j
public class Client {
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
}
