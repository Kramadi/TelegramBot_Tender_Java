package org.kramadi.bot.look4tender;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;

public class Connector {
    //For GET queries
    public static Document getDoc(String url) {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(10000)
                    .userAgent("Chrome/95.0.4638.69 Safari/537.36")
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    //For POST queries
    public static Document getDoc(String url, HashMap<String, String> data) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(40000).data(data).post();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
