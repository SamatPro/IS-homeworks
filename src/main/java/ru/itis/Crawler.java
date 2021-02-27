package ru.itis;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class Crawler {
    private static HashSet<String> links;
    private int i = 0;
    private static final String path = "src/main/resources/crawler4j/";
    private static final File index = new File("src/main/resources/crawler4j/index.txt");

    public Crawler() {
        links = new HashSet<>();
    }

    public void getPageLinks(String URL) {
        if (!links.contains(URL)) {
            try {
                links.add(URL);
                System.out.println(URL + " " + i);
                i++;

                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");

                File fileName = new File(path + i + ".txt");
                savePagesToArchive(document.text(), fileName);
                saveToIndex(i, URL);

                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"));
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
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
        new Crawler().getPageLinks("http://www.consultant.ru/");
        System.out.println(links.size());
    }
}
