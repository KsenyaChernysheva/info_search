package ru.kpfu.itis;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.morphology.russian.RussianAnalyzer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class InvertedIndexApplication {

    private static Properties properties;
    protected static Map<String, Map<Integer, Integer>> invertedIndex = new HashMap<String, Map<Integer, Integer>>();
    protected static Map<Integer, Integer> docWords = new HashMap<>();
    protected static Map<String, Double> wordIdfMap = new HashMap<>();
    protected static Map<String, Map<Integer, Double>> tfIdfMap = new HashMap<>();
    protected static int docMaxNumber = 0;
    protected static int docCount = 0;
    protected static Analyzer analyzer;

    static {
        try {
            analyzer = new RussianAnalyzer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = LemmatizationApplication.class.getClassLoader().getResourceAsStream("application.properties");
        properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        init();
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("Введите запрос:");
            String line = sc.nextLine();
            ListEvaluator evaluator = new ListEvaluator();
            System.out.println("Результаты поиска:");
            try{
                System.out.println(evaluator.evaluate(line));
            }
            catch (Exception e){
                System.err.println("Произошла ошибка при парсинге выражения.");
            }
            System.out.println("--------------");
        }
    }

    public static void init() throws IOException {

        File dir = new File(properties.getProperty("pages.path"));
        for(File file: dir.listFiles()){
            if(file.isFile()) {
                indexFile(file);
            }
        }
    }

    private static void indexFile(File file) throws IOException {
        String text = FileUtils.readFileToString(file, "utf-8");
        if (file.getName().equals("index.txt")) return;
        Integer fileName = Integer.valueOf(file.getName().replace(".txt", ""));
        TokenStream stream = analyzer.tokenStream("field", text);
        stream.reset();
        int countWords = 0;
        while (stream.incrementToken()) {
            String lemma = stream.getAttribute(CharTermAttribute.class).toString();
            if(!lemma.matches("[0-9]+") && lemma.length() != 1) {
                if(invertedIndex.get(lemma) == null){
                    invertedIndex.put(lemma, new TreeMap<>(new Comparator<Integer>() {
                        public int compare(Integer o1, Integer o2) {
                            return o1-o2;
                        }
                    }));
                }
                if(invertedIndex.get(lemma).get(fileName) == null){
                    invertedIndex.get(lemma).put(fileName, 0);
                }
                invertedIndex.get(lemma).put(fileName, invertedIndex.get(lemma).get(fileName) + 1);
            }
            countWords++;
        }
        if(docMaxNumber < fileName) docMaxNumber = fileName;
        docCount++;
        docWords.put(fileName, countWords);
        stream.end();
        stream.close();
    }

}
