package ru.kpfu.itis;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.morphology.russian.RussianAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class LemmatizationApplication {

    public static Integer docCount = 0;
    private static Properties properties;

    static {
        InputStream is = LemmatizationApplication.class.getClassLoader().getResourceAsStream("application.properties");
        properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int i = 0;
        String data = "";
        while(true) {
            try {
                File file = new File(properties.getProperty("pages.path") + "\\" + i + ".txt");
                System.out.println(file.getName());
                String text = FileUtils.readFileToString(file, "utf-8");
                Analyzer analyzer = new RussianAnalyzer();
                TokenStream stream = analyzer.tokenStream("field", text);
                stream.reset();
                StringBuilder res = new StringBuilder();
                while (stream.incrementToken()) {
                    String lemma = stream.getAttribute(CharTermAttribute.class).toString();
                    if(!lemma.matches("[0-9]+") && lemma.length() != 1)
                        res.append(lemma).append(" ");
                }
                stream.end();
                stream.close();
                i++;
                data+= res.toString();
            } catch (FileNotFoundException e) {
                File resultFile = new File(properties.getProperty("lemmas.path"));
                FileUtils.writeStringToFile(resultFile, data, "utf-8");
                docCount = i;
                break;
            }
        }
    }
}
//    public static String lemmatize(String l) throws IOException {
//        Analyzer analyzer = InvertedIndexApplication.analyzer;
//        TokenStream stream = analyzer.tokenStream("field", l);
//        stream.reset();
//        StringBuilder res = new StringBuilder();
//        while (stream.incrementToken()) {
//            String lemma = stream.getAttribute(CharTermAttribute.class).toString();
//            res.append(lemma).append(" ");
//        }
//        stream.end();
//        stream.close();
//        return res.toString();
//    }