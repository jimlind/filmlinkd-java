package jimlind.filmlinkd.system.letterboxd;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.HashSet;

public class LidComparer {
  /**
   * Compare Letterboxd IDs
   *
   * <p>Negative Value: A is before B.<br>
   * Zero Value: A is the same as B.<br>
   * Positive Value: A is after B
   *
   * <p>Shorter strings come before longer strings<br>
   * Strings are normally compared lowest to highest [0-9][A-Z][a-z] Letterboxd LIDs are compared
   * lowest to highest [0-9][a-z][A-Z] So we need to swap cases before doing a comparison
   */
  public static int compare(String letterboxdIdA, String letterboxdIdB) {
    if (letterboxdIdA == null || letterboxdIdB == null) {
      return 0;
    }

    if (letterboxdIdA.length() != letterboxdIdB.length()) {
      return letterboxdIdA.length() - letterboxdIdB.length();
    }

    return swapCase(letterboxdIdA).compareTo(swapCase(letterboxdIdB));
  }

  /**
   * Compare new Diary LID to existing list and add it while keeping things sorted and limiting the
   * total size of the list
   */
  public static ArrayList<String> buildMostRecentList(
      ArrayList<String> list, String diaryLid, int count) {
    list.add(diaryLid);
    list = new ArrayList<String>(new HashSet<String>(list)); // Remove duplicates
    list.sort(LidComparer::compare);

    int fromIndex = max(0, list.size() - count);
    int toIndex = list.size();

    return new ArrayList<String>(list.subList(fromIndex, toIndex));
  }

  /**
   * Build a string where uppercase and lowercase characters are swapped to match Letterboxd string
   * sorting
   */
  private static String swapCase(String input) {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      stringBuilder.append(reverseCase(input.charAt(i)));
    }

    return stringBuilder.toString();
  }

  /** Make upper case if lower case. Make lower case if upper case. */
  private static char reverseCase(char c) {
    return Character.isLowerCase(c) ? Character.toUpperCase(c) : Character.toLowerCase(c);
  }
}
