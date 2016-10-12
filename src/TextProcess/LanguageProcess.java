/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

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
    
    public static List<Tree> getPOS(String string){
        
        List<Tree> taggedSent = new ArrayList<Tree>();
        
        Document doc = new Document(string);
        for (Sentence sent : doc.sentences()) {
            taggedSent.add(sent.parse());
        }
        
        return taggedSent;
        
    }
    
    public static List<String> getNER(String string){
        
        List<String> retVal = new ArrayList<String>();
        
        Document doc = new Document(string);
        for (Sentence sent : doc.sentences()) {
            retVal = sent.nerTags();
        }
         
        return retVal;
        
    }
    
    public static void getWordInfo(String word, String POStag){
        
        
        
    }
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
    private static void write(Tree tree){
        
        System.out.println(tree);
        
    }
    
}
