package ru.kpfu.chernyshevaks;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import ru.kpfu.chernyshevaks.crawlers.AppCrawler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {

    private static Properties properties;

    static {
        InputStream is = Application.class.getClassLoader().getResourceAsStream("application.properties");
        properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        File crawlStorage = new File(properties.getProperty("crawler4j.path"));
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorage.getAbsolutePath());

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed(args[0]);

        CrawlController.WebCrawlerFactory<AppCrawler> factory = () -> new AppCrawler(
                args[0],
                properties.getProperty("pages.path"),
                properties.getProperty("pages.index"),
                Integer.parseInt(properties.getProperty("pages.minword"))
        );

        controller.start(factory, Integer.parseInt(properties.getProperty("crawler4j.number")));
    }
}
