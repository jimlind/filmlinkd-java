package jimlind.filmlinkd.system.letterboxd.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

@Component
public class Client {
  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";

  public <T> ResponseEntity<T> get(Function<UriBuilder, URI> uriFunction, Class<T> inputClass) {
    return this.buildClient()
        .get()
        .uri(uriFunction)
        .acceptCharset(StandardCharsets.UTF_8)
        .retrieve()
        .toEntity(inputClass)
        .block();
  }

  private WebClient buildClient() {
    return WebClient.builder().baseUrl(BASE_URL).build();
  }
}
