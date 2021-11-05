package html;

import grabber.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.DateTimeParser;
import utils.SqlRuDateTimeParser;
import java.io.IOException;
import java.util.Objects;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        for (int i = 1; i < 6; i++) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + i).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                System.out.println(getPost(href));
            }
        }
    }

    public static Post getPost(Element el) throws IOException {
        String link = el.attr("href");
        Document doc = Jsoup.connect(link).get();
        DateTimeParser dateToObject = new SqlRuDateTimeParser();
        return new Post(el.text(), link,
                        getDescription(doc),
                        dateToObject.parse(getDateWithSite(doc)));
    }

    public static String getDescription(Document link) {
        Elements row = link.select(".msgBody");
        Element parent = row.stream().skip(1).findFirst().orElse(null);
        return Objects.requireNonNull(parent).text();
    }

    public static String getDateWithSite(Document link) {
        Elements w = link.select(".msgFooter");
        Element p = w.stream().findFirst().orElse(null);
        return splitString(Objects.requireNonNull(p).text());
    }

    public static String splitString(String date) {
        StringBuilder str = new StringBuilder(date);
        int index = str.indexOf(":");
        return str.substring(0, index + 3);
    }
}

