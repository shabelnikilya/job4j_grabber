package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        for (int i = 1; i < 6; i++) {
            Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + i).get();
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element par = td.parent();
                Element href = td.child(0);
                System.out.println("Название вакансии - " + href.text());
                System.out.println("Ссылка на вакансию - " + href.attr("href"));
                System.out.println("Дата опубликования вакансии - " + par.children().get(5).text());
            }
        }
    }
}

