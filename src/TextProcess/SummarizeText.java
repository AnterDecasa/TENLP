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
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Optional;
import javax.swing.JTextArea;

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
    
    public static List<String> positiveWords = new ArrayList<String>();
    public static List<String> negativeWords = new ArrayList<String>();
    
    public static void summarizeDocument(){
        
    }
    
    public static void getSentimentofWholeDocument(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
//        write(noQuestions);
        
        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
        
            for(Sentence sent : docu.sentences()){
    //            write(sent.parse());
                List <String> tags = sent.posTags();
            
                List <String> words =  sent.lemmas();
            
                int index = 0;
                for(;index < tags.size(); index++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(index), LanguageProcess.GetPOSTag(tags.get(index)));
                        if(indexWord != null){
                            wordCount++;
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            double posScore = 0;
                            double negScore = 0;    
                            double senseCtr = 0;
                            IWord word = dictionary.getWord(wordIDs.get(0));
                            write(word.getLemma());
//                            write("ID of word: " + wordIDDisected[1]);
                            String sqlStmtwordID = "SELECT * FROM dict WHERE (SynsetTerms LIKE '%"+ word.getLemma() +"#%' OR SynsetTerms LIKE '%"+ word.getLemma() +"#%') AND (PosScore > 0 AND NegScore > 0)";
//                                write(sqlStmtwordID);
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
                        }

                        dictionary.close();
                    
                    }
                }
            }
            connect.close();
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
    }
    
    public static void listPosNeg(){
        write("Positive");
        for(String word : positiveWords){
            write(word);
        }
        write("Negative");
        for(String word : negativeWords){
            write(word);
        }
        
    }
    
    public static void getSentimentofWholeDocumentWithNegation(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
//        write(noQuestions);
        
        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
        
            for(Sentence sent : docu.sentences()){
    //            write(sent.parse());
                write(sent.text());
                double sentPos = 0;
                double sentNeg = 0;
                List <String> tags = sent.posTags();
            
                List <String> words =  sent.lemmas();
            
                int index = 0;
                for(;index < tags.size(); index++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(index), LanguageProcess.GetPOSTag(tags.get(index)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;
                            double negScore = 0;    
                            double senseCtr = 0;
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            IWord word = dictionary.getWord(wordIDs.get(0));
//                            write(word.getLemma());
//                            write("ID of word: " + wordIDDisected[1]);
                            String sqlStmtwordID = "SELECT * FROM dict WHERE (SynsetTerms LIKE '%"+ word.getLemma() +"#%' OR SynsetTerms LIKE '%"+ word.getLemma() +"#%') AND (PosScore > 0 AND NegScore > 0)";
//                                write(sqlStmtwordID);
                            results = stmt.executeQuery(sqlStmtwordID);
                            while(results.next()){
//                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                                senseCtr++;
                            }
//                            write("Sense Count: " + senseCtr++);
                            if((tags.get(index).matches("JJ(R|S)?") || tags.get(index).matches("VB(D|G|N|P|Z)?")) && (index > 0 && tags.get(index-1).matches("RB(S|R)?"))){
                                wordCount++;
                                indexWord = dictionary.getIndexWord(words.get(index-1), LanguageProcess.GetPOSTag(tags.get(index-1)));
                                wordIDs = indexWord.getWordIDs();
                                word = dictionary.getWord(wordIDs.get(0));
                                write(word.getLemma());
    //                            write("ID of word: " + wordIDDisected[1]);
                                sqlStmtwordID = "SELECT * FROM dict WHERE (SynsetTerms LIKE '%"+ word.getLemma() +"#%' OR SynsetTerms LIKE '%"+ word.getLemma() +"#%') AND (PosScore > 0 AND NegScore > 0)";
    //                                write(sqlStmtwordID);
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
                            }
                            positive += posScore/senseCtr;
                            negative += negScore/senseCtr;
                            sentPos += posScore/senseCtr;
                            sentNeg += negScore/senseCtr;
                            write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                            senseCtr = 0;
                            posScore = 0;
                            negScore = 0;
                        }

                        dictionary.close();
                    }
                    
                }
            }
            connect.close();
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
    }
    
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguation(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
//        noQuestions = TextFilePreProcess.correctPeriodsPutSpaceAfter(noQuestions);
        
//        write(noQuestions);

        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            
            PrintWriter pw = new PrintWriter(new File("sentenceScores.csv"));
            StringBuilder sb = new StringBuilder();
            
            PrintWriter cleanText = new PrintWriter(new File("cleanText.txt"));
            StringBuilder sbCleanText = new StringBuilder();
            
            sb.append("Sentence,Positive,Negative\n");
        
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                sbCleanText.append(sentences.get(sentCtr).text());
                sbCleanText.append(" ");
                double sentPos = 0;
                double sentNeg = 0;
                List <String> tags = sentences.get(sentCtr).posTags();
            
                List <String> words =  sentences.get(sentCtr).lemmas();
            
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), LanguageProcess.GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;
                            double negScore = 0;
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            IWord word= dictionary.getWord(wordIDs.get(indexForSense));
//                            write(word.getLemma());
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                            
//                            write("ID of word: " + wordIDDisected[1]);
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
//                            write(sqlStmtwordID);
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
//                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
//                            write("Sense Count: " + senseCtr++);
//                            write((" "+ words.get(lemmaTagIndex)).trim() + "\nPosScore: " + posScore + " NegScore: " + negScore);  
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), LanguageProcess.GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
//                                    write("ID of word: " + wordIDDisected[1]);
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
//                                    write("ID of word: " + wordIDDisected[1]);
//                                    write(sqlStmtwordID);
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
                                } 
                                
                            }
