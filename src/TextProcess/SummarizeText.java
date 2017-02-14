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
import ContainerClasses.StringAndTag;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.IRAMDictionary;
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
                double sentimentScore = GetSentiment(groupedAnswers.getTeachStrength(),answerIndex, 0);
                answerIndex++;
            }
            //TeachWeakness
            //Comments
    }
    
     public static double GetSentiment(List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
        write("Inside GetSentiment");
        
        double sentimentScore = 0;
        
        //Get sense for each word
        List<Sentence> answerSentences = compareToGroupedAnswers.get(answerIndex).sentences();
        int answerSentencesSize = answerSentences.size();
        try{
            IDictionary dictionary = WordNetAccess.loadDic();
            dictionary.open();
            for(int sentenceCtr = 0; sentenceCtr < answerSentencesSize; sentenceCtr++){
            
                Sentence currentSentence = answerSentences.get(sentenceCtr);
                int lemmaSize = currentSentence.lemmas().size();
            
                for(int lemmaCtr = 0; lemmaCtr < lemmaSize; lemmaCtr++){
                
                    String currentPOSTag = currentSentence.posTag(lemmaCtr);
                    if(currentPOSTag.matches("JJ|NN|VB|RB")){
                    
                        IIndexWord indexWord = dictionary.getIndexWord(currentSentence.lemma(lemmaCtr), LanguageProcess.GetPOSTag(currentPOSTag));
                        List<IWordID> wordIDs = indexWord.getWordIDs();
                        if(wordIDs.size() > 1){
                            //Disambiguate
                            int indexOfWordToBeUsed = Disambiguate(indexWord, compareToGroupedAnswers, answerIndex, sentenceCtr, lemmaCtr, questionIndex);
        
                        }
                    
                    }
                
                }
            }
        }
        catch(Exception exc){
            
        }
        
        
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
        
        write("Inside of IfUniqueWord");
        
        boolean retVal = true;
        
        int index = 0;
        for(; index < words.size(); index++){
            if(words.get(index).equalsIgnoreCase(word) && POS.get(index).equalsIgnoreCase(POSofWord)){
                retVal = false;
                break;
            }
        }
        
        write("Exiting IfUniqueWord");
        
        return retVal;
    }
    
    public static boolean IfUniqueWord(List<StringAndTag> listOfWords, String word){
        
        boolean retVal = false;
        
        for(StringAndTag currentWord : listOfWords){
            
            if(currentWord.word.compareToIgnoreCase(word) == 0){
                retVal = true;
            }
            
        }
        
        return retVal;
        
    }
    
    public static int Disambiguate(IIndexWord indexWord, List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
        write("Inside Disambiguate");
        
        WordNetAccess.loadDic();
        
        int wordSensesSize = indexWord.getWordIDs().size();
        int[] wordSenseScores = new int[wordSensesSize];
        List<IWordID> wordSenses = indexWord.getWordIDs();
        int indexOfWordToBeUsed = 0;
               
        //get word with most similar words in gloss
        
        //access each answer
        for(int wordSenseTraverseCtr = 0; wordSenseTraverseCtr < wordSensesSize; wordSenseTraverseCtr++){
            
            //get gloss for current wordSense from wordNet
            IWord word = WordNetAccess.dict.getWord(wordSenses.get(wordSenseTraverseCtr));
            String glossOfWordSense = word.getSynset().getGloss();
            
            List<StringAndTag> compareToWords = GetCompareToWords(compareToGroupedAnswers, answerIndex, sentenceIndex, lemmaIndex, questionIndex);
  
            for(StringAndTag wordWithTag : compareToWords){
                    
                IIndexWord indexWordOfNeighborWord = WordNetAccess.dict.getIndexWord(wordWithTag.word, wordWithTag.tag);
                List<IWordID> iWordIDsOfNeighborWord = indexWordOfNeighborWord.getWordIDs();
                
                int sameWordsCtr = 0;
                
                for(IWordID iWordIDOfNeighborWord : iWordIDsOfNeighborWord){
                    String oneGlossOfNeighborWord = WordNetAccess.dict.getWord(iWordIDOfNeighborWord).getSynset().getGloss();
                    
                    String[] glossOfWordSenseStringArray = glossOfWordSense.split(" ");
                    String[] oneGlossOfNeighborWordStringArray = oneGlossOfNeighborWord.split(" ");
                    for(String wordContainer1 : glossOfWordSenseStringArray){
                        for(String wordContainer2 : oneGlossOfNeighborWordStringArray){
                            wordContainer1 = wordContainer1.replaceAll("(|)|\"|;", "");
                            wordContainer2 = wordContainer2.replaceAll("(|)|\"|;", "");
                            if(wordContainer1.equalsIgnoreCase(wordContainer2) == true){
                                sameWordsCtr++;
                                break;
                            }
                        }
                        
                    }
                    
                }
                    
                wordSenseScores[wordSenseTraverseCtr] = sameWordsCtr;
            }
            
        }
        
        int largestCnt = wordSenseScores[0];
        for(int travCntr = 1; travCntr < wordSensesSize; travCntr++){
            if(largestCnt > wordSenseScores[travCntr]){
                indexOfWordToBeUsed = travCntr;
                largestCnt = wordSenseScores[travCntr];
            }
        }
        
        write("Exiting Disambiguate");
        
        return indexOfWordToBeUsed;
        
    }
    
    public static void Lesk(String word, String[] compareTo){ 
        
    }
    
