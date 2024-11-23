package jimlind.filmlinkd.system.letterboxd.web;

import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MemberWeb {
  public String getMemberLIDFromUsername(String username) {
    String url = String.format("https://letterboxd.com/%s/", username.toLowerCase());

    try {
      ResponseEntity<Void> response =
          WebClient.builder()
              .baseUrl(url)
              .build()
              .get()
              .retrieve()
              .toBodilessEntity()
              .timeout(Duration.ofSeconds(6))
              .block();
      return validateResponse(response);
    } catch (Exception e) {
      return "";
    }
  }

  private String validateResponse(ResponseEntity<Void> response) {
    if (response == null) {
      return "";
    }

    HttpHeaders headers = response.getHeaders();
    String letterboxdType = headers.getFirst("x-letterboxd-type");
    if (letterboxdType != null && !letterboxdType.equals("Member")) {
      return "";
    }

    String letterboxdId = headers.getFirst("x-letterboxd-identifier");
    return letterboxdId != null ? letterboxdId : "";
  }
}
