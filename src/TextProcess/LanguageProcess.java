/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.Polarity;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author anter_000
 */
public class LanguageProcess {
    
    public static List<String> getWords(String string){
        
        List<String> retVal = new ArrayList<>();
        
        Document doc = new Document(string);
         for (Sentence sent : doc.sentences()) {
            retVal = sent.words();
        }
         
        return retVal;
        
    }
    
    public static List<String> getLemma(String string){
        
        List<String> retVal = new ArrayList<>();
        
        Document doc = new Document(string);
         for (Sentence sent : doc.sentences()) {
            retVal = sent.words();
        }
         
        return retVal;
        
    }
    
    public static List<String> getPOS(String string){
        
        List<String> retVal = new ArrayList<>();
        
        Document doc = new Document(string);
         for (Sentence sent : doc.sentences()) {
            retVal = sent.words();
        }
         
        return retVal;
        
    }
    
    public static List<String> getNER(String string){
        
        List<String> retVal = new ArrayList<>();
        
        Document doc = new Document(string);
         for (Sentence sent : doc.sentences()) {
            retVal = sent.words();
        }
         
        return retVal;
        
    }
    
    public static List<Polarity[]> annotate(String text){
    
            StanfordCoreNLP pipeline = new StanfordCoreNLP(new Properties(){{
            setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog");
            setProperty("ssplit.isOneSentence", "true");
            setProperty("tokenize.class", "PTBTokenizer");
            setProperty("tokenize.language", "en");
            setProperty("enforceRequirements", "false");
            setProperty("natlog.neQuantifiers", "true");
            }});
    
            Annotation ann = new Annotation(text);
            pipeline.annotate(ann);
            List<CoreLabel> tokens = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(CoreAnnotations.TokensAnnotation.class);
            SemanticGraph tree = ann.get(CoreAnnotations.SentencesAnnotation.class).get(0).get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//            Polarity[] polarities = new Polarity[tokens.size()];
            List<Polarity[]> polarities = new ArrayList<>();
//            for (int i = 0; i < tokens.size(); ++i) {
////              polarities[i] = tokens.get(i).get(NaturalLogicAnnotations.PolarityAnnotation.class);
//            }
            Document doc = new Document(text);
            for(Sentence sent : doc.sentences()) {
                polarities = sent.words();
            }
    
            return polarities; 
            }
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
}