//                            write((adverb + " "+ words.get(lemmaTagIndex)).trim() + "\nPosScore: " + posScore + " NegScore: " + negScore);  
                            if(posScore >= negScore){
                                if(posScore != 0)    
                                    positiveWords.add((adverb + " "+ words.get(lemmaTagIndex)).trim());
                            }
                            else{
                                
                                    negativeWords.add((adverb + " "+ words.get(lemmaTagIndex)).trim());
                            }
                            
                            positive += posScore;
                            negative += negScore;
                            sentPos += posScore;
                            sentNeg += negScore;
//                            write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                            posScore = 0;
                            negScore = 0;
                        }

                        dictionary.close();
                    }
                    
                }
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                
                sb.append(sentences.get(sentCtr).text());
                sb.append(",");
                sb.append(sentPos);
                sb.append(",");
                sb.append(sentNeg);
                sb.append('\n');
        
            }
            connect.close();
            pw.write(sb.toString());
            pw.close();
            cleanText.write(sbCleanText.toString());
            cleanText.close();
            System.out.println("output sentences done!");
        }
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
//        write("Positive: " + positive);
//        write("Negtive: " + negative);
//        write("Word Count: " + wordCount);
    }
    
    public static String getSentimentofWholeDocumentWithNegationWithDisambiguationReturnString(String string){
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
//        noQuestions = TextFilePreProcess.correctPeriodsPutSpaceAfter(noQuestions);
        
//        write(noQuestions);

        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(noQuestions);
        
        write("Calculating sentiment...");
        try{
            
            PrintWriter pw = new PrintWriter(new File("sentenceScores.csv"));
            StringBuilder sb = new StringBuilder();
            
            PrintWriter cleanText = new PrintWriter(new File("cleanText.txt"));
            StringBuilder sbCleanText = new StringBuilder();
            
            sb.append("Sentence,Positive,Negative\n");
        
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                sbCleanText.append(sentences.get(sentCtr).text());
                sbCleanText.append(" ");
                double sentPos = 0;
                double sentNeg = 0;
                List <String> tags = sentences.get(sentCtr).posTags();
            
                List <String> words =  sentences.get(sentCtr).lemmas();
            
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), LanguageProcess.GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;
                            double negScore = 0;
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            IWord word= dictionary.getWord(wordIDs.get(indexForSense));
//                            write(word.getLemma());
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                            
//                            write("ID of word: " + wordIDDisected[1]);
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
//                            write(sqlStmtwordID);
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
//                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
//                            write("Sense Count: " + senseCtr++);
//                            write((" "+ words.get(lemmaTagIndex)).trim() + "\nPosScore: " + posScore + " NegScore: " + negScore);  
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), LanguageProcess.GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
//                                    write("ID of word: " + wordIDDisected[1]);
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
//                                    write("ID of word: " + wordIDDisected[1]);
//                                    write(sqlStmtwordID);
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
                                } 
                                
                            }
