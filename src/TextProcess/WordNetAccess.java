/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
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
    
    public static IRAMDictionary dict;
    
    public static IDictionary loadDic(){
        
        IDictionary retDictionary = null;
        
<<<<<<< HEAD
        String WNHome = "C:\\Users\\csc1701\\Google Drive\\Current Thesis\\tools";
=======
        String WNHome = "C:\\Users\\AnnTherese\\Google Drive\\Current Thesis\\tools\\WordNet-3.0";
>>>>>>> refs/remotes/origin/master
        String path = WNHome + File.separator + "dict";
        
        try{
            URL url = new URL("file", null, path);
            retDictionary = new Dictionary(url);
            dict = new RAMDictionary(url, ILoadPolicy.NO_LOAD);   
        }
        catch(MalformedURLException eURL){
            eURL.getStackTrace();
        }
        catch(IOException eIO){
            eIO.getStackTrace();
        }
        
        return retDictionary;
        
    }
    
    public static List<IWordID> getSense(String findWord, String POSSense){
        
        List<IWordID> wordIDs = new ArrayList<IWordID>();
        POS pos = null;
        
        if(POSSense.matches("JJ(R|S)?")){
            pos = POS.ADJECTIVE;
        }
        if(POSSense.matches("(NN)S?")){
            pos = POS.NOUN;
        }
        if(POSSense.matches("RB(S|R)?")){
            pos = POS.ADVERB;
        }
        if(POSSense.matches("VB(D|G|N|P|Z)?")){
            pos = POS.VERB;
        }
        try{
            loadDic();
            dict.open();
            if(findWord != null){
                write("findWord");
            }
            if(pos != null){
                write("pos");
            }
            IIndexWord idxWord = dict.getIndexWord(findWord, pos);
            if(idxWord != null){
                wordIDs = idxWord.getWordIDs();
            
                IWord word = dict.getWord(wordIDs.get(0));
            
                ISynset synset = word.getSynset();
                write(word.getLemma());
                write(word.getPOS().name());
            
                for(IWord w : synset.getWords()){
                    write(w.getLemma());
                }
                for(IWordID w: word.getRelatedWords()){
                    word = dict.getWord(w);
                    write(word.getLemma());
                }
            }
            dict.close();
        }
    
        catch(IOException e){
            e.printStackTrace();
        }
        
        return wordIDs;
        
    }

    private static void write(String string){
        System.out.println(string);
    }
    
}
