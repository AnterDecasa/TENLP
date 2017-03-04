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
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Synset;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;
import edu.mit.jwi.item.POS;
import java.sql.*;

/**
 *
 * @author AnnTherese
 */
public class SummarizeText {
    
    static AnswerGroups groupedAnswers;
    static String[] compareWords;
    private static String host = "jdbc:mysql://localhost:3306/tenlp";
    private static String user = "root";
    private static String password = "";
    
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
        
        write("----------------------------------------");
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
                            write("Index of word to be used: " + indexOfWordToBeUsed + "\n");
                            write("IWordID of word: " + wordIDs.get(indexOfWordToBeUsed).toString());
                            
                            IWord word = dictionary.getWord(wordIDs.get(indexOfWordToBeUsed));
                            ISynset synset = word.getSynset();
<<<<<<< HEAD
                            write("ISynset: "+synset);
                            List<ISynsetID> synsetID = synset.getRelatedSynsets();
                            String[] synsetIDdissected = synsetID.get(0).toString().split("-");
                            
                            Connection connect = DriverManager.getConnection(host,user,password);
                            Statement stmt = connect.createStatement();
                            write("Synset ID: " + synsetID.get(0));
=======
                            List<ISynsetID> synsetIDs = synset.getRelatedSynsets();
                            String[] synsetIDdissected = synsetIDs.get(0).toString().split("-");
                            
                            Connection connect = DriverManager.getConnection(host,user,password);
                            Statement stmt = connect.createStatement();
                            
                            write("Synset ID: " + synsetIDs.get(0));
                            write("Synset ID: ");
                            for(ISynsetID synsetID : synsetIDs){
                                write(synsetID.toString() + " ");
                            }
>>>>>>> refs/remotes/origin/master
                            String sqlStmtsynsetID =  "SELECT * FROM dict WHERE ID = " + synsetIDdissected[1]; 
                            
                            write(sqlStmtsynsetID);
                            ResultSet results = stmt.executeQuery(sqlStmtsynsetID);        
                            
