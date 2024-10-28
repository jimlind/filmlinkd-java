package jimlind.filmlinkd.system.letterboxd.model;

// https://api-docs.letterboxd.com/#/schemas/AbstractSearchItem
public class LBAbstractSearchItem {
  public String type;
  public long score;
  public LBContributor contributor;
  public LBFilmSummary film;
  public LBListSummary list;
}
