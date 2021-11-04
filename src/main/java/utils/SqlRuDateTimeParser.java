package utils;

import java.time.*;
import java.util.Map;

public class SqlRuDateTimeParser implements DateTimeParser {

    private static final Map<String, Integer> MONTHS = Map.ofEntries(
                            Map.entry("янв", 1),
                            Map.entry("фев", 2),
                            Map.entry("мар", 3),
                            Map.entry("апр", 4),
                            Map.entry("май", 5),
                            Map.entry("июн", 6),
                            Map.entry("июл", 7),
                            Map.entry("авг", 8),
                            Map.entry("сен", 9),
                            Map.entry("окт", 10),
                            Map.entry("ноя", 11),
                            Map.entry("дек", 12)
    );

    @Override
    public LocalDateTime parse(String parse) {
        validParse(parse);
        String[] splitParse = parse.split(",");
        String[] daysAndMounthAndYear = splitParse[0].split(" ");
        int hours = stringToInt(splitParse[1].split(":")[0].substring(1));
        int minutes = stringToInt(splitParse[1].split(":")[1]);
        LocalTime time = LocalTime.of(hours, minutes);
        LocalDate date = daysAndMounthAndYear.length == 1 ? getDateWhenOneWordDay(daysAndMounthAndYear[0])
                : getDateWhenFullDate(daysAndMounthAndYear[0], daysAndMounthAndYear[1], daysAndMounthAndYear[2]);
        return LocalDateTime.of(date, time);
    }

    public static int getMonth(String month) {
        return MONTHS.get(month);
    }

    public static LocalDate getDateWhenOneWordDay(String days) {
        validOneWordDate(days);
        return days.equals("сегодня") ? LocalDate.now() : LocalDate.now().minusDays(1);
    }

    public static LocalDate getDateWhenFullDate(String days, String month, String year) {
        int readFromSiteYear = stringToInt(year);
        int absYear = readFromSiteYear > LocalDate.now().getYear() - 2000
                ? readFromSiteYear + 1900 : readFromSiteYear + 2000;
        return LocalDate.of(absYear, getMonth(month), stringToInt(days));
    }

    public static int stringToInt(String in) {
        return Integer.parseInt(in);
    }

    public static void validParse(String parse) {
        if (parse == null || !parse.contains(",") || parse.split(",").length != 2) {
            throw new IllegalArgumentException("There is no readable data or it has an incorrect form");
        }
        if (parse.split(",")[1].split(":").length != 2) {
            throw new IllegalArgumentException("There is no readable time or it has an incorrect form");
        }
    }

    public static void validOneWordDate(String date) {
        if (!date.contains("сегодня") && !date.contains("вчера")) {
            throw new IllegalArgumentException("Wrong date");
        }
    }
}
