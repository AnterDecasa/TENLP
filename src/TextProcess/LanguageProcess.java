/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import ContainerClasses.LemmaSentenceWithPOStag;
import static TextProcess.TextFilePreProcess.ifQuestion;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import java.util.ArrayList;
import java.util.List;

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
    
    public static Tree getPOS(Sentence sent){
        return sent.parse();
    }
    
    public static List<String> getNER(String string){
        
        List<String> retVal = new ArrayList<String>();
        
        Document doc = new Document(string);
        for (Sentence sent : doc.sentences()) {
            retVal = sent.nerTags();
        }
         
        return retVal;
        
    }
    
    public static List<Sentence> getSentences(String string){
        
        Document doc = new Document(string);
        
        return doc.sentences();
        
    }
    
//    public static POS GetPOSTag(String POSTag){
//        
//        POS pos = null;
//        
//        if(POSTag.matches("JJ(R|S)?")){
//            pos = POS.ADJECTIVE;
//        }
//        if(POSTag.matches("(NN)S?")){
//            pos = POS.NOUN;
//        }
//        if(POSTag.matches("RB(S|R)?")){
//            pos = POS.ADVERB;
//        }
//        if(POSTag.matches("VB(D|G|N|P|Z)?")){
//            pos = POS.VERB;
//        }
//        
//        return pos;
//        
//    }
    
}
