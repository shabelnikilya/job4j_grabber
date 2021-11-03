package utils;

import java.time.*;

public class SqlRuDateTimeParser implements DateTimeParser {

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


    public static int getMonth(String in) {
        return switch (in) {
            case ("янв") -> 1;
            case ("фев") -> 2;
            case ("мар") -> 3;
            case ("апр") -> 4;
            case ("май") -> 5;
            case ("июн") -> 6;
            case ("июл") -> 7;
            case ("авг") -> 8;
            case ("сен") -> 9;
            case ("окт") -> 10;
            case ("ноя") -> 11;
            case ("дек") -> 12;
            default -> throw new IllegalArgumentException("Wrong month");
        };
    }

    public static LocalDate getDateWhenOneWordDay(String in) {
        validOneWordDate(in);
        return in.equals("сегодня") ? LocalDate.now() : LocalDate.now().minusDays(1);
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
