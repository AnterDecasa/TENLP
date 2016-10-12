/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AnnTherese
 */
public class WordNetAccess {
    
    public static  IRAMDictionary loadDic(){
        
        String WNHome = "C:\\Users\\AnnTherese\\Google Drive\\Current Thesis\\tools";
        String path = WNHome + File.separator + "dict";
        IRAMDictionary dict = null;
        
        try{
            URL url = new URL("file", null, path);
            dict = new RAMDictionary(url, ILoadPolicy.NO_LOAD);
            dict.open();    
        }
        catch(MalformedURLException eURL){
            eURL.getStackTrace();
        }
        catch(IOException eIO){
            eIO.getStackTrace();
        }
        
        return dict;
        
    }
    
    public static List<IWordID> getSenses(String findWord, String POSSense, IRAMDictionary dict){
        
        List<IWordID> wordID = new ArrayList<IWordID>(0);
        POS pos = null;
        
        if(POSSense.matches("JJ(R|S)?")){
            pos = POS.ADJECTIVE;
        }
        if(POSSense.matches("NNS?")){
            pos = POS.NOUN;
        }
        if(POSSense.matches("RB(S|R)?")){
            pos = POS.ADVERB;
        }
        if(POSSense.matches("VB(D|G|N|P|Z)?")){
            pos = POS.VERB;
        }
        
        IIndexWord idxWord = dict.getIndexWord(findWord, pos);
        wordID = idxWord.getWordIDs();
        if(idxWord.getWordIDs().size() > 1){
            //disambiguate
            //IWordID wordID = idxWord.getWordIDs().get(0);
            //IWord word = dict.getWord(wordID);
            //synset = word.getSynset();
        }
        
        /*ISynset synset = word.getSynset();
        write(word.getLemma());
        write(word.getPOS().name());
            
        for(IWord w : synset.getWords()){
            write(w.getLemma());
        }
        for(IWordID w: word.getRelatedWords()){
            word = dict.getWord(w);
            write(word.getLemma());
        }*/
        
        return wordID;
        
    }
    
    private static void write(String string){
        System.out.println(string);
    }
    
}
