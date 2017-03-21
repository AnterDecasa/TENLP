/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContainerClasses;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AnnTherese
 */
public class AnswerGroups {
    
    public static String[] questions = {
        "As a teacher, what are his/her strengths?",
        "As a teacher, what areas should s/he improve?",
        "What do you like best about this course?",
        "What do you like least about this course?",
        "Comment"
    };
    
    private List<Document> teachStrength = new ArrayList<>();
    private List<Document> teachWeak = new ArrayList<>();
    private List<Document> subjLike = new ArrayList<>();
    private List<Document> subjHate = new ArrayList<>();
    private List<Document> comments = new ArrayList<>();
    
    public List<Document> getTeachStrength(){
        
        return this.teachStrength;
        
    }
    
    public List<Document> getTeachWeak(){
        
        return this.teachWeak;
        
    }
    
    public List<Document> getSubjLike(){
        
        return this.subjLike;
        
    }
    
    public List<Document> getSubjHate(){
        
        return this.subjHate;
        
    }
    
    public List<Document> getComments(){
        
        return this.comments;
        
    }
    
    public void setList(int questionIndxNum, List<Document> list){
        
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
    
    public void asssignToGroup(int questionIndxNum, Document sentandTag){
        
        switch(questionIndxNum){
            case 0:
                teachStrength.add(sentandTag);
//                write("" + 0);
                break;
            case 1:
                teachWeak.add(sentandTag);
//                write("" + 1);
                break;
            case 2:
                subjLike.add(sentandTag);
//                write("" + 2);
                break;
            case 3:
                subjHate.add(sentandTag);
//                write("" + 3);
                break;
            case 4:
                comments.add(sentandTag);
//                write("" + 4);
                break;
            }
        
    }
    
    private void write(String string){
        System.out.println(string);
    }
    
}
