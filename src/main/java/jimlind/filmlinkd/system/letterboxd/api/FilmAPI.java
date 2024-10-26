package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.letterboxd.model.LBFilm;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FilmAPI {
  @Autowired private Client client;

  public CombinedLBFilmModel fetch(String searchTerm) {
    // Search for the film by name
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s&searchMethod=%s";
    String searchPath = String.format(uriTemplate, searchTerm, "FilmSearchItem", 1, "Autocomplete");

    ResponseEntity<LBSearchResponse> searchResponse =
        this.client.get(searchPath, LBSearchResponse.class);
    if (searchResponse == null
        || searchResponse.getBody() == null
        || searchResponse.getBody().items.isEmpty()) {
      return null;
    }

    LBFilmSummary filmSummary = searchResponse.getBody().items.get(0).film;

    // Load film details
    String filmDetailsPath = String.format("film/%s", filmSummary.id);
    ResponseEntity<LBFilm> filmDetailsResponse =
        this.client.getAuthorized(filmDetailsPath, LBFilm.class);
    if (filmDetailsResponse == null || filmDetailsResponse.getBody() == null) {
      return null;
    }

    // Load film statistics
    String filmStatisticsPath = String.format("film/%s/statistics", filmSummary.id);
    ResponseEntity<LBFilmStatistics> filmStatisticsResponse =
        this.client.getAuthorized(filmStatisticsPath, LBFilmStatistics.class);
    if (filmStatisticsResponse == null || filmStatisticsResponse.getBody() == null) {
      return null;
    }

    CombinedLBFilmModel combinedLBFilmModel = new CombinedLBFilmModel();
    combinedLBFilmModel.film = filmDetailsResponse.getBody();
    combinedLBFilmModel.filmStatistics = filmStatisticsResponse.getBody();
    combinedLBFilmModel.filmSummary = filmSummary;

    return combinedLBFilmModel;
  }
}
