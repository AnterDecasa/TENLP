/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import ContainerClasses.AnswerGroups;
import edu.stanford.nlp.trees.Tree;
import java.util.Iterator;
import ContainerClasses.LemmaSentenceWithPOStag;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Synset;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;
import edu.mit.jwi.item.POS;

/**
 *
 * @author AnnTherese
 */
public class SummarizeText {
    
    static AnswerGroups groupedAnswers;
    static String[] compareWords;
    
    public static void summarize(String string){
        
        string = TextFilePreProcess.removeCarets(string);
        String[] stringArray = string.split("\\r?\\n");
        
        groupedAnswers = groupAnswers(stringArray);
        
            //Get sentimentScore for each answer
            int answerIndex = 0;
            for(Document answer : groupedAnswers.getTeachStrength()){ 
                double sentimentScore = GetSentiment(answer,groupedAnswers.getTeachStrength(),answerIndex, 0);
                answerIndex++;
            }
        
    }
    
    public static double GetSentiment(Document answer, List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
        LemmaSentenceWithPOStag cleanedLemmaAnswerWithPOSTag = getCleanedLemmaSentence(answer);
        List<LemmaSentenceWithPOStag> cleanedLemmaCompareToAnswerWithPOSTag = new ArrayList<LemmaSentenceWithPOStag>();
        
        //Get lemma for all the rest of the answers
        for(Document onceCompareToGroupedAnswers : compareToGroupedAnswers){
            cleanedLemmaCompareToAnswerWithPOSTag.add(getCleanedLemmaSentence(onceCompareToGroupedAnswers));
        }
        
        //Get sense for each word
        WordNetAccess.loadDic();
        int index = 0;
        for(String cleanedSentence : cleanedLemmaAnswerWithPOSTag.cleanedLemmaAnswers){
            IIndexWord indexWord = WordNetAccess.dict.getIndexWord(cleanedSentence, LanguageProcess.GetPOSTag(cleanedLemmaAnswerWithPOSTag.cleanedLemmaAnswersPOSTag.get(index)));
            List<IWordID> wordIDs = indexWord.getWordIDs();
            if(wordIDs.size() > 1){
                //Disambiguate
                //getCompareToWords
                LemmaSentenceWithPOStag cleanedAnswerWithTag = cleanedLemmaCompareToAnswerWithPOSTag.get(answerIndex);
                if(index < 5){
                    
                }
                //checkIfWord has enough words before it.
                
            }
            index++;
        }
        
        double sentimentScore = 0;

        return sentimentScore;
        
    }
    
    public static LemmaSentenceWithPOStag getCleanedLemmaSentence(Document document){
        
        List<Sentence> sentencesAnswer = document.sentences();
        LemmaSentenceWithPOStag retVal = new LemmaSentenceWithPOStag();
        
        for(Sentence oneSentenceAnswer : sentencesAnswer){
                
                Tree sentenceTree = oneSentenceAnswer.parse();
                
                Iterator trav = sentenceTree.iterator();
                Tree childTree = sentenceTree;
                String POSTag = "";
                int wordIndex = 0;
      
                while(trav.hasNext()){
                    POSTag = childTree.value();
                    childTree = (Tree) trav.next();
                    if(childTree.isLeaf()){
                        if(POSTag.matches("NN(S|P|PS)?|RB(R|S)?|JJ(R|S)?|VB(D|G|N|P|Z)?")){
                            if(IfUniqueWord(retVal.cleanedLemmaAnswers, retVal.cleanedLemmaAnswersPOSTag, oneSentenceAnswer.lemma(wordIndex),POSTag)){
                                retVal.cleanedLemmaAnswers.add(oneSentenceAnswer.lemma(wordIndex));
                                retVal.cleanedLemmaAnswersPOSTag.add(POSTag);
                            }
                        }
                    }
                }
            
            }
        
        return retVal;
        
    }
    
    public static boolean IfUniqueWord(List<String> words,List<String> POS, String word, String POSofWord){
        
        boolean retVal = true;
        
        int index = 0;
        for(; index < words.size(); index++){
            if(words.get(index).equalsIgnoreCase(word) && POS.get(index).equalsIgnoreCase(POSofWord)){
                retVal = false;
                break;
            }
        }
        
        return retVal;
    }
    
    public static void Disambiguate(List<IWordID> senses, String[] compareToWords){
        
        
        
    }
    
    public static void Lesk(String word, String[] compareTo){ 
        
    }
    
    public static Tree GetSentenceTree(){
        
        Tree retTree = null;
        
        
        
        return retTree;
                
    }
    
    private static AnswerGroups groupAnswers(String[] answers){
        
        AnswerGroups answerGroups = new AnswerGroups();
        
        int cntQuestion = 0;
        
        for(int i = 0; i < answers.length; i++){
            if(TextFilePreProcess.ifQuestion(answers[i])){
                i++;
                cntQuestion++;
                while(i < answers.length && !TextFilePreProcess.ifQuestion(answers[i])){
                    
                    answerGroups.asssignToGroup(cntQuestion, new Document(answers[i]));
                    
                    i++;
                }
            }
        }
        
        return answerGroups;
        
    }
    
    /*
    //Test method
    public static void StoreImportantSentences(){
        
        Iterator it;
        //Tree tree = sentence.getTree();
        Sentence sentence = new Sentence("The pine cone is cute.");
        Tree tree = sentence.parse();
        
        Tree temp = null;
        String POStag = null;
        temp = tree;
        int wordIndex = 0;
        for(it = tree.iterator(); it.hasNext();){
            
            write(temp);
            
            POStag = temp.value();
            temp = (Tree) it.next();
            
            if(temp.isLeaf()){
                write("Leaf tag:" + POStag);
                write("Word Index: " + wordIndex);
                
                if(POStag.matches("NN(S|P|PS)?|RB(R|S)?|JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    getWordInfo(sentence.lemma(wordIndex), POStag, sentence.text(),1);
                }
                
                wordIndex++;
            }
            
        }
        
    }
    
    public static void StoreImportantSentences(SentenceAndTag sentence,int questionIndexNum){
        
        Iterator it;
        Tree tree = sentence.getTree();
        
        Tree temp = null;
        String POStag = null;
        
        int wordIndex = 0;
        for(it = tree.iterator(); it.hasNext();){
            POStag = temp.value();
            temp = (Tree) it.next();
            
            if(temp.isLeaf()){
                if(POStag.matches("NN(S|P|PS)?|RB(R|S)?|JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    getWordInfo(new Sentence(sentence.getSentence()).lemma(wordIndex), POStag, sentence.getSentence(), questionIndexNum);
                }
                wordIndex++;
            }  
        }
        
    }
    
    public static void getWordInfo(String word, String POStag, String sentence, int questionIndexNum){
        
        List<IWordID> correctTagWordIDs = WordNetAccess.getSense(word, POStag);   
        
        if(correctTagWordIDs.size() > 1){
            //start Lesk algo
            Sentence sentForLesk = new Sentence(sentence);
            for(String sentWord : sentForLesk.words()){
                List<IWordID> wordSet =  WordNetAccess.getSense(sentWord, sentence);
                for(IWordID wordInSet : wordSet){;
                    IWord wordFromDict = WordNetAccess.dict.getWord(wordInSet);
                    String glossOfWord = wordFromDict.getSynset().getGloss();
                    write(glossOfWord);
                }
            }
            
            //end Lesk algo
        }
        
    }*/
    
    private static void write(Tree tree){
        
        System.out.println(tree);
        
    }
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
}
