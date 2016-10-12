/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import ContainerClasses.AnswerGroups;
import edu.stanford.nlp.trees.Tree;
import java.util.Iterator;
import ContainerClasses.SentenceAndTag;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.item.IWordID;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AnnTherese
 */
public class SummarizeText {
    
    
    
    public static void StoreImportantSentence(SentenceAndTag sentence,int questionIndexNum){
        
        Iterator it;
        Tree tree = sentence.getTree();
        Tree temp = null;
        String POStag = null;
        
        for(it = tree.iterator(); it.hasNext();){
            POStag = temp.value();
            temp = (Tree) it.next();
            
            if(temp.isLeaf()){
                if(POStag.matches("NN(S|P|PS)?|RB(R|S)?|JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    getWordInfo(temp.value(), POStag, sentence.getSentence());
                }
            }
            
        }
        
    }
    
    public static void summarize(String string){
        
        string = TextFilePreProcess.removeCarets(string);
        String[] stringArray = string.split("\\r?\\n");
        
        AnswerGroups taggedAnswers = new AnswerGroups();
        
        int cntQuestion = 0;
        
        for(int i = 0; i < stringArray.length; i++){
            if(TextFilePreProcess.ifQuestion(stringArray[i])){
                i++;
                cntQuestion++;
                while(i < stringArray.length && !TextFilePreProcess.ifQuestion(stringArray[i])){
                    
                    List<Sentence> sents = LanguageProcess.getSentences(stringArray[i]);
                    for(Sentence sent : sents){
                        String rawSent =  sent.text();
                        Tree sentTree = sent.parse();
                        SentenceAndTag sentandTag = new SentenceAndTag(rawSent, sentTree);
                        taggedAnswers.setList(cntQuestion, sentandTag);
                    }
                    
                    i++;
                }
            }
        }
        
    }
    
    public static void getWordInfo(String word, String POStag, String sentence){
        
        List<IWordID> wordIDs = WordNetAccess.getSenses(word, POStag);
        
    }
    
    private static void write(Tree tree){
        
        System.out.println(tree);
        
    }
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
}
