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
            
            IIndexWord idxWord = dict.getIndexWord("approachable", POS.ADJECTIVE);
            IWordID wordID = idxWord.getWordIDs().get(0);
            IWord word = dict.getWord(wordID);
            ISynset synset = word.getSynset();
            
            for(IWord w : synset.getWords()){
                ISynset synset2 = w.getSynset();
                for(IWord w2 : synset2.getWords()){
                    write(w2.getLemma());
                }
            }
            for(IWordID w: word.getRelatedWords()){
                word = dict.getWord(w);
                write(word.getLemma());
            }
        }
        catch(MalformedURLException eURL){
            eURL.getStackTrace();
        }
        catch(IOException eIO){
            eIO.getStackTrace();
        }
        
        return dict;
        
    }
    
    private static void write(String string){
        System.out.println(string);
    }
    
}
