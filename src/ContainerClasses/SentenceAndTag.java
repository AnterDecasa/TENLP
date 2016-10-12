/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerClasses;

import edu.stanford.nlp.trees.Tree;

/**
 *
 * @author AnnTherese
 */
public class SentenceAndTag {
    
    private Tree tree;
    
    private String sentence = "";
    
    public SentenceAndTag(String sent, Tree tree){
        
        this.tree = tree;
        this.sentence = sent;
        
    }
    
    public Tree getTree(){
        
        return this.tree;
        
    }
    
    public String getSentence(){
        
        return this.sentence;
        
    }
    
    public void setTree(Tree tree){
        
        this.tree = tree;
        
    }
    
    public void setSentence(String sent){
        
        this.sentence = sent;
        
    }
    
}
