package ru.kpfu.itis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.*;

public class VectorSearchApplication {

    public static void main(String[] args) throws IOException {
        InvertedIndexApplication.init();
        TfIdfApplication.initTfIdfMap();
        Map<String, Map<Integer, Double>> tfIdfMap = InvertedIndexApplication.tfIdfMap;
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("Введите поисковой запрос:");
            String line = sc.nextLine();
            System.out.println("Результаты поиска:");
            List<String> lineWords = Arrays.asList(line.split("\\s"));
            Set<Integer> relevantDoc = new TreeSet<>(Comparator.comparingInt(o -> o));
            for(String word: lineWords){
                relevantDoc.addAll(tfIdfMap.get(word) != null ? tfIdfMap.get(word).keySet() : Collections.emptyList());
            }
            double[] vector = new double[lineWords.size()];
            for(int i = 0; i < lineWords.size(); i++){
                String word = lineWords.get(i);
                double idf = Optional.ofNullable(InvertedIndexApplication.wordIdfMap.get(word)).orElse(0d);
                vector[i] = idf * TfIdfApplication.tf(1, lineWords.size());
            }
            Map<Integer, Double> docSimilarityMap = new TreeMap<>();
            for(Integer docNum: relevantDoc){
                double[] docVectorProjection = new double[lineWords.size()];
                for(int i = 0; i < lineWords.size(); i++){
                    String word = lineWords.get(i);
                    if(tfIdfMap.get(word) == null){
                        docVectorProjection[i] = 0;
                    }
                    else {
                        docVectorProjection[i] = tfIdfMap.get(word).get(docNum) != null ? tfIdfMap.get(word).get(docNum) : 0;
                    }
                }
                double[] docVector = new double[InvertedIndexApplication.docWords.get(docNum)];
                int i = 0;
                for(String word: tfIdfMap.keySet()){
                    if(tfIdfMap.get(word).get(docNum) != null){
                        docVector[i] = tfIdfMap.get(word).get(docNum);
                        i++;
                    }
                }
                docSimilarityMap.put(docNum, cosineSimilarity(vector, docVectorProjection, docVector));
            }
            docSimilarityMap.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .forEach(s -> System.out.println(s.getKey() + ".txt [sim=" + s.getValue() + "]"));
            System.out.println("\n---------------");
        }
    }

    public static String format(String l){
        return l.replaceAll("[^А-Яа-яA-Za-z\\s]", "");
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorBProjection, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorBProjection[i];
            normA += Math.pow(vectorA[i], 2);
        }
        for (int i = 0; i < vectorB.length; i++) {
            normB += Math.pow(vectorB[i], 2);
        }
        double q = (Math.sqrt(normA) * Math.sqrt(normB));
        if(q == 0){
            return 0.0;
        }
        else return (dotProduct / q);
    }
}
