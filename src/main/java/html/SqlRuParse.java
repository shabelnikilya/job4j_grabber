package html;

import grabber.Parse;
import grabber.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DateTimeParser;
import utils.SqlRuDateTimeParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SqlRuParse implements Parse {

    private final DateTimeParser dateTimeParser;
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class.getName());

    public SqlRuParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static Post getPost(Element el) {
        String link = el.attr("href");
        Document doc = null;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException io) {
            LOG.error("I/O exception in method - getPost", io);
        }
        DateTimeParser dateToObject = new SqlRuDateTimeParser();
        return new Post(el.text(), link,
                        getDescription(Objects.requireNonNull(doc)),
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

    @Override
    public List<Post> list(String link) {
        List<Post> rsl = new ArrayList<>();
        Document doc = null;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException io) {
            LOG.error("I/O exception in method - list", io);
        }
        Elements row = Objects.requireNonNull(doc).select(".postslisttopic");
        row.forEach(x -> rsl.add(getPost(x.child(0))));
        return rsl;
    }

    @Override
    public Post detail(String link) {
        Document doc = null;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException io) {
            LOG.error("I/O exception in method - detail", io);
        }
        Elements e = Objects.requireNonNull(doc).select(".messageHeader");
        DateTimeParser dateToObject = new SqlRuDateTimeParser();
        String namePost = Objects.requireNonNull(e.stream().findFirst().orElse(null)).text();
        return new Post(namePost.substring(0, namePost.length() - 6),
                        link,
                        getDescription(doc),
                        dateToObject.parse(getDateWithSite(doc)
                        ));
    }
}

