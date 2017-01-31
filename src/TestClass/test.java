/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TestClass;

import ContainerClasses.*;
import TextProcess.*;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author csc1701
 */
public class test {
        static AnswerGroups groupedAnswers;
        static String string ="As a teacher, what are his/her strengths?\n" +
">\n" +
">.\n" +
"> her knowledge\n" +
"> Patience and mastery over subject matter. Materials are up to date. Very open and easy to communicate with\n" +
"> Responsible\n" +
"> She helps us!\n" +
"> She is a really determined teacher who encourages her students to dwell more on excellence in studies and the\n" +
"field of research.\n" +
"> she knows what she is teaching and she can expound it to simpler terms\n" +
"> she's ok\n" +
"> teacher is nice and provides good feedback to studentsteacher is knowledgable about courseteacher is\n" +
"approachable and enthusiastic\n" +
"> The way she handles her class.\n" +
"\n" +
"As a teacher, what areas should s/he improve?\n" +
">\n" +
">.\n" +
"> I think nothing.\n" +
"> none\n" +
"> not much\n" +
"> Nothing\n" +
"> Nothing really.\n" +
"\n" +
"What do you like best about this course?\n" +
">\n" +
">.\n" +
"> encourages us to find solutions to problems and learn new things\n" +
"> i accidentally learn a lot of stuffs\n" +
"> Informative\n" +
"> Interesting and makes me feel like Im doing something. Training my professionalism\n" +
"> It helps us with our thesis\n" +
"> it is a gateway to many research and ideas\n" +
"> The course outline.\n" +
"> The fun in learning something new.\n" +
"> the teacher is good and the course leads us to learning new things we can apply\n" +
"\n" +
"What do you like least about this course?\n" +
">\n" +
">.\n" +
"> a lot of reading\n" +
"> Finding a topic is hard / volatile\n" +
"> It's THE thesis. That's scary\n" +
"> none\n" +
"> Nothing\n" +
"> Sometimes this course put pressure to me.\n" +
"> tiring\n" +
"\n" +
"Comment\n" +
">\n" +
">.\n" +
"> me\n" +
"> none\n" +
"> Nothing\n" +
"";
        
//        public static void test(String string){
//            string = TextFilePreProcess.removeCarets(string);
//            String[] stringArray = string.split("\\r?\\n");
//        
//            groupedAnswers = groupAnswers(stringArray);
//            
//        }
        
        private static AnswerGroups groupAnswers(String[] answers){
        
        AnswerGroups answerGroups = new AnswerGroups();
        
        int cntQuestion = 0;
        
        for(int i = 0; i < answers.length; ){
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
        
        public static void main (String args[]){
            string = TextFilePreProcess.removeCarets(string);
            String[] stringArray = string.split("\\r?\\n");
        
            groupedAnswers = groupAnswers(stringArray);
            
            List<Document> str = groupedAnswers.getTeachStrength();
            List<Document> weak = groupedAnswers.getTeachWeak();
            List<Document> like = groupedAnswers.getSubjLike();
            List<Document> hate = groupedAnswers.getSubjHate();
            List<Document> com = groupedAnswers.getComments();
            
            System.out.println("Strengths");
            for(Document d : str){
                System.out.println(d.sentences());
            }
            
            System.out.println("Weakness");
            for(Document d : weak){
                System.out.println(d.sentences());
            }
            
            System.out.println("What I like");
            for(Document d : like){
                System.out.println(d.sentences());
            }
            
            System.out.println("What I hate");
            for(Document d : hate){
                System.out.println(d.sentences());
            }
            
            System.out.println("Comments");
            for(Document d : com){
                System.out.println(d.sentences());
            }
            
        }
}
