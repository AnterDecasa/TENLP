/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import edu.stanford.nlp.trees.Tree;
import java.util.Iterator;

/**
 *
 * @author AnnTherese
 */
public class SummarizeText {
    
    public static void traverseTree(Tree tree){
        
        Iterator it;
        Tree temp = null;
        String POStag = null;
        
        for(it = tree.iterator(); it.hasNext();){
            POStag = temp.value();
            temp = (Tree) it.next();
            
            if(temp.isLeaf()){
                if(POStag.matches("NN(S|P|PS)?|RB(R|S)?|JJ(R|S)?|VB(D|G|N|P|Z)?")){
                    LanguageProcess.getWordInfo(temp.value(), POStag);
                }
            }
            
        }
        
    }
    
}