//                            write((adverb + " "+ words.get(lemmaTagIndex)).trim() + "\nPosScore: " + posScore + " NegScore: " + negScore);  
                            if(posScore >= negScore){
                                if(posScore != 0)    
                                    positiveWords.add((adverb + " "+ words.get(lemmaTagIndex)).trim());
                            }
                            else{
                                
                                    negativeWords.add((adverb + " "+ words.get(lemmaTagIndex)).trim());
                            }
                            
                            positive += posScore;
                            negative += negScore;
                            sentPos += posScore;
                            sentNeg += negScore;
//                            write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                            posScore = 0;
                            negScore = 0;
                        }

                        dictionary.close();
                    }
                    
                }
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                
                sb.append(sentences.get(sentCtr).text());
                sb.append(",");
                sb.append(sentPos);
                sb.append(",");
                sb.append(sentNeg);
                sb.append('\n');
        
            }
            connect.close();
            pw.write(sb.toString());
            pw.close();
            cleanText.write(sbCleanText.toString());
            cleanText.close();
            System.out.println("output sentences done!");
        }
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
        
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);  
        
        return "Positive: " + positive/wordCount + "\n" + "Negtive: " + negative/wordCount + "\n" + "Word Count: " + wordCount;
        
//        write("Positive: " + positive);
//        write("Negtive: " + negative);
//        write("Word Count: " + wordCount);
    }
    
    public static void getSentimentFromCleanfile(String string, JTextArea resultsArea){
        resultsArea.setText("");
        resultsArea.setText(getSentimentofWholeDocumentWithNegationWithDisambiguationReturnString(string));
    } 
    
    public static void getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanText(String string){

        double positive = 0;
        double negative = 0;
        double wordCount = 0;
        
        Document docu = new Document(TextFilePreProcess.replaceAllPageBreaks(string));
        
        write("Calculating sentiment...");
        try{
            
            PrintWriter pw = new PrintWriter(new File("sentenceScores.csv"));
            StringBuilder sb = new StringBuilder();
            
            PrintWriter cleanText = new PrintWriter(new File("cleanText.txt"));
            StringBuilder sbCleanText = new StringBuilder();
            
            sb.append("Sentence,Positive,Negative\n");
        
            Connection connect = DriverManager.getConnection(host,user,password);
            Statement stmt = connect.createStatement();
            ResultSet results;
            
            List<Sentence> sentences = docu.sentences();
            for(int sentCtr = 0; sentCtr < sentences.size(); sentCtr++){
                
                write(sentences.get(sentCtr).text());
                write(sentences.get(sentCtr).parse());
                sbCleanText.append(sentences.get(sentCtr).text());
                sbCleanText.append(" ");
                double sentPos = 0;
                double sentNeg = 0;
                List <String> tags = sentences.get(sentCtr).posTags();
            
                List <String> words =  sentences.get(sentCtr).lemmas();
            
                int lemmaTagIndex = 0;
                for(;lemmaTagIndex < tags.size(); lemmaTagIndex++){
//                    if(tags.get(index).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(tags.get(lemmaTagIndex).matches("JJ(R|S)?")){
//                    if(tags.get(index).matches("JJ(R|S)?|RB(S|R)?")){
                        
                        IDictionary dictionary = WordNetAccess.loadDic();
                        dictionary.open();

                        IIndexWord indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex), LanguageProcess.GetPOSTag(tags.get(lemmaTagIndex)));
                        if(indexWord != null){
                            wordCount++;
                            double posScore = 0;
                            double negScore = 0;
                            List<IWordID> wordIDs = indexWord.getWordIDs();
                            int indexForSense = 0;
                            if(wordIDs.size() > 1){
                                indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex);
                            }
                            IWord word= dictionary.getWord(wordIDs.get(indexForSense));
//                            write(word.getLemma());
                            String[] wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
                            
//                            write("ID of word: " + wordIDDisected[1]);
                            String sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
//                            write(sqlStmtwordID);
                            results = stmt.executeQuery(sqlStmtwordID);
                            if(results.next()){
//                                write("Result: \t" + results.getInt("ID") + "\t|" + results.getFloat("PosScore") + "\t|" + results.getFloat("NegScore"));
                                posScore += results.getFloat("PosScore");
                                negScore += results.getFloat("NegScore");
                            }
//                            write("Sense Count: " + senseCtr++);
//                            write((" "+ words.get(lemmaTagIndex)).trim() + "\nPosScore: " + posScore + " NegScore: " + negScore);  
                            String adverb = "";
                            if((tags.get(lemmaTagIndex).matches("JJ(R|S)?") || tags.get(lemmaTagIndex).matches("VB(D|G|N|P|Z)?")) && (lemmaTagIndex > 0 && tags.get(lemmaTagIndex-1).matches("RB(S|R)?"))){
                                wordCount++;
                                adverb += words.get(lemmaTagIndex-1);
                                indexWord = dictionary.getIndexWord(words.get(lemmaTagIndex-1), LanguageProcess.GetPOSTag(tags.get(lemmaTagIndex-1)));
                                if(indexWord != null){
                                    wordIDs = indexWord.getWordIDs();
                                    indexForSense = 0;
                                    if(wordIDs.size() > 1){
                                        indexForSense = Disambiguate(indexWord, docu, sentCtr,lemmaTagIndex-1);;
                                    }
                                    wordIDDisected = wordIDs.get(indexForSense).toString().split("-");
//                                    write("ID of word: " + wordIDDisected[1]);
                                    sqlStmtwordID = "SELECT * FROM dict WHERE ID = "+ wordIDDisected[1];
//                                    write("ID of word: " + wordIDDisected[1]);
//                                    write(sqlStmtwordID);
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
                                } 
                                
                            }
//                            write((adverb + " "+ words.get(lemmaTagIndex)).trim() + "\nPosScore: " + posScore + " NegScore: " + negScore);  
                            if(posScore >= negScore){
                                if(posScore != 0)    
                                    positiveWords.add((adverb + " "+ words.get(lemmaTagIndex)).trim());
                            }
                            else{
                                
                                    negativeWords.add((adverb + " "+ words.get(lemmaTagIndex)).trim());
                            }
                            
                            positive += posScore;
                            negative += negScore;
                            sentPos += posScore;
                            sentNeg += negScore;
//                            write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                            posScore = 0;
                            negScore = 0;
                        }

                        dictionary.close();
                    }
                    
                }
                write("Sent Pos: " + sentPos + " Sent Neg: " + sentNeg);
                
                sb.append(sentences.get(sentCtr).text());
                sb.append(",");
                sb.append(sentPos);
                sb.append(",");
                sb.append(sentNeg);
                sb.append('\n');
        
            }
            connect.close();
            pw.write(sb.toString());
            pw.close();
            cleanText.write(sbCleanText.toString());
            cleanText.close();
            System.out.println("output sentences done!");
        }
        catch(Exception exc){
            exc.printStackTrace();
            exc.getMessage();
        }
           
        write("Positive: " + positive/wordCount);
        write("Negtive: " + negative/wordCount);
        write("Word Count: " + wordCount);
