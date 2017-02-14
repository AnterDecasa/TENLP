/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestClass;

import ContainerClasses.AnswerGroups;
import ContainerClasses.LemmaSentenceWithPOStag;
import ContainerClasses.StringAndTag;
import TextProcess.LanguageProcess;
import TextProcess.TextFilePreProcess;
import TextProcess.WordNetAccess;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author csc1701
 */
public class TestSummarizeText {
    
    static AnswerGroups groupedAnswers;
    static String[] compareWords;
    static String string ="As a teacher, what are his/her strengths?\n" +
"> her knowledge\n" +
"> Patience and mastery over subject matter. Materials are up to date. Very open and easy to communicate with\n" +
"> Responsible\n" +
"> She helps us!\n" +
"> She is a really determined teacher who encourages her students to dwell more on excellence in studies and the field of research.\n" +
"> she knows what she is teaching and she can expound it to simpler terms\n" +
"> she's ok\n" +
"> teacher is nice and provides good feedback to studentsteacher is knowledgable about courseteacher is approachable and enthusiastic\n" +
"> The way she handles her class.\n" +
"As a teacher, what areas should s/he improve?\n" +
"> I think nothing.\n" +
"> not much\n" +
"> Nothing really.\n" +
"What do you like best about this course?\n" +
"> encourages us to find solutions to problems and learn new things\n" +
"> i accidentally learn a lot of stuffs\n" +
"> Informative\n" +
"> Interesting and makes me feel like Im doing something. Training my professionalism\n" +
"> It helps us with our thesis\n" +
"> it is a gateway to many research and ideas\n" +
"> The course outline.\n" +
"> The fun in learning something new.\n" +
"> the teacher is good and the course leads us to learning new things we can apply\n" +
"What do you like least about this course?\n" +
"> a lot of reading\n" +
"> Finding a topic is hard / volatile\n" +
"> It's THE thesis. That's scary\n" +
"> Sometimes this course put pressure to me.\n" +
"> tiring\n" +
"Comment\n" +
"> me\n" +
"> Nothing 2\n" +
"IT 184\n" +
"As a teacher, what are his/her strengths?\n" +
"> Available usually for consultation\n" +
"> Discussion and giving us motivation\n" +
"> finds time in monitoring group progresses\n" +
"> has knowledge on the subject\n" +
"> her dedication to the students\n" +
"> her organize and systematic plan in teaching us\n" +
"> Really checks on students\n" +
"> She is very experienced.\n" +
"> She is very nice and takes time to explain and clarify topics. She caters to our questions.\n" +
"> she knows what she's doing and the topics that she discusses\n" +
"> She speaks well and she entertains her students fairly.\n" +
"> She supervises the class very well\n" +
"As a teacher, what areas should s/he improve?\n" +
">> n/a\n" +
"> She is always busy and sometimes unavailable.\n" +
"> time management because she's often missing when we need something from her\n" +
"What do you like best about this course?\n" +
"> all\n" +
"> Developed, presented and defended an IT project.\n" +
"> everything\n" +
"> eveything\n" +
"> Has way less documentation than soft eng\n" +
"> I can learn a lot.\n" +
"> It was very challenging\n" +
"> Researching and developing\n" +
"What do you like least about this course?\n" +
">> difficult and stressful\n" +
"> It was very tiring. Struggle.\n" +
"> n/a 3\n" +
"IT 184\n" +
"> Researching and developing\n" +
"> the capstone nomination. unfair.\n" +
"> The pressure.\n" +
"Comment\n" +
">> Change the nomination system that will fairly cater and consider the whole set of teams. Basically, just like an algorithm that works with accuracy and correctness. :)\n" +
"> Good sem!\n" +
"> Researching is tiresome but challenging.\n" +
"> Thank you Miss :)\n" +
"> Thank you ma'am for being considerate and for tolerating our shortcomings.. See you :) 4";
    
    public static void summarize(String string){
        
        string = TextFilePreProcess.removeCarets(string);
        String[] stringArray = string.split("\\r?\\n");
        
        groupedAnswers = groupAnswers(stringArray);
        
            //Get sentimentScore for each answer
            int answerIndex = 0;
            for(Document answer : groupedAnswers.getTeachStrength()){ 
                double sentimentScore = GetSentiment(groupedAnswers.getTeachStrength(),answerIndex, 0);
                answerIndex++;
                //added
                System.out.println(sentimentScore);
            }
            //TeachWeakness
            //Comments
    }
    
     public static double GetSentiment(List<Document> compareToGroupedAnswers, int answerIndex, int questionIndex){
        
        double sentimentScore = 0;
        
        //Get sense for each word
        List<Sentence> answerSentences = compareToGroupedAnswers.get(answerIndex).sentences();
        int answerSentencesSize = answerSentences.size();
        WordNetAccess.loadDic();
        for(int sentenceCtr = 0; sentenceCtr < answerSentencesSize; sentenceCtr++){
            
            Sentence currentSentence = answerSentences.get(sentenceCtr);
            int lemmaSize = currentSentence.lemmas().size();
            
            for(int lemmaCtr = 0; lemmaCtr < lemmaSize; lemmaCtr++){
                
                String currentPOSTag = currentSentence.posTag(lemmaCtr);
                if(currentPOSTag.matches("JJ|NN|VB|RB")){
                    //added
                    System.out.println(currentPOSTag);
                    System.out.println(currentSentence.lemma(lemmaCtr));
                    System.out.println(LanguageProcess.GetPOSTag(currentPOSTag));
                    IIndexWord indexWord = WordNetAccess.dict.getIndexWord(currentSentence.lemma(lemmaCtr), LanguageProcess.GetPOSTag(currentPOSTag));
                    List<IWordID> wordIDs = indexWord.getWordIDs();
                    if(wordIDs.size() > 1){
                        //Disambiguate
                        int indexOfWordToBeUsed = Disambiguate(indexWord, compareToGroupedAnswers, answerIndex, sentenceCtr, lemmaCtr, questionIndex);
                        //added
                        System.out.println(indexOfWordToBeUsed);
                    }
                    
                }
                
            }
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
         
        return indexOfWordToBeUsed;
        
    }
    
    public static void Lesk(String word, String[] compareTo){ 
        
    }
    
    public static List<StringAndTag> GetCompareToWords(List<Document> compareToGroupedAnswers, int answerIndex, int sentenceIndex, int lemmaIndex, int questionIndex){
        
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
    
    private static void write(Tree tree){
        
        System.out.println(tree);
        
    }
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
    public static void main (String args[]){
        summarize(string);
    }
    
}
