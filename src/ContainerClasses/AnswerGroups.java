/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerClasses;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AnnTherese
 */
public class AnswerGroups {
    
    private List<SentenceAndTag> teachStrength = new ArrayList<>();
    private List<SentenceAndTag> teachWeak = new ArrayList<>();
    private List<SentenceAndTag> subjLike = new ArrayList<>();
    private List<SentenceAndTag> subjHate = new ArrayList<>();
    private List<SentenceAndTag> comments = new ArrayList<>();
    
    public List<SentenceAndTag> getTeachStrength(){
        
        return this.teachStrength;
        
    }
    
    public List<SentenceAndTag> getTeachWeak(){
        
        return this.teachWeak;
        
    }
    
    public List<SentenceAndTag> getSubjLike(){
        
        return this.subjLike;
        
    }
    
    public List<SentenceAndTag> getSubjHate(){
        
        return this.subjHate;
        
    }
    
    public List<SentenceAndTag> getComments(){
        
        return this.comments;
        
    }
    
    public void setList(int questionIndxNum, List<SentenceAndTag> list){
        
        switch(questionIndxNum){
            case 1:
                teachStrength = list;
                break;
            case 2:
                teachWeak = list;
                break;
            case 3:
                subjLike = list;
                break;
            case 4:
                subjHate = list;
                break;
            case 5:
                comments = list;
                break;
            }
        
    }
    
    public void setList(int questionIndxNum, SentenceAndTag sentandTag){
        
        switch(questionIndxNum){
            case 1:
                teachStrength.add(sentandTag);
                break;
            case 2:
                teachWeak.add(sentandTag);
                break;
            case 3:
                subjLike.add(sentandTag);
                break;
            case 4:
                subjHate.add(sentandTag);
                break;
            case 5:
                comments.add(sentandTag);
                break;
            }
        
    }
    
}
