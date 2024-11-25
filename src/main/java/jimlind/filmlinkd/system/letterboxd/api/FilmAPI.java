package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.letterboxd.model.LBFilm;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
public class FilmAPI {
  @Autowired private Client client;

  public CombinedLBFilmModel fetch(String searchTerm) {
    // Search for the film by name
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s&searchMethod=%s";
    String input = UriUtils.encodePath(searchTerm, "UTF-8");
    String searchPath = String.format(uriTemplate, input, "FilmSearchItem", 1, "Autocomplete");

    LBSearchResponse searchResponse = this.client.get(searchPath, LBSearchResponse.class);
    if (searchResponse == null || searchResponse.items.isEmpty()) {
      return null;
    }

    LBFilmSummary filmSummary = searchResponse.items.get(0).film;

    // Load film details
    String filmDetailsPath = String.format("film/%s", filmSummary.id);
    LBFilm filmDetailsResponse = this.client.getAuthorized(filmDetailsPath, LBFilm.class);
    if (filmDetailsResponse == null) {
      return null;
    }

    // Load film statistics
    String filmStatisticsPath = String.format("film/%s/statistics", filmSummary.id);
    LBFilmStatistics filmStatisticsResponse =
        this.client.getAuthorized(filmStatisticsPath, LBFilmStatistics.class);
    if (filmStatisticsResponse == null) {
      return null;
    }

    CombinedLBFilmModel combinedLBFilmModel = new CombinedLBFilmModel();
    combinedLBFilmModel.film = filmDetailsResponse;
    combinedLBFilmModel.filmStatistics = filmStatisticsResponse;
    combinedLBFilmModel.filmSummary = filmSummary;

    return combinedLBFilmModel;
  }
}