//        write("Positive: " + positive);
//        write("Negtive: " + negative);
//        write("Word Count: " + wordCount);
    }
    
    private static int Disambiguate(IIndexWord indexWord, Document document, int sentenceIndex, int lemmaIndex){
        
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

                List<StringAndTag> compareToWords = GetCompareToWords(document, sentenceIndex, lemmaIndex);
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
            dictionary.close();
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
//        write("Exiting Disambiguate");
        return indexOfWordToBeUsed;
        
    }
    
    private static List<StringAndTag> GetCompareToWords(Document docu, int sentenceIndex, int lemmaIndex){
        
//        write("Inside of GetCompareToWords");
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
                            Sentence currentSentenceTemp = sentences.get(sentenceIndexPrev);
                            lemmaIndexPrev = currentSentenceTemp.lemmas().size()-1;
                        }
                    }
                    
                }
                if(sentenceIndexPrec < sentences.size()){
                    currentSentence = sentences.get(sentenceIndexPrec);
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
            }   
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
        
//        write("Exiting GetCompareToWords");
        return compareToWords;
        
    }
    
    public static void summarizeZeroOneNegative(String string){
        
        try{
        
            string = TextFilePreProcess.removeCarets(string);
            String[] stringArray = string.split("\\r?\\n");
        
            groupedAnswers = groupAnswers(stringArray);
        
            //Get sentimentScore for each answer
            write("Teacher Strength");
            int answerIndex = 0;
            List<Document> currentGroup = groupedAnswers.getTeachStrength();
//            printAnswerGroup(currentGroup);
            for(; answerIndex < groupedAnswers.getTeachStrength().size(); answerIndex++){ 
                int polarity = 0;
                polarity += GetSentimentReturnZeroOneNegative(currentGroup,answerIndex, 0);
                AssignToPosNegZeroOneNegative(polarity,currentGroup.get(answerIndex));
                write("Score:" + polarity);
            }
            //TeachWeakness
            write("\nTeacher Weakness");
            answerIndex = 0;
            currentGroup = new ArrayList<Document>();
            currentGroup = groupedAnswers.getTeachWeak();
//            printAnswerGroup(currentGroup);
            for(; answerIndex < groupedAnswers.getTeachWeak().size(); answerIndex++){ 
                int polarity = 0;
                polarity += GetSentimentReturnZeroOneNegative(currentGroup,answerIndex, 0);
                AssignToPosNegZeroOneNegative(polarity,currentGroup.get(answerIndex));
                write("Score:" + polarity);
            }
//            
            //Comments
            write("\nComment");
            answerIndex = 0;
            currentGroup = new ArrayList<Document>();
            currentGroup = groupedAnswers.getComments();
//            printAnswerGroup(currentGroup);
            for(; answerIndex < groupedAnswers.getComments().size(); answerIndex++){ 
                int polarity = 0;
                polarity += GetSentimentReturnZeroOneNegative(currentGroup,answerIndex, 0);
                AssignToPosNegZeroOneNegative(polarity,currentGroup.get(answerIndex));
                write("Score:" + polarity);
            }
            
            write("Done");
            
        }
        
        catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
    private static int GetSentimentReturnZeroOneNegative(List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
//        write("Inside GetSentiment");
        int sentimentScore = 0;
        double positive = 0;
        double negative = 0;
        int wordCount = 0;
        try{
        
            //Get sense for each word
            List<Sentence> answerSentences = compareToGroupedAnswers.get(answerIndex).sentences();
            int answerSentencesSize = answerSentences.size();
        
            IDictionary dictionary = WordNetAccess.loadDic();
            dictionary.open();
            for(int sentenceCtr = 0; sentenceCtr < answerSentencesSize; sentenceCtr++){
            
                Sentence currentSentence = answerSentences.get(sentenceCtr);
//                write(currentSentence.text());
                write(currentSentence.parse());
                int lemmaSize = currentSentence.lemmas().size();
            
                for(int lemmaCtr = 0; lemmaCtr < lemmaSize; lemmaCtr++){
                
                    String currentPOSTag = currentSentence.posTag(lemmaCtr);
//                    if(currentPOSTag.matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(currentPOSTag.matches("JJ(R|S)?|VB(D|G|N|P|Z)?")){
//                    if(currentPOSTag.matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(currentPOSTag.matches("JJ(R|S)?|RB(S|R)?")){
//                    if(currentPOSTag.matches("JJ(R|S)?")){
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
                                    positive += results.getFloat("PosScore");
                                    negative += results.getFloat("NegScore");
                                    wordCount++;
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
                                    positive += results.getFloat("PosScore");
                                    negative += results.getFloat("NegScore");
                                    wordCount++;
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
        
        write("Pos: " + positive + " Neg: " + negative);
        if(positive >= negative){
            sentimentScore = 1;
        }
        else if(negative > positive){
            sentimentScore = -1;
        }

        return sentimentScore;
        
    }
    
    private static void AssignToPosNegZeroOneNegative(int polarity,  Document answer){
//        if(negativeCount == 0){
//            neutralSentences.add(answer);
//        }
//        else{
        if(polarity == -1){
            negativeSentences.add(answer);
        }
        else if(polarity == 1) {
            positiveSentences.add(answer);
        }
        else{
            neutralSentences.add(answer);
        }
//        }

    }
    
    public static void summarizeAdjAdvCount(String string){
        
        try{
        
            string = TextFilePreProcess.removeCarets(string);
            String[] stringArray = string.split("\\r?\\n");
        
            groupedAnswers = groupAnswers(stringArray);
        
            //Get sentimentScore for each answer
            write("Teacher Strength");
            int answerIndex = 0;
            List<Document> currentGroup = groupedAnswers.getTeachStrength();
            for(; answerIndex < groupedAnswers.getTeachStrength().size(); answerIndex++){ 
                int negativeCount = 0;
                negativeCount += GetSentimentAdjAdvNegativeCount(currentGroup,answerIndex, 0);
                AssignToPosNegAdjAdvNegativeCount(negativeCount,currentGroup.get(answerIndex));
                write("Score:" + negativeCount);
            }
            //TeachWeakness
            write("Teacher Weakness");
            answerIndex = 0;
            currentGroup = groupedAnswers.getTeachWeak();
            for(; answerIndex < groupedAnswers.getTeachWeak().size(); answerIndex++){ 
                int negativeCount = 0;
                negativeCount += GetSentimentAdjAdvNegativeCount(currentGroup,answerIndex, 0);
                AssignToPosNegAdjAdvNegativeCount(negativeCount,currentGroup.get(answerIndex));
                write("Score:" + negativeCount);
            }
            
            //Comments
            write("Comment");
            answerIndex = 0;
            currentGroup = groupedAnswers.getComments();
            for(; answerIndex < groupedAnswers.getComments().size(); answerIndex++){ 
                int negativeCount = 0;
                negativeCount += GetSentimentAdjAdvNegativeCount(currentGroup,answerIndex, 0);
                AssignToPosNegAdjAdvNegativeCount(negativeCount,currentGroup.get(answerIndex));
                write("Score:" + negativeCount);
            }
            
            write("Done");
            
        }
        
        catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
    private static int GetSentimentAdjAdvNegativeCount(List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
//        write("Inside GetSentiment");
        int negativeCount = 0;
        try{
        
            //Get sense for each word
            List<Sentence> answerSentences = compareToGroupedAnswers.get(answerIndex).sentences();
            int answerSentencesSize = answerSentences.size();
        
            IDictionary dictionary = WordNetAccess.loadDic();
            dictionary.open();
            for(int sentenceCtr = 0; sentenceCtr < answerSentencesSize; sentenceCtr++){
            
                Sentence currentSentence = answerSentences.get(sentenceCtr);
//                write(LanguageProcess.getPOS(currentSentence));
                int lemmaSize = currentSentence.lemmas().size();
            
                for(int lemmaCtr = 0; lemmaCtr < lemmaSize; lemmaCtr++){
                
                    String currentPOSTag = currentSentence.posTag(lemmaCtr);
//                    try{
//                        Optional<Integer> dependency = currentSentence.governor(lemmaCtr);
//                        if(dependency.isPresent()){
//                            dependency.toString();
//                        }
//                    }
//                    catch(Exception exc){
//                        exc.printStackTrace();
//                    }
//                    if(currentPOSTag.matches("JJ(R|S)?|(NN)S?|VB(D|G|N|P|Z)?|RB(S|R)?")){
                    if(currentPOSTag.matches("JJ(R|S)?|VB(D|G|N|P|Z)?|RB(S|R)?")){
//                    if(currentPOSTag.matches("JJ(R|S)?|RB(S|R)?")){
//                    if(currentPOSTag.matches("JJ(R|S)?")){
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
                                    double objective = 1 - results.getFloat("NegScore") - results.getFloat("PosScore");
                                    if(results.getFloat("NegScore") > results.getFloat("PosScore")){
                                        negativeCount++;
                                    }
                                    
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
                                    double objective = 1 - results.getFloat("NegScore") - results.getFloat("PosScore");
                                    if(results.getFloat("NegScore") > results.getFloat("PosScore")){
                                        negativeCount++;
                                    }
                                }
                            }
                        }
                        
                    }   
                
                }
            }
        }
        catch(SQLException sqlError){
            sqlError.printStackTrace();
        }
        catch(Exception exc){
            write(exc.getMessage());
//            write(sqlError.getMessage());
            exc.printStackTrace();
        }
        
//        write("Exiting GetSentiment");
        
        return negativeCount;
        
    }
    
    private static void AssignToPosNegAdjAdvNegativeCount(int negativeCount,  Document answer){
//        if(negativeCount == 0){
//            neutralSentences.add(answer);
//        }
//        else{
        if(negativeCount%2 != 0){
            negativeSentences.add(answer);
        }
        else {
            positiveSentences.add(answer);
        }
//        }
        
    }
    
    public static void summarizeFirstAlgo(String string){
        
        try{
        
            string = TextFilePreProcess.removeCarets(string);
            String[] stringArray = string.split("\\r?\\n");
        
            groupedAnswers = groupAnswers(stringArray);
        
            //Get sentimentScore for each answer
            write("Teacher Strength");
            int answerIndex = 0;
            List<Document> currentGroup = groupedAnswers.getTeachStrength();
            for(; answerIndex < groupedAnswers.getTeachStrength().size(); answerIndex++){ 
               double sentimentScore = GetSentimentFirstAlgo(currentGroup,answerIndex, 0);
               AssignToPosNegFirstAlgo(sentimentScore,currentGroup.get(answerIndex));
               write("Score:" + sentimentScore);
            }
            //TeachWeakness
            write("Teacher Weakness");
            answerIndex = 0;
            currentGroup = groupedAnswers.getTeachWeak();
            for(; answerIndex < groupedAnswers.getTeachWeak().size(); answerIndex++){ 
                double sentimentScore = GetSentimentFirstAlgo(currentGroup,answerIndex, 0);
                AssignToPosNegFirstAlgo(sentimentScore,currentGroup.get(answerIndex));
                write("Score:" + sentimentScore);
            }
            
            //Comments
            write("Comment");
            answerIndex = 0;
            currentGroup = groupedAnswers.getComments();
            for(; answerIndex < groupedAnswers.getComments().size(); answerIndex++){ 
                double sentimentScore = GetSentimentFirstAlgo(currentGroup,answerIndex, 0);
                AssignToPosNegFirstAlgo(sentimentScore,currentGroup.get(answerIndex));
                write("Score:" + sentimentScore);
            }
            
            write("Done");
            
        }
        
        catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
    private static void AssignToPosNegFirstAlgo(double score, Document answer){
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
    
    private static double GetSentimentFirstAlgo(List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
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
    
    private static int Disambiguate(IIndexWord indexWord, List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
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
    
    private static List<StringAndTag> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
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
                ++i;
                while(i < answers.length && !TextFilePreProcess.ifQuestion(answers[i])){
//                    write(answers[i]);
                    answerGroups.asssignToGroup(cntQuestion, new Document(answers[i]));
                    
                    i++;
                }
                cntQuestion++;
            }
        }
        
        return answerGroups;
        
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
    
    private static void printAnswerGroup(List<Document> answers){
        for(Document answer : answers){
            write(answer.text());
        }
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
