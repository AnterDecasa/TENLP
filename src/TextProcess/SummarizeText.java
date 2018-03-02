/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import ContainerClasses.AnswerGroups;
import edu.stanford.nlp.trees.Tree;
import ContainerClasses.StringAndTag;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;
import edu.mit.jwi.item.POS;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import javax.swing.JTextArea;

/**
 *
 * @author AnnTherese
 */
public class SummarizeText {
    
    static AnswerGroups groupedAnswers;
    static String[] compareWords;
    private static final String host = "jdbc:mysql://localhost:3306/tenlp";
    private static final String user = "root";
    private static final String password = "";
    private static List<Document> positiveSentences = new ArrayList<Document>();
    private static List<Document> negativeSentences = new ArrayList<Document>();
    private static List<Document> neutralSentences = new ArrayList<Document>();
    
    private static List<String> positiveSentencesList = new ArrayList<String>();
    private static List<String> negativeSentencesList = new ArrayList<String>();
    
    public static List<String> getPositiveSentences(){
        return positiveSentencesList;
    }
    
    public static List<String> getNegativeSentences(){
        return negativeSentencesList;
    }
    
    /*
    1st algorithm
        - Used for getting sentiment of one document only
        - No disambiguation
        - No negation
    */
    public static void getSentimentofWholeDocument(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
        
        /*
        Sum of Positive score and negative score,
        and counted words that were used in calculation of sentiment 
        of entire document
        */
        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            /*Connect to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
        
            for(Sentence sent : docu.sentences()){
                /*
                Getting the part-of-speech tag for each word in the sentence
                Getting the root form of the words for each sentence
                */
                List <String> tags = sent.posTags();           
                List <String> words =  sent.lemmas();
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int index = 0;
                for(;index < tags.size(); index++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        /*Loading WordNet dictionary for accessing*/
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();

                        /*Get the wordID that represent word meaning*/
                        IIndexWord indexWord = dictionary.getIndexWord(words.get(index), GetPOSTag(tags.get(index)));
                        if(indexWord != null){
                            wordCount++;
                            /*Get all the word IDs*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            
                            /*Positive, negative and objective score of the word*/
                            double posScore = 0;
                            double negScore = 0;    
                            double senseCtr = 0;
                            
                            /*Get the first meaning/sense of the word*/
                            IWord word = dictionary.getWord(wordIDs.get(0));
                            String sqlStmtwordID = "SELECT * FROM dict WHERE (SynsetTerms LIKE '%"+ word.getLemma() +"#%' OR SynsetTerms LIKE '%"+ word.getLemma() +"#%') AND (PosScore > 0 AND NegScore > 0)";
                            results = stmt.executeQuery(sqlStmtwordID);
                            while(results.next()){
                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                                senseCtr++;
                            }
                            write("Sense Count: " + senseCtr++);
                            positive += posScore/senseCtr;
                            negative += negScore/senseCtr;
                            senseCtr = 0;
                            posScore = 0;
                            negScore = 0;
                        }/*getting word score close*/

                        dictionary.close();
                    
                    }/*if noun, verb, adj or adv close*/
                }/*loop for traversing each word in each sentence*/
            }/*loop for traversing each sentence in each document close*/
            connect.close();
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
    }
    
    /*
    2nd algorithm
        - Used for getting sentiment of 1 document only
        - No disambiguation
        - With negation
        - Only displays in cosole the sentiment score
    */
    public static void getSentimentofWholeDocumentWithNegation(String string){
        
        /*Removing unnecessary text from evaluation*/
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
        
        /*
        Sum of Positive score and negative score,
        and counted words that were used in calculation of sentiment 
        of entire document
        */
        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            /*Connect to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
        
            for(Sentence sent : docu.sentences()){
             /*
                Positive and Negative scores of the sentence
                */
                double sentPos = 0;
                double sentNeg = 0;
                /*
                Getting the part-of-speech tag of the words in the sentence
                Getting the root form of the word in the sentence
                */
                List <String> tags = sent.posTags();            
                List <String> words =  sent.lemmas();
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int index = 0;
                for(;index < tags.size(); index++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        /*
                        Loading the WordNet Dictionary for accessing
                        */
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();
                        IIndexWord indexWord = dictionary.getIndexWord(words.get(index), GetPOSTag(tags.get(index)));
                        if(indexWord != null){
                            wordCount++;
                            
                            double posScore = 0;/*Sum of positive scores for all word meanings or senses*/
                            double negScore = 0;/*Sum of negative scores for all word meanings or senses*/    
                            double senseCtr = 0;/*Counts the number of sense/ meaning of the word*/
                            /*
                            Get all the wordIDs for each word meaning
                            but only use the first word sense or wordID
                            */
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            IWord word = dictionary.getWord(wordIDs.get(0));
                            String sqlStmtwordID = "SELECT * FROM dict WHERE (SynsetTerms LIKE '%"+ word.getLemma() +"#%' OR SynsetTerms LIKE '%"+ word.getLemma() +"#%') AND (PosScore > 0 AND NegScore > 0)";
                            results = stmt.executeQuery(sqlStmtwordID);
                            while(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                                senseCtr++;
                            }
                            /*
                            For Negation, checking if there is adverb before adjectives and verbs
                            If there is, combine the adverb score with the adjective or vebr score
                            */
                            if((tags.get(index).matches("JJ(R|S)?") || tags.get(index).matches("VB(D|G|N|P|Z)?")) && (index > 0 && tags.get(index-1).matches("RB(S|R)?"))){
                                wordCount++;
                                indexWord = dictionary.getIndexWord(words.get(index-1), GetPOSTag(tags.get(index-1)));
                                wordIDs = indexWord.getWordIDs();
                                word = dictionary.getWord(wordIDs.get(0));
                                write(word.getLemma());

                                sqlStmtwordID = "SELECT * FROM dict WHERE (SynsetTerms LIKE '%"+ word.getLemma() +"#%' OR SynsetTerms LIKE '%"+ word.getLemma() +"#%') AND (PosScore > 0 AND NegScore > 0)";
                                results = stmt.executeQuery(sqlStmtwordID);
                                double advPosScore = 0;
                                double advNegScore = 0;
                                while(results.next()){
                                    write("Result: \t" + results.getInt("ID") + "\t|" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                    advPosScore += results.getFloat("PosScore");
                                    advNegScore += results.getFloat("NegScore");
                                    senseCtr++;
                                }
//                                if(advPosScore <= advNegScore){
//                                    double temp = posScore;
//                                    posScore = negScore;
//                                    negScore = temp;
//                                }
//                                if(advPosScore <= advNegScore){
//                                    double temp = posScore;
//                                    posScore = negScore + advPosScore;
//                                    negScore = temp + advNegScore;
//                                }
                                if(advPosScore <= advNegScore){
                                    double temp = posScore;
                                    posScore = negScore;
                                    negScore = temp + advNegScore;
                                }
                                else if(advPosScore >= advNegScore){
                                    double temp = posScore;
                                    posScore = negScore + advPosScore;
                                    negScore = temp;
                                }
                            }/*If there is adverb close*/
                            /*
                            Add the average score of all the senses/meaning of the word
                            */
                            positive += posScore/senseCtr;
                            negative += negScore/senseCtr;
                            sentPos += posScore/senseCtr;
                            sentNeg += negScore/senseCtr;
                            write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                            senseCtr = 0;
                            posScore = 0;
                            negScore = 0;
                        }/*getting word score close*/

                        dictionary.close();
                   }/*if noun, verb, adj, adv close*/
                    
                }/*loop for traversing all words in each sentence close*/
            }/*loop for traversing all sentences in each document close*/
            connect.close();
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
    }
    
    /*
    3rd algorithm
        - Used for getting sentiment of 1 document only
        - With disambiguation
        - With negation
        - Only displays in console the sentiment score
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguation(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
//        noQuestions = TextFilePreProcess.correctPeriodsPutSpaceAfter(noQuestions);
        
        double positive = 0;/*Total positive score of document*/
        double negative = 0;/*Total negative score of document*/
        double wordCount = 0;/*Counter for the number of words that was included in sentiment calculation*/
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            /*Put into a file the sentiment score for each sentence*/
            PrintWriter pw = new PrintWriter(new File("sentenceScores.csv"));
            StringBuilder sb = new StringBuilder();
            
            /*Put into a file the tex that has no unnecessart text*/
            PrintWriter cleanText = new PrintWriter(new File("cleanText.txt"));
            StringBuilder sbCleanText = new StringBuilder();
            
            sb.append("Sentence,Positive,Negative\n");
        
            /*Connectint to SentiWordNet Database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                sbCleanText.append(sentences.get(sentCtr).text());
                sbCleanText.append(" ");
                double sentPos = 0;/*Positive score for each sentence*/
                double sentNeg = 0;/*Negative score for each sentence*/
                List <String> tags = sentences.get(sentCtr).posTags();/*Get part-of-speech tags for each word*/           
                List <String> words =  sentences.get(sentCtr).lemmas();/*Get root form of each word*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Load WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*Positive score for each word*/
                            double negScore = 0;/*Negative score for each word*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Get all the wordIDs that represent all the senses/meaning of each word*/
                            
                            /*If there are many senses Disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            /*
                            Extract the wordID that taken from
                            WordNet so that it will be useful
                            in SentiWordNet Database search
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
                            /*
                            Check verbs and adjectives if they have adverbs before them
                            If there is, combine the adverb score to the verb or adjective
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    /*
                                    Extract the wordID that was taken from WordNet
                                    so that it will be useful in the SentiWordNet database search
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;
                                    double advNegScore = 0;
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore;
    //                                    negScore = temp;
    //                                }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore + advPosScore;
    //                                    negScore = temp + advNegScore;
    //                                }
                                    if(advPosScore < advNegScore){
                                        double temp = posScore;
                                        posScore = negScore;
                                        negScore = temp + advNegScore;
                                    }
                                    else if(advPosScore >= advNegScore){
                                        if(posScore >= negScore){
                                            posScore = posScore + advPosScore;
                                            negScore = negScore + advNegScore;
                                        }
                                        else if(posScore < negScore){
                                            negScore = negScore + advPosScore;
                                            posScore = posScore + advNegScore;
                                        }
                                        
                                    }
                                } 
                                
                            }/*if there is adverb close*/
                            
                            positive += posScore;/*Add positive score of word to total positive score of document*/
                            negative += negScore;/*Add negative score of word to total negative score of document*/
                            sentPos += posScore;/*Add positive score of word to total positive score of sentence*/
                            sentNeg += negScore;/*Add negative score of word to total negative score of sentence*/
                            posScore = 0;
                            negScore = 0;
                        }/*if noun, verb, adj, and adv close*/

                        dictionary.close();
                    }/*if noun, verb, adj, or adv close*/
                    
                }/*loop for traversing all the words in each sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                
                sb.append(sentences.get(sentCtr).text());
                sb.append(",");
                sb.append(sentPos);
                sb.append(",");
                sb.append(sentNeg);
                sb.append('\n');
        
            }/*loop for traversing all sentences in a document close*/
            connect.close();
            pw.write(sb.toString());
            pw.close();
            cleanText.write(sbCleanText.toString());
            cleanText.close();
            System.out.println("output sentences done!");
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
    }
    
    /*
    4th algorithm
        - Used for getting sentiment of 1 document only
        - With disambiguation
        - With negation
        - Returns as string the sentiment score of document
    */
    public static String getSentimentofWholeDocumentWithNegationWithDisambiguationReturnString(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
//        noQuestions = TextFilePreProcess.correctPeriodsPutSpaceAfter(noQuestions);
        
        double positive = 0;/*Total positive score of document*/
        double negative = 0;/*Total negative score of document*/
        double wordCount = 0;/*Counter for the words that was included in the calculation*/
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            /*Output into a file the sentiment scores for each sentence*/
            PrintWriter pw = new PrintWriter(new File("sentenceScores.csv"));
            StringBuilder sb = new StringBuilder();
            
            /*Output into a file the cleaned text*/
            PrintWriter cleanText = new PrintWriter(new File("cleanText.txt"));
            StringBuilder sbCleanText = new StringBuilder();
            
            sb.append("Sentence,Positive,Negative\n");
        
            /*Connecting to the SentiWordNet Database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                sbCleanText.append(sentences.get(sentCtr).text());
                sbCleanText.append(" ");
                double sentPos = 0;/*Total positive score for each sentence*/
                double sentNeg = 0;/*Total negative score for each sentence*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*Getting the part-of-speech tags for each word*/            
                List <String> words =  sentences.get(sentCtr).lemmas();/*Getting the root form of each word*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Loading the WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*Positive score for each word*/
                            double negScore = 0;/*Negative score for each word*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Getting all wordIDs for all word senses/meanings*/
                            /*If there are many word senses, disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }

                            /*
                            Extracint the wordID taken form WordNet
                            so that the wordID will be used
                            in accessing the SentiWordNet Database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }

                            /*
                            Checking if a verb or an adjective has an adverb before it
                            If it has, the adverb score will be combined with the verb or adjective
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();
                                    /*
                                    If there are many word sense or meanings disambiguate
                                    */
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    /*
                                    Extract the wordID taken from WordNet
                                    so that this extracteed wordID can be used
                                    in searching the SentiWordNet database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;
                                    double advNegScore = 0;
                                    if(results.next()){
//                                        write("Result: \t" + results.getInt("ID") + "\t |" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore;
    //                                    negScore = temp;
    //                                }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore + advPosScore;
    //                                    negScore = temp + advNegScore;
    //                                }
                                    if(advPosScore < advNegScore){
                                        double temp = posScore;
                                        posScore = negScore;
                                        negScore = temp + advNegScore;
                                    }
                                    else if(advPosScore >= advNegScore){
                                        if(posScore >= negScore){
                                            posScore = posScore + advPosScore;
                                            negScore = negScore + advNegScore;
                                        }
                                        else if(posScore < negScore){
                                            negScore = negScore + advPosScore;
                                            posScore = posScore + advNegScore;
                                        }
                                        
                                    }
                                }/*getting score for adverb close*/ 
                                
                            }/*if there is adverb close*/
                            
                            positive += posScore;/*Add the positive score of the word to total score of the document*/
                            negative += negScore;/*Add the negative score of the word to total score of the document*/
                            sentPos += posScore;/*Add the positive score of the word to total score of the sentence*/
                            sentNeg += negScore;/*Add the negative score of the word to total score of the sentence*/
                            posScore = 0;
                            negScore = 0;
                        }/*getting of word score close*/

                        dictionary.close();
                    }/*if noun, verb, adj, or adv close*/
                    
                }/*loop for traversing all word in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                
                sb.append(sentences.get(sentCtr).text());
                sb.append(",");
                sb.append(sentPos);
                sb.append(",");
                sb.append(sentNeg);
                sb.append('\n');
        
            }/*loop for traversing sentences in a document close*/
            connect.close();
            pw.write(sb.toString());
            pw.close();
            cleanText.write(sbCleanText.toString());
            cleanText.close();
            System.out.println("output sentences done!");
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
        
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);  
        
        return "Positive: " + positive/wordCount + "\n" + "Negtive: " + negative/wordCount + "\n" + "Word Count: " + wordCount;
    }
    
    /*
    5th algorithm
        - Used for getting sentiment of 1 document only
        - With disambiguation
        - With negation
        - Returns as string the sentiment score of document
    */
    public static String getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanText(String string){

        double positive = 0;/*Total positive sentiment score of the document*/
        double negative = 0;/*Total negative sentiment score of the document*/
        double wordCount = 0;/*Counter for words that were included in the calculation of sentiment score*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            
            /*Created a file that contains the sentiment scores for each sentence*/
            PrintWriter pw = new PrintWriter(new File("sentenceScores.csv"));
            StringBuilder sb = new StringBuilder();
            
            /*Create file that has the clean text of the teacher evaluation*/
            PrintWriter cleanText = new PrintWriter(new File("cleanText.txt"));
            StringBuilder sbCleanText = new StringBuilder();
            
            sb.append("Sentence,Positive,Negative\n");
        
            /*Connect to the SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){        
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                sbCleanText.append(sentences.get(sentCtr).text());
                sbCleanText.append(" ");
                double sentPos = 0;/*Total positive score*/
                double sentNeg = 0;/*Total negative score*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*Get the part-of-speech tags for each word in a sentence*/
                List <String> words =  sentences.get(sentCtr).lemmas();/*Get the root form of each word in a sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Loading WordNet Dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*Positive score for each word*/
                            double negScore = 0;/*Negative score for each word*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Get wordIDs for all word senses or meanings*/
                            /*If there are many word senses, disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            /*
                            Extract the wordID taken form WordNet
                            in order for it to be useful in
                            searching for the sentiment score in SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                           
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
                            /*
                            Check if verbs or adjectives have adverbs preceding it
                            if there is an adverb, combine the adverb score to the verb or adjective score
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*Get wordIDs for all the senses/meanings of a word*/
                                    
                                    /*If many wordSenses, disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    
                                    /*
                                    Extract the wordID taken form WordNet
                                    so that it will be useful in 
                                    searching in the SentiWordNet Database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;
                                    double advNegScore = 0;
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore;
    //                                    negScore = temp;
    //                                }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore + advPosScore;
    //                                    negScore = temp + advNegScore;
    //                                }
                                    if(advPosScore < advNegScore){
                                        double temp = posScore;
                                        posScore = negScore;
                                        negScore = temp + advNegScore;
                                    }
                                    else if(advPosScore >= advNegScore){
                                        if(posScore >= negScore){
                                            posScore = posScore + advPosScore;
                                            negScore = negScore + advNegScore;
                                        }
                                        else if(posScore < negScore){
                                            negScore = negScore + advPosScore;
                                            posScore = posScore + advNegScore;
                                        }
                                        
                                    }
                                } 
                                
                            }/*getting score of adverb close*/
                            
                            positive += posScore;/*Add the positive word score to the total positive score of the document*/
                            negative += negScore;/*Add the negative word score to the total negative score of the document*/
                            sentPos += posScore;/*Add the positive word score to the total positive score of the sentence*/
                            sentNeg += negScore;/*Add the negative word score to the total negative score of the sentence*/
                            posScore = 0;
                            negScore = 0;
                        }/*getting word score close*/

                        dictionary.close();
                    }/*if word is noun, verb, adj, or adv close*/
                    
                }/*loo for traversing each word in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                
                sb.append(sentences.get(sentCtr).text());
                sb.append(",");
                sb.append(sentPos);
                sb.append(",");
                sb.append(sentNeg);
                sb.append('\n');
        
            }/*loop for traversing each sentence in a document close*/
            connect.close();
            pw.write(sb.toString());
            pw.close();
            cleanText.write(sbCleanText.toString());
            cleanText.close();
            System.out.println("output sentences done!");
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);  
        
        return "Positive: " + positive/wordCount + "\n" + "Negtive: " + negative/wordCount + "\n" + "Word Count: " + wordCount;
    }
    
    /*
    6th algorithm
        - Used for getting sentiment of multiple documents
        - With disambiguation
        - With negation
        - Records in a file called documentScores.csv to show results of sentiments for each document
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFile(String fileName, String string, PrintWriter pw, StringBuilder  sb){

        double positive = 0;/*Total positive score of the document*/
        double negative = 0;/*Total negative score of the document*/
        double wordCount = 0;/*Counter for words that had been used in the calculation of sentiment*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            /*Connect to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*Total positive score of each sentence*/
                double sentNeg = 0;/*Total negative score of each sentence*/
                List <String> tags = sentences.get(sentCtr).posTags();/*Get all the part-of-speech tags for each word in the sentence*/           
                List <String> words =  sentences.get(sentCtr).lemmas();/*Get all the root form of each word in the sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Loading WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*positive score for each word*/
                            double negScore = 0;/*negative score for each word*/
                            
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Get all wordIDs that represent all the senses/meaning of the word*/
                            /*If there are many wordIDs then disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            
                            /*
                            Extract the wordID taken from WordNet
                            so tha it can be used in
                            searchin the SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                           
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }

                            /*
                            Checking of a verb or an adjective has a preceding adverb
                            if there is, the score of the adverb will be
                            combined with the vebr or adjective score
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*Get all the wordIDs that represent all the senses/meanings of the word*/
                                    /*if many wordIDs returned, then disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    /*
                                    Extract the wordID taken from WordNet
                                    so that it can be used in
                                    searching the SentiWordNet database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;
                                    double advNegScore = 0;
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
                                    //IMDB Files Test (3)
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore;
    //                                    negScore = temp;
    //                                }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore + advPosScore;
    //                                    negScore = temp + advNegScore;
    //                                }
//                                    if(advPosScore < advNegScore){
//                                        double temp = posScore;
//                                        posScore = negScore + advPosScore;
//                                        negScore = temp + advNegScore;
//                                    }
//                                    else if(advPosScore >= advNegScore){
//                                        if(posScore >= negScore){
//                                            posScore = posScore + advPosScore;
//                                            negScore = negScore + advNegScore;
//                                        }
//                                        else if(posScore < negScore){
//                                            negScore = negScore + advPosScore;
//                                            posScore = posScore + advNegScore;
//                                        }
//                                        
//                                    }

                                    if(advPosScore < advNegScore){
                                        double temp = posScore;
                                        posScore = negScore;
                                        negScore = temp;
                                    }
                                    else if(advPosScore >= advNegScore){
                                        if(posScore >= negScore){
                                            posScore = posScore + advPosScore;
                                        }
                                        else if(posScore < negScore){
                                            negScore = negScore + advPosScore;
                                        }
                                        
                                    }
                                }/*Getting adverb score close*/ 
                                
                            }/*if there is adverb close*/                         
                            positive += posScore;
                            negative += negScore;
                            sentPos += posScore;
                            sentNeg += negScore;
                            posScore = 0;
                            negScore = 0;
                        }/*getting word score close*/

                        dictionary.close();
                    }/*if noun, verb, adj or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
        
            }/*loop for traversing all sentences in a document close*/
            connect.close();    

            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            /*Recording into the file, the sentiment score for each document*/
            sb.append(fileName);
            sb.append(",");
            sb.append(positive/wordCount);
            sb.append(",");
            sb.append(negative/wordCount);
            sb.append('\n');
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
    }
    
    /*
    7th algorithm
        - Used for getting sentiment of multiple documents
        - With disambiguation
        - With negation
        - Records in a file called documentScores.csv to show results of sentiments for each document
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFileTwo(String fileName, String string, PrintWriter pw, StringBuilder  sb){

        double positive = 0;/*Total positive score of the document*/
        double negative = 0;/*Total negative scoe of the document*/
        double wordCount = 0;/*Counter for all the words used in calculating the sentiment*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            /*Connect to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*Total positive score for each sentence*/
                double sentNeg = 0;/*Total negative score for each sentence*/
                List <String> tags = sentences.get(sentCtr).posTags();/*Get all the part-of-speech tag for all the words in a sentence*/           
                List <String> words =  sentences.get(sentCtr).lemmas();/*Get all the root form for each word in a sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Load WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*Positive score for each word*/
                            double negScore = 0;/*Negative score for each word*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Get all the wordIDs for all senses/meaning for each word*/
                            /*if there are many wordIDs returned then disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            /*
                            Extract the wordID taken from WordNet
                            so that is will be useful in
                            searching the SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
  
                            /*
                            Check if verb or adjective has preceding adverb.
                            if there is adverb, combine the adverb score to the adjective or verb score
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*Get wordIDs that represent all the senses/meaning of a word*/
                                    /*If there are many senses or meanings, then disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    /*
                                    Extract the wordID taken from WordNet
                                    so that it will be useful in 
                                    searching SentiWordNet database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;
                                    double advNegScore = 0;
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
                                    //IMDB Files Test (3)
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore;
    //                                    negScore = temp;
    //                                }
    //                                if(advPosScore <= advNegScore){
    //                                    double temp = posScore;
    //                                    posScore = negScore + advPosScore;
    //                                    negScore = temp + advNegScore;
    //                                }
//                                    if(advPosScore < advNegScore){
//                                        double temp = posScore;
//                                        posScore = negScore + advPosScore;
//                                        negScore = temp + advNegScore;
//                                    }
//                                    else if(advPosScore >= advNegScore){
//                                        if(posScore >= negScore){
//                                            posScore = posScore + advPosScore;
//                                            negScore = negScore + advNegScore;
//                                        }
//                                        else if(posScore < negScore){
//                                            negScore = negScore + advPosScore;
//                                            posScore = posScore + advNegScore;
//                                        }
//                                        
//                                    }

//                                    if(advPosScore < advNegScore){
//                                        double temp = posScore;
//                                        posScore = negScore;
//                                        negScore = temp;
//                                    }
//                                    else if(advPosScore >= advNegScore){
//                                        if(posScore >= negScore){
//                                            posScore = posScore + advPosScore;
//                                        }
//                                        else if(posScore < negScore){
//                                            negScore = negScore + advPosScore;
//                                        }
//                                        
//                                    }
                                    if(advPosScore < advNegScore){
                                        double temp = posScore;
                                        posScore = negScore;
                                        negScore = temp + advNegScore;
                                    }
                                    else if(advPosScore >= advNegScore){
                                        if(posScore >= negScore){
                                            posScore = posScore + advPosScore;
                                        }
                                        else if(posScore < negScore){
                                            negScore = negScore + advPosScore;
                                        }
                                        
                                    }
                                }/*Get adverb score close*/ 
                                
                            }/*if there is adverb close*/
                            
                            positive += posScore;/*add positive word score to total positive score of document*/
                            negative += negScore;/*add negative word score to total negative score of document*/
                            sentPos += posScore;/*add positive word score to total positive score of sentence*/
                            sentNeg += negScore;/*add negative word score to total negative score of sentence*/
                            posScore = 0;
                            negScore = 0;
                        }/*get word score close*/

                        dictionary.close();
                    }/*If noun, verb, adj or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
        
            }/*loop for traversing all sentences in a document close*/
            connect.close();
            

            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            /*Record into a documentScores.csv file the score of a document*/
            sb.append(fileName);
            sb.append(",");
            sb.append(positive/wordCount);
            sb.append(",");
            sb.append(negative/wordCount);
            sb.append('\n');
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
    }
    
    /*
    8th algorithm
        - Used for getting sentiment of multiple documents
        - With disambiguation
        - With negation
        - Records in a file called documentScores.csv to show results of sentiments for each document
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFileThree(String fileName, String string, PrintWriter pw, StringBuilder  sb){

        double positive = 0;/*Total positive score of document*/
        double negative = 0;/*Total negative score of document*/
        double wordCount = 0;/*Counter for all the words used in the sentiment calculation*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            /*Connecting to SentiWordNet Database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*Total positive score of each sentence*/
                double sentNeg = 0;/*Total negative score of each sentence*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*Get all the part-of-speech tags for all words in the sentence*/
                List <String> words =  sentences.get(sentCtr).lemmas();/*get the root form of all the words in the sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Load WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*positive score of the word*/
                            double negScore = 0;/*negative score of the word*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Get all the wordIDs that represent all the senses/meanings of the word*/
                            
                            /*If there are many wordIDs, disambiguate */
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }

                            /*
                            Extract the wordID taken from WordNet
                            so that we can use it 
                            in searchin the SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                         
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
  
                            /*
                            Check if a verb or adjective has a preceding adverb
                            if there is combine the score of the adverb to the verb or adjective
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*Get wordIDs that represent all the senses/meanings of the word*/
                                    /*if there are many wordIDs, disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }

                                    /*
                                    Extract the wordID taken from WordNet
                                    so that the ID# will be used
                                    in searching SentiWordNet database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);

                                    double advPosScore = 0;/*positive score of the adverb*/
                                    double advNegScore = 0;/*negative score of the adverb*/
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
    
                                    if(advPosScore < advNegScore){
//                                        double temp = posScore;
//                                        posScore = negScore + advPosScore;
//                                        negScore = temp + advNegScore;
                                        posScore = posScore + advPosScore;
                                        negScore = negScore + advNegScore;
    
                                    }
                                    else{
                                        if(posScore >= negScore){
                                            posScore = posScore + advPosScore;
                                            negScore = negScore + advNegScore;
                                        }
                                        else{
                                            negScore = negScore + advPosScore;
                                            posScore = posScore + advNegScore;
                                        }  
                                    }

                                }/*get adverb score close*/ 
                                
                            }/*If there is a preceding adverb close*/
                            
                            positive += posScore;/*adding the positive score of the word to the positive score of the entire document*/
                            negative += negScore;/*adding the negative score of the word to the negative score of the entire document*/
                            sentPos += posScore;/*adding the positive score of the word to the positive score of the entire sentence*/
                            sentNeg += negScore;/*adding the negative score of the word to the negative score of the entire sentence*/
                            posScore = 0;
                            negScore = 0;
                        }/*get score of word close*/

                        dictionary.close();
                    }/*if noun, verb, adj, or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
        
            }/*loop for traversing all the sentences in a document*/
            connect.close();
            

            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            /*Record into documentScores.csv file the sentiment score of the document*/
            sb.append(fileName);
            sb.append(",");
            sb.append(positive/wordCount);
            sb.append(",");
            sb.append(negative/wordCount);
            sb.append('\n');
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
    }
    
    /*
    9th algorithm
        - Used for getting sentiment of multiple documents
        - With disambiguation
        - With negation
        - Records in a file called documentScores.csv to show results of sentiments for each document
        - Calculates sentiment score by getting the average of the sum of the sentiment scores of all words of a certain part-of-speech 
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFileFour(String fileName, String string, PrintWriter pw, StringBuilder  sb){

        double positive = 0;/*total positive score of document*/
        double negative = 0;/*total negative score of document*/
        double wordCount = 0;/*Counter for all wrods used in the sentiment calculation*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            /*Connect to the SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*total positive score for a sentence*/
                double sentNeg = 0;/*total negative score for a sentence*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*get the part-of-speech tags for all words in a sentence*/
                List <String> words =  sentences.get(sentCtr).lemmas();/*get the root form of all the words in a sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*loading the WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*positive score for a word*/
                            double negScore = 0;/*negative score for a word*/
                            
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*get all the wordIDs of a word that represents all the senses/meanings of a word*/
                            
                            /*if there are many wordIDs/senses then disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }

                            /*
                            Extract the wordID taken from WordNet
                            so that it can be used in
                            searching SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                            
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
                            
                            positive += posScore;/*adding the positive score of the word to the total positive score of the document*/
                            negative += negScore;/*adding the negative score of the word to the total negative score of the document*/
                            sentPos += posScore;/*adding the positive score of the word to the total positive score of the sentence*/
                            sentNeg += negScore;/*adding the negative score of the word to the total negative score of the sentence*/

                            posScore = 0;
                            negScore = 0;
                        }/*get score of the word close*/

                        dictionary.close();
                    }/*if noun, verb, adj, or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
        
            }/*loop for traversing all sentences in a document close*/
            connect.close();
            
            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            /*Record into documentScores.csv file the sentiment score of the document*/
            sb.append(fileName);
            sb.append(",");
            sb.append(positive/wordCount);
            sb.append(",");
            sb.append(negative/wordCount);
            sb.append('\n');
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }        
    }    
    
    /*
    10th algorithm
        - Used for getting sentiment of multiple documents
        - With disambiguation
        - With negation
        - Records in a file called documentScores.csv to show results of sentiments for each document
        - Calculates sentiment score with the use of adjective-adverb and verb-adverb relationship
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFileFive(String fileName, String string, PrintWriter pw, StringBuilder  sb){

        double positive = 0;/*total positive score of the document*/
        double negative = 0;/*total negative score of document*/
        double wordCount = 0;/*Counter of words used in the sentiment calculation*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            /*Connecting to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*total positive score of a sentence*/
                double sentNeg = 0;/*total negative score of a sentence*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*Get the part-of-speech tag for all words in a sentence*/            
                List <String> words =  sentences.get(sentCtr).lemmas();/*Get the root form of all the words in a sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*Loading WordNet Dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*positive score of a word*/
                            double negScore = 0;/*negative score of a word*/
                            
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*Get wordIDs that represent all the senses/meanings of a word*/
                            /*If there are many wordIDs, disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }

                            /*
                            Extract numerical idea taken from WordNet
                            so that this will be useable
                            in searching the SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                            
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }

                            /*
                            Checking if a verb or adjective has a preceding adverb
                            If there is combine the adverb score to the verb or adjective score
                            */
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*Getting the wordIDs of all senses/meanings of a word*/
                                    
                                    /*if there are many wordIDs then disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;/*positive score of the adverb*/
                                    double advNegScore = 0;/*negative score of the adverb*/
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
                                    
                                    /*combining of the adjective or verb score to the adverb score*/
                                    double objectiveScore = 1 - (posScore + negScore); 
                                    posScore = posScore + (objectiveScore * advPosScore);
                                    negScore = negScore + (objectiveScore * advNegScore);

                                }/*get the score of the adverb close*/ 
                                
                            }/*if there is preceding adverb close*/
                            
                            positive += posScore;/*adding the positive score of the word to the total positive score of the document*/
                            negative += negScore;/*adding the negative score of the word to the total negative score of the document*/
                            sentPos += posScore;/*adding the positive score of the word to the total positive score of the sentence*/
                            sentNeg += negScore;/*adding the negative score of the word to the total negative score of the sentence*/

                            posScore = 0;
                            negScore = 0;
                        }/*get word score close*/

                        dictionary.close();
                    }/*if noun, verb, adj or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
        
            }/*loop for traversing all sentences in a document close*/
            connect.close();
            

            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            /*Record into documentScores.csv file the sentiment score of the document*/
            sb.append(fileName);
            sb.append(",");
            sb.append(positive/wordCount);
            sb.append(",");
            sb.append(negative/wordCount);
            sb.append('\n');
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }           
    }    
    
    /*
    11th algorithm
        - Used for getting sentiment of multiple documents
        - With disambiguation
        - With negation
        - Records in a file called documentScores.csv to show results of sentiments for each document
        - Calculates sentiment score with the use of adjective-adverb relationship
    */
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFileSix(String fileName, String string, PrintWriter pw, StringBuilder  sb){

        double positive = 0;/*total positive score of the document*/
        double negative = 0;/*total negative score of the document*/
        double wordCount = 0;/*Counter for all the words used in the calculation of sentiment score*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            /*Connecting to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*total positive score for a sentence*/
                double sentNeg = 0;/*total negative score for a sentence*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*get part-of-speech tags for all words in a sentence*/            
                List <String> words =  sentences.get(sentCtr).lemmas();/*get the root form of all the words in a sentence*/
                
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*loading WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*positive score of a word*/
                            double negScore = 0;/*negative score of a word*/
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*get the wordIDs of all the senses/meanings of a word*/
                            /*if there are many wordIDs, disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }

                            /*
                            Extracting the numeric wordID taken from WordNet
                            so that a numeric ID will be 
                            used in searching SentiWordNet
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                          
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
  
                            /*
                            checking adjectives if they have preceding adverbs
                            if there is an adverb, then combine the adverb score with the adjective score
                            */
                            String adverb = "";
                            if(tags.get(lemmaTagIndex).matches("JJ(R|S)?") && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*Get the wordIDs for all the senses/meanings of the word*/
                                    /*if there are many wordIDs, disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    
                                    /*
                                    Extractin the numeric ID from the wordID taken from wordNet
                                    so that this numerics ID will useable in
                                    searchin SentiWordNet database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);
                                    double advPosScore = 0;/*positive adverb score*/
                                    double advNegScore = 0;/*negative adverb score*/
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
                                    
                                    /*combining the adverb score to adjective score*/
                                    double objectiveScore = 1 - (posScore + negScore); 
                                    posScore = posScore + (objectiveScore * advPosScore);
                                    negScore = negScore + (objectiveScore * advNegScore);

                                }/*get adverb score close*/
                                
                            }/*if there is adverb close*/
                            
                            positive += posScore;/*adding the positive score of the word to the total positive score of the document*/
                            negative += negScore;/*adding the negative score of the word to the total negative score of the document*/
                            sentPos += posScore;/*adding the positive score of the word to the total positive score of the sentence*/
                            sentNeg += negScore;/*adding the negative score of the word to the total negative score of the sentence*/

                            posScore = 0;
                            negScore = 0;
                        }/*get word score close*/

                        dictionary.close();
                    }/*if noun, verb, adj or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
        
            }/*loop for traversing all sentences in a document close*/
            connect.close();
            
            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            /*record into documentScores.csv file the sentiment score of the document*/
            sb.append(fileName);
            sb.append(",");
            sb.append(positive/wordCount);
            sb.append(",");
            sb.append(negative/wordCount);
            sb.append('\n');
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
    }
    
    /*
    12th algorithm
        - Used for getting sentiment of 1 document only
        - With disambiguation
        - With negation
        - returns as string the positive and negative score of the document
        - Calculates sentiment score with the use of adjective-adverb relationship
    */
    public static String getSentimentofOneDocument(String string){

        String retValSentimentScore = "";
        
        double positive = 0;/*total positive score of the document*/
        double negative = 0;/*total negative score of the document*/
        double wordCount = 0;/*counter for the number of words included in the sentiment calculation*/
        
        positiveSentencesList.clear();/*emptying the positive sentences list*/
        negativeSentencesList.clear();/*emptying the negative sentences list*/
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            
            /*Connecting to SentiWordNet database*/
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                
                double sentPos = 0;/*total positive score for a sentence*/
                double sentNeg = 0;/*total negative score for a sentence*/
                
                List <String> tags = sentences.get(sentCtr).posTags();/*get the part-of-speech tags for all the words in the sentence*/
                List <String> words =  sentences.get(sentCtr).lemmas();/*get the root form of all the words in the sentence*/
            
                /*
                Calculating sentiment using different combinations of
                nouns(NN), verbs(VB), adjectives(JJ) and adverbs(RB)
                */
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();/*loading the WordNet dictionary for accessing*/
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;/*positive score of a word*/
                            double negScore = 0;/*negative score of a word*/
                            
                            List<IWordID> wordIDs = indexWord.getWordIDs();/*get the wordIDs for all the senses/meanings of a word*/
                            
                            /* if there are many wordIDs/senses, disambiguate*/
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            
                            /*
                            Extract the numeric part of the wordID taken from wordNet
                            so that the wordID can be used
                            in searing SentiWordNet database
                            */
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");                        
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }


                            /*
                            Check if word is adjective then check if there is adverb that preceeds it
                            if there is an adverb then combine the adverb score to the adjective score
                            */
                            String adverb = "";
                            if(tags.get(lemmaTagIndex).matches("JJ(R|S)?") && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), GetPOSTag(tags.get(lemmaTagIndex-1)));
                                
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();/*get the wordIDs of all the senses/meanings of the word*/
                                    /*if there are many wordIDs or senses, disambiguate*/
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    
                                    /*
                                    Extract the numeric part of the wordID taken from wordNet
                                    so that the wordID can be used
                                    in searing SentiWordNet database
                                    */
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
                                    results = stmt.executeQuery(sqlStmtwordID);

                                    double advPosScore = 0;/*positive adverb score*/
                                    double advNegScore = 0;/*negative adverb score*/
                                    if(results.next()){
                                        advPosScore = results.getFloat("PosScore");
                                        advNegScore = results.getFloat("NegScore");
                                    }
                                    
                                    /*Combine score of adverb and adjective*/
                                    double objectiveScore = 1 - (posScore + negScore); 
                                    posScore = posScore + (objectiveScore * advPosScore);
                                    negScore = negScore + (objectiveScore * advNegScore);

                                }/*get adverb score close*/ 
                                
                            }/*if adjective has preceding adverb close*/
                            
                            /*
                            Add the sentiment of word to total score of document and to the total score of its sentence                      
                            */
                            positive += posScore;
                            negative += negScore;
                            sentPos += posScore;
                            sentNeg += negScore;
                            
                            posScore = 0;
                            negScore = 0;
                        }/*get word score close*/

                        dictionary.close();
                    }/*if noun, verb, adj, or adv close*/
                    
                }/*loop for traversing all words in a sentence close*/
                
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                /*add sentence into negative or positive list of sentencese*/
                if(sentPos >= sentNeg){
                    positiveSentencesList.add(sentences.get(sentCtr).text());
                }
                else{
                    negativeSentencesList.add(sentences.get(sentCtr).text());
                }
            }/*loop for traversing all sentences in a document close*/
            
            connect.close();
            
            write("Positive: " + positive/wordCount);
            write("Negtive: " + negative/wordCount);
            write("Word Count: " + wordCount);

            //Get average of scores and put the scores into a string
            retValSentimentScore = retValSentimentScore + (positive/wordCount);
            retValSentimentScore = retValSentimentScore + ",";
            retValSentimentScore = retValSentimentScore + (negative/wordCount);
            
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
        
          return retValSentimentScore;
    }
    
    /*
    Disambiguation is the identification of the correct meaning of the word as words could
    tend to have many meanings.
    
    This is the disambiguation code
    it uses the Lesk Algorithm.
    
    Arguments needed:
        * indexWord - this is used for accessing WordNet. This is used to access the word to be disambiguated
        * documet - the entire text consisting of all the sentences of the the text to be evaluated
        * sentenceIndex - the index of the sentence. sentences of a document is put in a list. this index
            will be used in accessing the right sentence in the list.
        *lemmaIndex - index of the word being disambiguated. all words in a sentence are put in a list. this index will
            be usd in accessing the correct word to be disambiguated
    */
    private static int Disambiguate(IIndexWord indexWord, Document document, int sentenceIndex, int lemmaIndex){
        
        int indexOfWordToBeUsed = 0;
        try
        {
            IDictionary dictionary = WordNetAccess.loadDic();/*loading WordNet dictionary*/
            dictionary.open();

            int wordSensesSize = indexWord.getWordIDs().size();/*get the number of senses/meanings a word has*/
            int[] wordSenseScores = new int[wordSensesSize];
            List<IWordID> wordSenses = indexWord.getWordIDs();/*get wordIDs of all the senses/meaningg of the words*/

            
            int wordSenseTraverseCtr = 0;
            while(wordSenseTraverseCtr != wordSensesSize){/*traverse all the wordIDs or wordSenses*/

                IWord word = dictionary.getWord(wordSenses.get(wordSenseTraverseCtr));

                String glossOfWordSense = word.getSynset().getGloss();/*get the dictionary meaning or gloss of the word*/

                List<StringAndTag> compareToWords = GetCompareToWords(document, sentenceIndex, lemmaIndex);/*Get 10 neighboring words of the word being disambiguated*/

                for(StringAndTag wordWithTag : compareToWords){/*Compare the meanings of neighbouring words and the meanings of the word disambiguated*/
                    IIndexWord indexWordOfNeighborWord = dictionary.getIndexWord(wordWithTag.word, wordWithTag.tag);
                    
                    if(indexWordOfNeighborWord != null){
                        List<IWordID> iWordIDsOfNeighborWord = indexWordOfNeighborWord.getWordIDs();/*get all the wordIds of the senses of a neighbor word*/

                        int sameWordsCtr = 0;/*counter of words that both occur in the gloss of the disambiguated word and the gloss of a neighbor word*/

                        for(IWordID iWordIDOfNeighborWord : iWordIDsOfNeighborWord){/*traverse all neighbor word wordIDs*/
                            String oneGlossOfNeighborWord = dictionary.getWord(iWordIDOfNeighborWord).getSynset().getGloss();
                            String[] glossOfWordSenseStringArray = glossOfWordSense.split(" ");/*array of words of the gloss of the disambiguated word*/
                            String[] oneGlossOfNeighborWordStringArray = oneGlossOfNeighborWord.split(" ");/*array of words of the gloss of the neighbor word*/
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

                        wordSenseScores[wordSenseTraverseCtr] = sameWordsCtr;/*record into an array the number of similar words of disambiguated word and neighbor word*/
                    }/*getting of wordIDs of neighbor word close*/
                    
                }/*loop for traversing all neighbor words close*/
                wordSenseTraverseCtr++;
            }/*loop for traversing all wordIDs of disambiguated word close*/
            
            /*Choose the meaning to be used*/
            int largestCnt = wordSenseScores[0];
            for(int travCntr = 1; travCntr < wordSensesSize; travCntr++){
                if(largestCnt > wordSenseScores[travCntr]){
                    indexOfWordToBeUsed = travCntr;
                    largestCnt = wordSenseScores[travCntr];
                }
            }
            dictionary.close();
        }/*try close*/
        catch(Exception exc){
            exc.printStackTrace();
        }
        
        return indexOfWordToBeUsed;
    }
    
    /*
    This returns 10 neighboring words of the disambiguated word. 5 words to the left of the word,
    5 words to the right. if the number of words is not enough on one of either sides then,
    the number of words on the side with more words compensate.
    
    arguments for the method
        *docu - This is the entire text of the evaluated document
        *sentenceIndex - the index of the sentence where the word dismabiguated belongs to.
            this index is used to access the list of sentences a document has
        *lemmaIndex - index of the word disambiguated
    */
    private static List<StringAndTag> GetCompareToWords(Document docu, int sentenceIndex, int lemmaIndex){
        
        List<StringAndTag> compareToWords = new ArrayList<StringAndTag>();
        try{

            int compareToWordsFullSize = 10;
            boolean full = false;
            int sentenceIndexPrev = sentenceIndex;
            int lemmaIndexPrev = lemmaIndex;
            int sentenceIndexPrec = sentenceIndex;
            int lemmaIndexPrec = lemmaIndex;

            List<Sentence> sentences = docu.sentences();
            Sentence currentSentence;
            List<String> lemmas;
            while(!full){
                if(sentenceIndexPrev >= 0){
                    currentSentence = sentences.get(sentenceIndexPrev);
                    lemmas = currentSentence.lemmas();
                    if((--lemmaIndexPrev) >= 0){
                        if(currentSentence.posTag(lemmaIndexPrev).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                            if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
//                                compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
//                                write("Compare to words size: " + compareToWords.size() + "\n");
//                            }
                        compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexPrev),GetPOSTag(currentSentence.posTag(lemmaIndexPrev))));
                                if(compareToWords.size() == compareToWordsFullSize){
                                full = true;
                            }
                        }
                    }
                    else{
                        if((--sentenceIndexPrev) >= 0){
                            Sentence currentSentenceTemp = sentences.get(sentenceIndexPrev);
                            lemmaIndexPrev = currentSentenceTemp.lemmas().size()-1;
                        }
                    }
                    
                }
                if(sentenceIndexPrec < sentences.size()){
                    currentSentence = sentences.get(sentenceIndexPrec);
                    lemmas = currentSentence.lemmas();
                    if((++lemmaIndexPrec) < lemmas.size()){
                        if(currentSentence.posTag(lemmaIndexPrec).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                            if(IfUniqueWord(compareToWords,lemmas.get(lemmaIndexCurrent))){
//                                compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexCurrent),GetPOSTag(currentSentence.posTag(lemmaIndexCurrent))));
//                                write("Compare to words size: " + compareToWords.size() + "\n");
//                            }
                            compareToWords.add(new StringAndTag(lemmas.get(lemmaIndexPrec),GetPOSTag(currentSentence.posTag(lemmaIndexPrec))));
                            
                        }
                    }
                    else{
                        sentenceIndexPrec++;
                        lemmaIndexPrec = 0;
                    }
                    
                }
                if(compareToWords.size() == compareToWordsFullSize){
                    full = true;
                }
                if(sentenceIndexPrec >= sentences.size() && sentenceIndexPrev < 0){
                    full = true;
                }
            }   
        }
        catch(Exception exc){
            exc.printStackTrace();
        } 
        
        return compareToWords;
        
    }
    
    private static boolean IfUniqueWord(List<String> words,List<String> POS, String word, String POSofWord){
        
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
    
    private static boolean IfUniqueWord(List<StringAndTag> listOfWords, String word){
        
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
    
    public static String printPositiveNegativeStatements(){
            
        String classifiedString = "";
            
            
        classifiedString += "Positive" + "\n\n";
        for(Document answer : positiveSentences){
            List <Sentence> sentences = answer.sentences();

            classifiedString += ">";

            for(Sentence sentence : sentences){
                classifiedString += sentence.text();
            }

            classifiedString += "\n";
        }

        classifiedString += "\n" + "Negative" + "\n\n";
        for(Document answer : negativeSentences){
            List <Sentence> sentences = answer.sentences();

            classifiedString += ">";

            for(Sentence sentence : sentences){
                classifiedString += sentence.text();
            }

            classifiedString += "\n";
        }

        classifiedString += "\n" + "Neutral" + "\n\n";
        for(Document answer : neutralSentences){
            List <Sentence> sentences = answer.sentences();

            classifiedString += ">";

            for(Sentence sentence : sentences){
                classifiedString += sentence.text();
            }

            classifiedString += "\n";
        }

        return classifiedString;
            
    }
    
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