                            for(IWordID wordIDeach : wordIDs){
                                String[] wordIDDisected = wordIDeach.toString().split("-");
                                write("ID of word: " + wordIDDisected[1]);
                            }
<<<<<<< HEAD
=======
//                            String[] wordIDDisected = wordIDs.get(indexOfWordToBeUsed.toString().split("-");
//                                write("ID of word: " + wordIDDisected[1]);
                            
//                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = " + wordIDDisected[1];
//                            write(sqlStmtwordID);
//                            results = stmt.executeQuery(sqlStmtwordID);
//                            if(results.next()){
//                                write("Result: " + results.getInt("ID") + "|" + results.getInt("POS") + "|" + results.getInt("Gloss"));
//                            }
                            
>>>>>>> refs/remotes/origin/master
                        }
                    }   
                
                }
            }
        }
        catch(SQLException sqlError){
            write(sqlError.getMessage());
        }
        catch(Exception exc){
            
        }
        
        write("Exiting GetSentiment");
        
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
        
        //write("Inside of IfUniqueWord");
        
        boolean retVal = true;
        
        int index = 0;
        for(; index < words.size(); index++){
            if(words.get(index).equalsIgnoreCase(word) && POS.get(index).equalsIgnoreCase(POSofWord)){
                retVal = false;
                break;
            }
        }
        
        //write("Exiting IfUniqueWord");
        
        return retVal;
    }
    
    public static boolean IfUniqueWord(List<StringAndTag> listOfWords, String word){
        
        boolean retVal = false;
        
        if(listOfWords.size() != 0){
            for(StringAndTag currentWord : listOfWords){
            
                if(currentWord.word.compareToIgnoreCase(word) == 0){
                    retVal = true;
                }
            
            }
        }
        else{
            retVal = true;
        }
        
        return retVal;
        
    }
    
    public static int Disambiguate(IIndexWord indexWord, List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
        write("Inside Disambiguate");
        
        IDictionary dictionary = WordNetAccess.loadDic();
        int indexOfWordToBeUsed = 0;
        try
        {
            dictionary.open();

            int wordSensesSize = indexWord.getWordIDs().size();
            //write("Word Sense Size: " + wordSensesSize + "\n");

            int[] wordSenseScores = new int[wordSensesSize];
            List<IWordID> wordSenses = indexWord.getWordIDs();


            //get word with most similar words in gloss

            //access each answer
            int wordSenseTraverseCtr = 0;
            while(wordSenseTraverseCtr != wordSensesSize){

                //write("Disambiguate 1st loop");
                //write("Word Sense Traverse Ctr: " + wordSenseTraverseCtr + "\n");
                //get gloss for current wordSense from wordNet

                IWord word = dictionary.getWord(wordSenses.get(wordSenseTraverseCtr));

                String glossOfWordSense = word.getSynset().getGloss();

                List<StringAndTag> compareToWords = GetCompareToWords(compareToGroupedAnswers, answerIndex, sentenceIndex, lemmaIndex, questionIndex);
//                if(compareToWords != null){
//                    write("not null");
//                }
                //write("Disambiguate Compare To Words Size: " + compareToWords.size() + "\n");

                for(StringAndTag wordWithTag : compareToWords){
                    //write("Disambiguate 2nd loop" + "\n");
                    IIndexWord indexWordOfNeighborWord = dictionary.getIndexWord(wordWithTag.word, wordWithTag.tag);
                    List<IWordID> iWordIDsOfNeighborWord = indexWordOfNeighborWord.getWordIDs();

                    int sameWordsCtr = 0;

                    for(IWordID iWordIDOfNeighborWord : iWordIDsOfNeighborWord){
                        String oneGlossOfNeighborWord = dictionary.getWord(iWordIDOfNeighborWord).getSynset().getGloss();

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
                    //write("Score : " + wordSenseScores[wordSenseTraverseCtr] + "\n");
                }
                wordSenseTraverseCtr++;
            }

            int largestCnt = wordSenseScores[0];
            //write("Largest Count: " + largestCnt + "\n");
            for(int travCntr = 1; travCntr < wordSensesSize; travCntr++){
                //write("Disambiguate 3rd loop" + "\n");
                if(largestCnt > wordSenseScores[travCntr]){
                    indexOfWordToBeUsed = travCntr;
                    largestCnt = wordSenseScores[travCntr];
                }
            }

        }
        catch(Exception exc){
            write(exc.getMessage());
        }
        write("Exiting Disambiguate");
        return indexOfWordToBeUsed;
        
    }
    
//    public static List<StringAndTag> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
//        
//        write("Inside of GetCompareToWords");
//        
//        List<StringAndTag> compareToWords = new ArrayList<StringAndTag>();
//        int compareToWordsSize = 10;
//        boolean compareToWordListFull = false;
//        boolean firstSetDone = false;
//        int firstSetCount = 0;
//        int secondSetCount = 0; 
//        int answerIndexCurrent = answerIndex;
//        int sentenceIndexCurrent = sentenceIndex;
//        int lemmaIndexCurrent = lemmaIndex;
//        
//        while(answerIndexCurrent >= 0){
//            Document currentAnswer = compareToGroupedAnswers.get(answerIndexCurrent);
//            List<Sentence> currentSentences = currentAnswer.sentences();
//            while(sentenceIndexCurrent >= 0){
//                while((lemmaIndexCurrent--) >= 0){
//                  
//                    lemmaIndexCurrent--;
//                    if(lemmaIndexCurrent >= 0){
//                           
//                        firstSetCount++;
//                        if(firstSetCount == compareToWordsSize/2){
//                            firstSetDone = true;
//                            break;
//                        }
//
//                    }
//                    
//                }
//                if(firstSetDone){
//                    break;
//                }
//                else{
//                    sentenceIndexCurrent--;
//                    Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexCurrent);
//                    Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexCurrent);
//                    lemmaIndexCurrent = currentSentenceTemp.lemmas().size()-1;
//                }
//            }
//            if(firstSetDone){
//                break;
//            }
//            else{
//                sentenceIndexCurrent--;
//                Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexCurrent);
//                Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexCurrent);
//                lemmaIndexCurrent = currentSentenceTemp.lemmas().size()-1;
//            }
//        }
//        
//        answerIndexCurrent = answerIndex;
//        sentenceIndexCurrent = sentenceIndex;
//        lemmaIndexCurrent = lemmaIndex;
//        
//        Document currentAnswer;
//        List<Sentence> currentSentences;
//        Sentence currentSentence;
//        List<String> lemmas;
//        while(answerIndexCurrent < compareToGroupedAnswers.size()){
//            currentAnswer = compareToGroupedAnswers.get(answerIndexCurrent);
//            currentSentences = currentAnswer.sentences();
//            while(sentenceIndexCurrent < currentSentences.size()){
//                currentSentence = currentSentences.get(sentenceIndexCurrent);
//                lemmas = currentSentence.lemmas();
//                while((lemmaIndexCurrent++) < lemmas.size() && secondSetCount < compareToWordsSize){
//                    secondSetCount++;
//                }
//            }
//        }
//        if(secondSetCount < compareToWordsSize/2){
//            firstSetCount = firstSetCount + ((compareToWordsSize/2) - secondSetCount);
//        }
//        secondSetCount = compareToWordsSize - firstSetCount;
//        
//        //traverse answers
//        while(answerIndexCurrent >= 0){
//            //write("Compare to 1st loop" + "\n");
//            currentAnswer = compareToGroupedAnswers.get(answerIndexCurrent);
//            currentSentences = currentAnswer.sentences();
//
//            //traversing sentences in answer
//            while(sentenceIndexCurrent >= 0){
//                //write("Compare to 2nd loop");
//                currentSentence = currentSentences.get(sentenceIndexCurrent);
//
//                //traversing words in sentence
//                while((lemmaIndexCurrent--) >= 0){
//                    //write("Comparet to 3rd loop");
//                    //get lemma to be added here
//                    lemmas = currentSentence.lemmas();
//
//                    if(currentSentence.posTag(lemmaIndexCurrent).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                        //write("Compare to 1st if");
////                        if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
////                            //write("Compare to 2nd if");
////                            compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
////                            //compareToWords.add(new StringAndTag());
////                            write("Compare to words size: " + compareToWords.size() + "\n");
////                        }
//                          compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
//                    }
//
//                    if(compareToWords.size() == firstSetCount){
//                        firstSetDone = true;
//                        //write("first half done");
//                        break;
//                    }
//
//                }
//
//                if(firstSetDone){
//                   break; 
//                }
//                else{
//                    sentenceIndexCurrent--;
//                    Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexCurrent);
//                    Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexCurrent);
//                    lemmaIndexCurrent = currentSentenceTemp.lemmas().size()-1;
//                }
//
//            }
//
//            if(firstSetDone){
//                break;
//            }
//            else{
//                answerIndexCurrent--;
//                Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexCurrent);
//                sentenceIndexCurrent = currentAnswerTemp.sentences().size()-1;
//                Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexCurrent);
//                lemmaIndexCurrent = currentSentenceTemp.lemmas().size()-1;
//            }
//        }
//        
//        answerIndexCurrent = answerIndex;
//        sentenceIndexCurrent = sentenceIndex;
//        lemmaIndexCurrent = lemmaIndex;
//        
//        //traverse answers
//        while((answerIndexCurrent < compareToGroupedAnswers.size())){
//
//        compareToGroupedAnswers.get(answerIndexCurrent);
//        currentSentences = currentAnswer.sentences();
//
//            //traverse sentences
//            while((sentenceIndexCurrent < currentSentences.size())){
//
//                Sentence currentSentence = currentSentences.get(sentenceIndexCurrent);
//
//                while((lemmaIndexCurrent++) < currentSentence.lemmas().size()){
//
//                    //get lemma to be added here
//                    List<String> lemmas = currentSentence.lemmas();
//
//                    if(currentSentence.posTag(lemmaIndexCurrent).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
////                        if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
////                            compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
////                            write("Compare to words size: " + compareToWords.size() + "\n");
////                        }
//                        compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
//                    }
//
//                    if(compareToWords.size() == compareToWordsSize){
//                        compareToWordListFull = true;
//                        break;
//                    }
//                }
//
//                if(compareToWordListFull){
//                    break;
//                }
//                else{
//                    sentenceIndexCurrent++;
//                    lemmaIndexCurrent = 0;
//                }
//
//
//            }
//
//            if(compareToWordListFull){
//                break;
//            }
//            else{
//                answerIndexCurrent++;
//                sentenceIndexCurrent = 0;
//                lemmaIndexCurrent = 0;
//            }
//
//        }
//        
//        write("Exiting GetCompareToWords");
//        
//        return compareToWords;
//        
//    }
    
    public static List<StringAndTag> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
        write("Inside of GetCompareToWords");
        List<StringAndTag> compareToWords = new ArrayList<StringAndTag>();
        try{

            int compareToWordsFullSize = 10;
            boolean full = false;
            int answerIndexPrev = answerIndex;
            int sentenceIndexPrev = sentenceIndex;
            int lemmaIndexPrev = lemmaIndex;
            int answerIndexPrec = answerIndex;
            int sentenceIndexPrec = sentenceIndex;
            int lemmaIndexPrec = lemmaIndex;

            Document currentAnswer;
            List<Sentence> currentSentences;
            Sentence currentSentence;
            List<String> lemmas;
            while(!full){
                if(answerIndexPrev >= 0){
                    currentAnswer = compareToGroupedAnswers.get(answerIndexPrev);
                    currentSentences = currentAnswer.sentences();
                    if(sentenceIndexPrev >= 0){
                        currentSentence = currentSentences.get(sentenceIndexPrev);
                        lemmas = currentSentence.lemmas();
                        if((--lemmaIndexPrev) >= 0){
                            //write("Lemmas: " + lemmas.size() + "\n" + "Lemma Prev Index: " + lemmaIndexPrev + "\n");
                            if(currentSentence.posTag(lemmaIndexPrev).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
    //                            if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
    //                                compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
    //                                write("Compare to words size: " + compareToWords.size() + "\n");
    //                            }
                                compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexPrev),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexPrev))));
                                if(compareToWords.size() == compareToWordsFullSize){
                                    full = true;
                                }
                            }
                        }
                        else{
                            if((--sentenceIndexPrev) >= 0){
                                Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexPrev);
                                Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexPrev);
                                lemmaIndexPrev = currentSentenceTemp.lemmas().size()-1;
                            }
                        }
                    }
                    else{
                        if((--answerIndexPrev) >=0 ){
                            Document currentAnswerTemp = compareToGroupedAnswers.get(answerIndexPrev);
                            sentenceIndexPrev = currentAnswerTemp.sentences().size()-1;
                            Sentence currentSentenceTemp = currentAnswerTemp.sentences().get(sentenceIndexPrev);
                            lemmaIndexPrev = currentSentenceTemp.lemmas().size()-1;
                    
                        }
                    }
                }
                if(answerIndexPrec < compareToGroupedAnswers.size()){
                    currentAnswer = compareToGroupedAnswers.get(answerIndexPrec);
                    currentSentences = currentAnswer.sentences();
                    if(sentenceIndexPrec < currentSentences.size()){
                        currentSentence = currentSentences.get(sentenceIndexPrec);
                        lemmas = currentSentence.lemmas();
                        if((++lemmaIndexPrec) < lemmas.size()){
                            //write("Lemmas: " + lemmas.size() + "\n" + "Lemma Prec Index: " + lemmaIndexPrec + "\n");
                            if(currentSentence.posTag(lemmaIndexPrec).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
    //                            if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
    //                                compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
    //                                write("Compare to words size: " + compareToWords.size() + "\n");
    //                            }
                                compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexPrec),LanguageProcess.GetPOSTag(currentSentence.posTag(lemmaIndexPrec))));
                                if(compareToWords.size() == compareToWordsFullSize){
                                    full = true;
                                }
                            }
                        }
                        else{
                            sentenceIndexPrec++;
                            lemmaIndexPrec = 0;
                        }
                    }
                    else{
                        answerIndexPrec++;
                        sentenceIndexPrec = 0;
                        lemmaIndexPrec = 0;
                    }
                }
            }   
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
        
        write("Exiting GetCompareToWords");
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
    
    private static void write(int integer){
        
        System.out.print(integer + "");
        
    }
    
}
