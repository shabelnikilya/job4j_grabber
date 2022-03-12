package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Exp {

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.binance.com/ru/nft/collection?tradeType=0&currency=BUSD&orderBy=amount_"
                + "sort&orderType=1&isBack=1&id=518648719579475968&order=amount_sort%401")
                .userAgent("Mozilla").get();

//        Document doc = Jsoup.parse(new File("https://www.binance.com/ru/nft/collection?"
//                + "tradeType=0&currency=BUSD&orderBy=amount_sort&orderType=1&isBack=1&id"
//                +"=518648719579475968&order=amount_sort%401"), "utf-8");
//        Elements el = doc.getElementsByClass("css-rjqmed");
//        System.out.println(el.size());
//        for (Element e : el) {
//            System.out.println(e.className());
//        }
//        Element el = doc.select("css-rjqmed").first();
//        System.out.println(el == null);
        String rsl = doc.html();
        System.out.println(rsl.contains("295"));
//        try (BufferedOutputStream out = new BufferedOutputStream(
//                new FileOutputStream("C:\\Users\\shabe\\OneDrive\\Рабочий стол\\it\\parse.txt")
//        )) {
//            out.write(rsl.getBytes(StandardCharsets.UTF_8));
//        }
        //System.out.println(rsl);
    }
}
