package ru.kpfu.itis;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ListEvaluator  extends AbstractEvaluator<List<Integer>> {

    /** The negate unary operator.*/
    public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);
    /** The logical AND operator.*/
    private static final Operator AND = new Operator("&", 2, Operator.Associativity.LEFT, 2);
    /** The logical OR operator.*/
    public final static Operator OR = new Operator("|", 2, Operator.Associativity.LEFT, 1);

    private static final Parameters PARAMETERS;

    static {
        PARAMETERS = new Parameters();
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        PARAMETERS.add(NEGATE);
    }

    public ListEvaluator() {
        super(PARAMETERS);
    }

    protected List<Integer> toValue(String s, Object o) {
        TokenStream stream = InvertedIndexApplication.analyzer.tokenStream("field", s);
        String lemma = "";
        try {
            stream.reset();
            stream.incrementToken();
            lemma = stream.getAttribute(CharTermAttribute.class).toString();
            stream.end();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<Integer> res = InvertedIndexApplication.invertedIndex.get(lemma).keySet();
        if(res != null) {
            return Arrays.asList(res.toArray(new Integer[]{}));
        }
        else return new ArrayList<>();
    }

    @Override
    protected List<Integer> evaluate(Operator operator, Iterator<List<Integer>> operands, Object evaluationContext) {
        List<Integer> result;
        if (operator == NEGATE) {
            List<Integer> o = operands.next();
            List<Integer> all = Arrays.stream(IntStream.rangeClosed(1, InvertedIndexApplication.docMaxNumber)
                    .toArray()).boxed().collect(Collectors.toList());
            all.removeAll(o);
            result = all;
        } else if (operator == OR) {
            List<Integer> o1 = operands.next();
            List<Integer> o2 = operands.next();
            result = Stream.concat(o1.stream(), o2.stream())
                    .distinct()
                    .collect(Collectors.toList());
        } else if (operator == AND) {
            List<Integer> o1 = operands.next();
            List<Integer> o2 = operands.next();
            result = o1.stream()
                    .filter(o2::contains)
                    .collect(Collectors.toList());
        } else {
            return super.evaluate(operator, operands, evaluationContext);
        }
        result.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        return result;
    }
}
