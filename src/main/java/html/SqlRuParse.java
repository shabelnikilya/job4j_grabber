package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Objects;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        for (int i = 1; i < 6; i++) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + i).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element par = td.parent();
                Element href = td.child(0);
                Document docPost = Jsoup.connect(href.attr("href")).get();
                System.out.println("Название вакансии - " + href.text());
                System.out.println("Ссылка на вакансию - " + href.attr("href"));
                System.out.println("Дата опубликования вакансии - " + par.children().get(5).text());
                System.out.println(getDescription(docPost));
                System.out.println(getDateCreatedPost(docPost));
            }
        }
    }

    public static String getDescription(Document link) {
        Elements row = link.select(".msgBody");
        Element parent = row.stream().skip(1).findFirst().orElse(null);
        return Objects.requireNonNull(parent).text();
    }

    public static String getDateCreatedPost(Document link) {
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

