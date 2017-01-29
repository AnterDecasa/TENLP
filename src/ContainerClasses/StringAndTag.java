/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerClasses;

import edu.mit.jwi.item.POS;

/**
 *
 * @author AnnTherese
 */
public class StringAndTag {
    
    public String word;
    public POS tag;
        
    public StringAndTag(String word, POS tag){
        this.word = word;
        this.tag = tag;
    }
    
}
