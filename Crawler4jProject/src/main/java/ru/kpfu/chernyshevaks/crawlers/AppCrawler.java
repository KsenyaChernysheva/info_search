package ru.kpfu.chernyshevaks.crawlers;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class AppCrawler extends WebCrawler {

    private final static Pattern EXCLUSIONS
            = Pattern.compile(".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");
    private static int pagesCount = 0;

    private String seedUrl;
    private String dirPath;
    private String fileName;
    private int minWordCount;

    public AppCrawler(String url, String dirPath, String fileName, int minWordCount) {
        this.seedUrl = url;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.minWordCount = minWordCount;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String urlString = url.getURL().toLowerCase();
        return !EXCLUSIONS.matcher(urlString).matches()
                && urlString.startsWith(seedUrl);
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        if (page.getParseData() instanceof HtmlParseData) {
            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String text = doc.body().getElementsMatchingOwnText("[А-Яа-я]+")
                    .stream().map(Element::text).reduce((x, y) -> x + " " + y).get();
            if (text.split("\\s+").length < minWordCount) return;
            File dir = new File(dirPath);
            if (!dir.isDirectory() || !dir.exists()) throw new IllegalArgumentException("Property init error");
            File pageFile = new File(dir.getAbsolutePath() + "/" + pagesCount + ".txt");
            File indexFile = new File(dir.getAbsolutePath() + "/" + fileName);
            try {
                pageFile.createNewFile();
                FileWriter writer = new FileWriter(pageFile);
                writer.write(text);
                writer.close();
                if (!indexFile.exists()) indexFile.createNewFile();
                FileWriter writer2 = new FileWriter(indexFile, true);
                writer2.write(pagesCount + ": " + url + "\n");
                writer2.close();
                pagesCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