//    public static List<String> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
//        
//        List<String> compareToWords = new ArrayList<String>();
//        int compareToWordsSize = 10;
//        boolean compareToWordListFull = false;
//        int compareToGroupedAnswersSize = compareToGroupedAnswers.size();
//        
//        if(answerIndex == 0){
//            if(sentenceIndex == 0){
//                if(lemmaIndex == 0){
//                    List<Sentence> answerSentences =  compareToGroupedAnswers.get(answerIndex).sentences(); 
//                    //access each sentence
//            
//                    for(int sentenceCtr = 0; sentenceCtr < answerSentences.size(); sentenceCtr++){
//                        int lemmaSize = answerSentences.get(sentenceCtr).length();
//                        for(int lemmaCtr = 0; lemmaCtr < lemmaSize; lemmaCtr++){
//                            Sentence currentSentence = answerSentences.get(sentenceCtr);
//                            if(currentSentence.posTag(lemmaCtr).matches("JJ|NN|VB|RB")){
//                                //get gloss for current lemma in sentence 
//                                compareToWords.add(currentSentence.lemma(lemmaCtr));
//                                if(compareToWords.size() == compareToWordsSize){
//                                    compareToWordListFull = true;
//                                    break;
//                                }
//                            }
//                        }
//                        if(compareToWordListFull){
//                            break;
//                        }
//                
//                    }
//                }
//            }
//            
//        }
//        else if(answerIndex > 0 && answerIndex < compareToGroupedAnswersSize){
//            
//            
//            
//        }
//        else{
//            
//        }
//        boolean firstSetDone = false;
//        while(!(answerIndex < 0)){
//            while(!(sentenceIndex < 0)){
//                while(!(lemmaIndex < 0)){
//                    
//                    
//                    
//                }
//            }
//        }
//        if(firstSetDone){
//            
//        }
//        
//        return compareToWords;
//        
//    }
    
    public static List<StringAndTag> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
        write("Inside of GetCompareToWords");
        
        List<StringAndTag> compareToWords = new ArrayList<StringAndTag>();
        int compareToWordsSize = 10;
        boolean compareToWordListFull = false;
       
        
        boolean firstSetDone = false;
        int answerIndexCurrent = answerIndex;
        int sentenceIndexCurrent = sentenceIndex;
        int lemmaIndexCurrent = lemmaIndex;
        
        //traverse answers
        while(!(answerIndexCurrent < 0)){
            
            Document currentAnswer = compareToGroupedAnswers.get(answerIndexCurrent);
            List<Sentence> currentSentences = currentAnswer.sentences();
            
            //traversing sentences in answer
            while(!(sentenceIndexCurrent < 0)){
                
                Sentence currentSentence = currentSentences.get(sentenceIndexCurrent);
                
                //traversing words in sentence
                while(!(lemmaIndexCurrent < 0)){
                    
                    //get lemma to be added here
                    List<String> lemmas = currentSentence.lemmas();
                    
                    if(currentSentence.posTag(lemmaIndexCurrent).matches("JJ|NN|VB|RB")){
                        if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
                            
                            compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
                            //compareToWords.add(new StringAndTag());
                        }
                    }
                    
                    if(compareToWords.size() == compareToWordsSize/2){
                        firstSetDone = true;
                        break;
                    }
                    
                    lemmaIndexCurrent--;
                    
                }
                
                if(firstSetDone){
                   break; 
                }
                else{
                    sentenceIndexCurrent--;
                    Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexCurrent);
                    Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexCurrent);
                    lemmaIndexCurrent = currentSentenceTemp.lemmas().size()-1;
                    continue;
                }
            
            }
            
            if(firstSetDone){
                break;
            }
            else{
                answerIndexCurrent--;
                Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexCurrent);
                lemmaIndexCurrent = currentAnswerTemp.sentences().size()-1;
                continue;
            }
        }
        
        answerIndexCurrent = answerIndex;
        sentenceIndexCurrent = sentenceIndex;
        lemmaIndexCurrent = lemmaIndex;
        
        //traverse answers
        while(!(answerIndexCurrent < compareToGroupedAnswers.size())){
            
            Document currentAnswer = compareToGroupedAnswers.get(answerIndexCurrent);
            List<Sentence> currentSentences = currentAnswer.sentences();
            
            //traverse sentences
            while(!(sentenceIndexCurrent < currentSentences.size())){
                
                Sentence currentSentence = currentSentences.get(sentenceIndexCurrent);
                
                while(!(lemmaIndexCurrent < currentSentence.lemmas().size())){
                    
                    //get lemma to be added here
                    List<String> lemmas = currentSentence.lemmas();
                    
                    if(currentSentence.posTag(lemmaIndexCurrent).matches("JJ|NN|VB|RB")){
                        if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
                            compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
                        }
                    }
                    
                    if(compareToWords.size() == compareToWordsSize){
                        compareToWordListFull = true;
                        break;
                    }
                    
                    lemmaIndexCurrent++;
                    
                }
                
                if(compareToWordListFull){
                    break;
                }
                
            }
            
            if(compareToWordListFull){
                break;
            }
            
        }
        
        write("GetCompareToWords");
        
        return compareToWords;
        
    }
    
    public static Tree GetSentenceTree(){
        
        Tree retTree = null;
        
        
        
        return retTree;
                
    }
    
    private static AnswerGroups groupAnswers(String[] answers){
        
        AnswerGroups answerGroups = new AnswerGroups();
        
        int cntQuestion = 0;
        
        for(int i = 0; i < answers.length;){
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
