package org.kramadi.bot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import java.io.FileWriter;
import java.io.IOException;


public class TestNashornJsoup {

    public static void main(String[] args) {

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName("nashorn");


        try (FileWriter writer = new FileWriter("notes4.txt", false)){

            Document doc = Jsoup.connect("https://tender.uub.com.ua/PositionList.aspx?&page=1&filter_type=filter&fvFTWord_0=%D0%BC%D0%B0%D1%81%D0%BB%D0%BE&action=Y")
                    .timeout(10000)
                    .userAgent("Chrome/95.0.4638.69 Safari/537.36")
                    .get();
            Elements body = doc.select("#pnList > div");
            String title = body.select("div.col-12.font-weight-bold.text-success > span").text();
            System.out.println(title);
            String text = title;
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
