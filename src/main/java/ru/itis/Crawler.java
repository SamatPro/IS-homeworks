package ru.itis;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class Crawler {
    private static HashSet<String> links;
    private int i = 0;
    private static final String path = "src/main/resources/crawler4j/";
    private static final File index = new File("src/main/resources/crawler4j/index.txt");
    private String[] denyListPattern = {
        "remember.me", "register", "search", "characters", "li.ru",
            "yandex.ru", "aminis", "telegram.org", "download",
            "shortwork", "biography", "material/add", "contacts",
            "login", "Dekster", "account"
    };

    public Crawler() {
        links = new HashSet<>();
    }

    public void getPageLinks(String URL) {
        if (isAllowed(URL)) {
            try {
                links.add(URL);
                System.out.println(URL + " " + i);

                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");

                File fileName = new File(path + i + ".txt");
                savePagesToArchive(document.text(), fileName);
                int wordCount = document.text().split(" ").length;
                if (wordCount < 1000){
                    inner(linksOnPage, fileName, wordCount);
                }
                i++;
                saveToIndex(i, URL);

                for (Element page : linksOnPage){
                    getPageLinks(page.attr("abs:href"));
                }


            } catch (Exception e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
        if (i >= 100){
            System.exit(0);
        }
    }

    private void inner(Elements linksOnPage, File fileName, int wordCount) throws IOException {
        Elements linksOnPageInner = null;
        for (Element page : linksOnPage) {
            String innerUrl = page.attr("abs:href");
            if (isAllowed(innerUrl)) {
                links.add(innerUrl);
                System.out.println("Inner -- " + innerUrl);
                Document documentInner = Jsoup.connect(innerUrl).get();
                linksOnPageInner = documentInner.select("a[href]");
                savePagesToArchive(documentInner.text(), fileName);
                wordCount += documentInner.text().split(" ").length;
                if (wordCount >= 1000){
                    break;
//                    getPageLinks(innerUrl);
                }
            }
        }
        if (wordCount<1000){
            inner(linksOnPageInner, fileName, wordCount);
        }

    }

    private boolean isAllowed(String URL){
        return !links.contains(URL) &&
                !Arrays.stream(denyListPattern).anyMatch(deny -> URL.contains(deny)) &&
                !URL.equals("http://litra.ru/");
    }

    private void savePagesToArchive(String text, File fileName) {
        try {
            FileUtils.writeStringToFile(fileName, text, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToIndex(int docNumber, String link) {
        String fullLine = docNumber + " " + link + "\n";
        try {
            FileUtils.writeStringToFile(index, fullLine, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Crawler().getPageLinks("http://www.litra.ru/fullwork/work/wrid/00040601184773068612/");
        System.out.println(links.size());
    }
}
