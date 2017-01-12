/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

<<<<<<< HEAD
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.Polarity;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
=======
import ContainerClasses.LemmaSentenceWithPOStag;
import static TextProcess.TextFilePreProcess.ifQuestion;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
>>>>>>> refs/remotes/origin/master
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
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
    
    public static Tree getPOS(String sent){
        
        Sentence sentence = new Sentence(sent);
        
        return sentence.parse();
        
    }
    
    public static List<String> getNER(String string){
        
        List<String> retVal = new ArrayList<String>();
        
        Document doc = new Document(string);
        for (Sentence sent : doc.sentences()) {
            retVal = sent.nerTags();
        }
         
        return retVal;
        
    }
    
<<<<<<< HEAD
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
=======
    public static List<Sentence> getSentences(String string){
        
        Document doc = new Document(string);
        
        return doc.sentences();
        
    }
    
    public static POS GetPOSTag(String POSTag){
        
        POS pos = null;
        
        if(POSTag.matches("JJ(R|S)?")){
            pos = POS.ADJECTIVE;
        }
        if(POSTag.matches("(NN)S?")){
            pos = POS.NOUN;
        }
        if(POSTag.matches("RB(S|R)?")){
            pos = POS.ADVERB;
        }
        if(POSTag.matches("VB(D|G|N|P|Z)?")){
            pos = POS.VERB;
        }
        
        return pos;
        
    }
>>>>>>> refs/remotes/origin/master
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
    private static void write(Tree tree){
        
        System.out.println(tree);
        
    }
    
}
