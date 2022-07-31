package org.kramadi.bot.look4tender;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Selector {

    public static String getHtml(Document doc) {
        return doc.html();
    }

    public static Elements select(Document document, String select) {
        return document.select(select);
    }

    public static Elements select(Elements elements, String select) {
        return elements.select(select);
    }

}
