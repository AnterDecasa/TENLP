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
    private static List<Document> positiveSentences = new ArrayList<Document>();
    private static List<Document> negativeSentences = new ArrayList<Document>();
    private static List<Document> neutralSentences = new ArrayList<Document>();
    
    public static void summarize(String string){
        
        try{
        
            string = TextFilePreProcess.removeCarets(string);
            String[] stringArray = string.split("\\r?\\n");
        
            groupedAnswers = groupAnswers(stringArray);
        
            //Get sentimentScore for each answer
            write("Teacher Strength");
            int answerIndex = 0;
            for(; answerIndex < groupedAnswers.getTeachStrength().size();answerIndex++){ 
                double sentimentScore = GetSentiment(groupedAnswers.getTeachStrength(),answerIndex, 0);
                write("Score:" + sentimentScore);
            }
            //TeachWeakness
            write("Teacher Weakness");
            answerIndex = 0;
            List<Document> currentGroup = groupedAnswers.getTeachWeak();
            for(; answerIndex < groupedAnswers.getTeachWeak().size(); answerIndex++){ 
                double sentimentScore = GetSentiment(currentGroup,answerIndex, 0);
                AssignToPosNeg(sentimentScore,currentGroup.get(answerIndex));
                write("Score:" + sentimentScore);
            }
            
            //Comments
            write("Comment");
            answerIndex = 0;
            currentGroup = groupedAnswers.getComments();
            for(; answerIndex < groupedAnswers.getComments().size(); answerIndex++){ 
                double sentimentScore = GetSentiment(currentGroup,answerIndex, 0);
                AssignToPosNeg(sentimentScore,currentGroup.get(answerIndex));
                write("Score:" + sentimentScore);
            }
            
            write("Done");
            
        }
        
        catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
    public static void AssignToPosNeg(double score, Document answer){
        if(score < 0){
            negativeSentences.add(answer);
        }
        else if(score == 0){
            neutralSentences.add(answer);
        }
        else if(score > 0){
            positiveSentences.add(answer);
        }
    }
    
    public static double GetSentiment(List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
//        write("Inside GetSentiment");
        double sentimentScore = 0;
        try{
        
            //Get sense for each word
            List<Sentence> answerSentences = compareToGroupedAnswers.get(answerIndex).sentences();
            int answerSentencesSize = answerSentences.size();
        
            IDictionary dictionary = WordNetAccess.loadDic();
            dictionary.open();
            for(int sentenceCtr = 0; sentenceCtr < answerSentencesSize; sentenceCtr++){
            
                Sentence currentSentence = answerSentences.get(sentenceCtr);
                int lemmaSize = currentSentence.lemmas().size();
            
                for(int lemmaCtr = 0; lemmaCtr < lemmaSize; lemmaCtr++){
                
                    String currentPOSTag = currentSentence.posTag(lemmaCtr);
                    if(currentPOSTag.matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                        Connection connect = DriverManager.getConnection(host,user,password);
                        Statement stmt = connect.createStatement();
                        ResultSet results;
                            
//                        write(currentSentence.lemma(lemmaCtr));
//                        write("Tag: " + LanguageProcess.GetPOSTag(currentPOSTag));
                        IIndexWord indexWord = dictionary.getIndexWord(currentSentence.lemma(lemmaCtr), LanguageProcess.GetPOSTag(currentPOSTag));
                        if(indexWord != null){
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            if(wordIDs.size() > 1){
                                //Disambiguate
                                int indexOfWordToBeUsed = Disambiguate(indexWord, compareToGroupedAnswers, answerIndex, sentenceCtr, lemmaCtr, questionIndex);
    //                            write("Index of word to be used: " + indexOfWordToBeUsed + "\n");
    //                            write("IWordID of word: " + wordIDs.get(indexOfWordToBeUsed).toString());

                                IWord word = dictionary.getWord(wordIDs.get(indexOfWordToBeUsed));
    //                            ISynset synset = word.getSynset();
    //                            List<ISynsetID> synsetIDs = synset.getRelatedSynsets();
    //                            String[] synsetIDdissected = synsetIDs.get(0).toString().split("-");

    //                            write("Synset ID: " + synsetIDs.get(0));
    //                            write("Synset ID: ");
    //                            for(ISynsetID synsetID : synsetIDs){
    //                                write(synsetID.toString() + " ");
    //                            }
    //                            String sqlStmtsynsetID =  "SELECT * FROM dict WHERE ID = " + synsetIDdissected[1]; 

    //                            write(sqlStmtsynsetID);     

    //                            for(IWordID wordIDeach : wordIDs){
    //                                String[] wordIDDisected = wordIDeach.toString().split("-");
    //                                write("ID of word: " + wordIDDisected[1]);
    //                            }
                                String[] wordIDDisected = wordIDs.get(indexOfWordToBeUsed).toString().split("-");
    //                            write("ID of word: " + wordIDDisected[1]);

                                String sqlStmtwordID = "SELECT * FROM dict WHERE ID = " + wordIDDisected[1];
    //                            write(sqlStmtwordID);
                                results = stmt.executeQuery(sqlStmtwordID);
    //                            write("\t\t" + "ID" + "\t|" + "PosScore" + "\t|" + "NegScore");
                                if(results.next()){
    //                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getInt("PosScore") + "\t|" + results.getInt("NegScore"));
                                    sentimentScore += results.getFloat("PosScore") - results.getFloat("NegScore");
                                }
                                else{
                                    write("not result");
                                }

                            }
                            else{
                                IWord word = dictionary.getWord(wordIDs.get(0));
                                String[] wordIDDisected = wordIDs.get(0).toString().split("-");
    //                            write("ID of word: " + wordIDDisected[1]);

                                String sqlStmtwordID = "SELECT * FROM dict WHERE ID = " + wordIDDisected[1];
    //                            write(sqlStmtwordID);
                                results = stmt.executeQuery(sqlStmtwordID);
    //                            write("\t\t" + "ID" + "\t|" + "PosScore" + "\t|" + "NegScore");
                                if(results.next()){
    //                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getInt("PosScore") + "\t|" + results.getInt("NegScore"));
                                    sentimentScore += results.getFloat("PosScore") - results.getFloat("NegScore");
                                }
                            }
                        }
                        
                    }   
                
                }
            }
        }
        catch(SQLException sqlError){
            write(sqlError.getMessage());
            sqlError.printStackTrace();
        }
        catch(Exception exc){
            write(exc.getMessage());
            exc.printStackTrace();
        }
        
//        write("Exiting GetSentiment");
        
        return sentimentScore;
        
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
        
//        write("Inside Disambiguate");
        
        int indexOfWordToBeUsed = 0;
        try
        {
            IDictionary dictionary = WordNetAccess.loadDic();
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
                    
                    if(indexWordOfNeighborWord != null){
                        List<IWordID> iWordIDsOfNeighborWord = indexWordOfNeighborWord.getWordIDs();

                        int sameWordsCtr = 0;

                        for(IWordID iWordIDOfNeighborWord : iWordIDsOfNeighborWord){
                            String oneGlossOfNeighborWord = dictionary.getWord(iWordIDOfNeighborWord).getSynset().getGloss();
//                            write(oneGlossOfNeighborWord.replaceAll("\"|;", " "));
//                            String[] glossOfWordSenseStringArray = glossOfWordSense.split("| |\"|;");
//                            String[] oneGlossOfNeighborWordStringArray = oneGlossOfNeighborWord.split("| |\"|;");
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
            exc.printStackTrace();
        }
//        write("Exiting Disambiguate");
        return indexOfWordToBeUsed;
        
    }
    
    public static List<StringAndTag> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
//        write("Inside of GetCompareToWords");
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
        
//        write("Exiting GetCompareToWords");
        return compareToWords;
        
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
    */
    
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
