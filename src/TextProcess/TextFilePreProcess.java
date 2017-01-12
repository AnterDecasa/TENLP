package TextProcess;

<<<<<<< HEAD
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.Polarity;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
=======
import edu.stanford.nlp.trees.Tree;
>>>>>>> refs/remotes/origin/master
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import static junit.framework.Assert.assertTrue;
=======
import ContainerClasses.LemmaSentenceWithPOStag;
>>>>>>> refs/remotes/origin/master

public class TextFilePreProcess {
	
	static private String currSubject = "";
        static private String[] subjects;
        
        static public List<String> questions = new ArrayList<String>();
	
	private static String getTeacherName(BufferedReader text){
            
		String name = "";
		String line;
		try{
			line = text.readLine();
         
			while(!line.equalsIgnoreCase("Faculty Evaluation Report Tool")){
				line = text.readLine();
			}
			for(int i = 1; i <= 3; i++){
				line = text.readLine();
			}
			name = line.trim();
			write("Teacher name identified.");
		}
		catch(IOException e){
			write("No text read");
		}
		
		return name;
		
	}
        
        public static String getTeacherName(String string){
            
            return string.split("\r?\n|\n")[0];
            
        }
        
        private static String removeRedundantSubjectLabel(String string){
            
            String retVal = "";
            String[] array = string.split("\r?\n|\n");
            String currSubject = array[1];
            
            retVal += getTeacherName(string) + "\n";
            retVal += currSubject.trim() + "\n";
            
            write("Removing redundant subject labels");
                    
            for(int i = 2; i < array.length; i++){
                if(!currSubject.trim().equalsIgnoreCase(array[i].trim())){
                    if(TextFilePreProcess.newSubject(array[i])){
                        currSubject = array[i-1].trim();
                    }
                    retVal += array[i].trim() + "\n";
                }
                
            }
            
            return retVal;
            
        }
        
        public static String[] getSubjects(){
            return subjects;
        }
        
        private static String[] getSubjects(String string){
            
            ArrayList<String> retVal = new ArrayList<>();
        
            String[] stringArray = string.split("\\r?\\n|\\n|\\n?\\f");
            
            for(int i = 1; i < stringArray.length; i++){
                if(i < stringArray.length-1 && newSubject(stringArray[i+1])){
                    retVal.add(stringArray[i].trim());
                }
            }
            
            return retVal.toArray(new String[0]);
        
        }
        
        public static boolean isSubject(String string){
            
            boolean retVal = false;
            
            for(String subject : subjects){
                if(string.equalsIgnoreCase(subject)){
                    retVal = true;
                    break;
                }
            }
            
            return retVal;
            
        }
        
        private static String putTogetherOneCommment(String string){
            
            String[] stringArray = string.split("\\r?\\n|\\n|\\n?\\f");
            String retVal = stringArray[0] ;
            
            write("Combining separate comments");
            
            for(int i = 1; i < stringArray.length; i++){
                if (stringArray[i].charAt(0) != '>'){
                    if(!ifQuestion(stringArray[i]) && !isSubject(stringArray[i])){
                        retVal +=  " " + stringArray[i];
                    }
                    else{
                        retVal +=  "\n" + stringArray[i];
                    }
                }
                else{
                    retVal +=  "\n" + stringArray[i];
                }
            }
            
            return retVal;
                    
        }
        
        public static boolean ifQuestion(String string){
            
            return string.matches("(.*\\?)|Comment");
        
        }
        
        private static String removeExtraCarriageReturn(String string){
            
            String retVal = "";
            String[] array = string.split("\\r?\\n|\\n|\\n?\\f");
            
            for (String line : array) {
                if (!line.trim().equalsIgnoreCase("")) {
                    retVal += line.trim() + "\n";
                }
            }
            
            return retVal;
            
        }
        
        public static boolean newSubject(String string){
            
            return string.equalsIgnoreCase("As a teacher, what are his/her strengths?");
            
        }
	
	public static String newSubject(BufferedReader text){
		
		String newSubject = "";
		String line;
		
		try{
			line = text.readLine();
			if(!line.equalsIgnoreCase("As a teacher, what are his/her strengths?")){
				currSubject = line.trim();
				text.mark(20);
			}
			else{
				newSubject = currSubject;
			}
		}
		catch(IOException e){
			write("No text read" + "\n");
		}
		
		return newSubject;
		
	}
        
        private static String removeDate(String string){
            
            String[] array = string.split("\\r?\\n|\\r");
            String retVal = "";
            
            write("Removing dates");
            
            for (String line : array) {
                if (!isDate(line)) {
                    retVal += line + "\r\n";
                }
            }
            
            return retVal;
            
        }
        
        private static String removePageNumber(String string){
            
            String[] array = string.split("\\r?\\n|\\r");
            String retVal = "";
            
            write("Removing page numbers");
            
            for (String line : array) {
                if (!isNumber(line)) {
                    retVal += line + "\r\n";
                }
            }
            
            return retVal;
            
        }
        
        private static boolean isNumber(String string){
            return string.matches("(1|..|50)");
        }
        
        private static boolean isDate(String string){
            return string.matches("(.*)(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)(.*)");
        }
        
        public static String removeNoComment(String string){
            
            String retVal = "";
            String[] stringArray = string.split("\\r?\\n|\\r");
            
            write("Removing no comments");
            
            for(String lineArray : stringArray){
                if(!isNoComment(lineArray) && !isEmptyComment(lineArray)){
                    retVal += lineArray + "\n";
                }
            }
            
            return retVal;
            
        }
        
        public static boolean isEmptyComment(String string){
            
            return string.matches(">*\\ *>*\\-*>*\\.*");
            
        }
        
        public static boolean isNoComment(String string){
            
            return string.matches(">\\ ?\\-?((n|N)(o|O)(n|N)(e|E)|(n|N)\\/?(a|A)|(n|N)o (c|C)omment|(n|N)othing|NC)?\\.*");
            
        }
        
        public static String getImportantText(File file){
            
            String retVal = "";
            BufferedReader evaluationText;
            String newSubject = "";
            
            write("Getting evaluation");
            
            try{
                evaluationText = new BufferedReader(new FileReader(file.getAbsolutePath()));
                String line;
                    
                //Get Teacher name
                retVal += getTeacherName(evaluationText) + "\n";
                    
                //Remove other info
                while((newSubject = newSubject(evaluationText)).equalsIgnoreCase("")){}
                retVal += newSubject + "\n";
                    
                evaluationText.reset();
                    
                line = evaluationText.readLine();
                while(line != null){
                    retVal += line + "\n";
                    line = evaluationText.readLine();
//                    Polarity[] p = annotate(line);
//                    for(int ctr = 0; ctr < line.length(); ctr++){
//                        assertTrue(p[ctr].isUpwards());
//                    	assertTrue(p[ctr].isDownwards());
//                    }
                }
                retVal = removeDate(retVal);
                retVal = removePageNumber(retVal);
                retVal = removeRedundantSubjectLabel(retVal);
                retVal = removeExtraCarriageReturn(retVal);
                subjects = getSubjects(retVal);
                retVal = putTogetherOneCommment(retVal);
                retVal = removeNoComment(retVal);
                String[] array = retVal.split("\\r?\\n");
<<<<<<< HEAD
                LanguageProcess.getWords(array[4].replace('>', ' '));
=======
                questions.add("As a teacher, what are his/her strengths?");
                questions.add("As a teacher, what areas should s/he improve?");
                questions.add("What do you like best about this course?");
                questions.add("What do you like least about this course?");
                questions.add("Comment");
                //LanguageProcess.getWords(array[4].replace('>', ' '));
>>>>>>> refs/remotes/origin/master
            }
            catch(IOException e){
                write("No file read");
            }
            
            return retVal;
            
        }
        
        public static String getSubjectEvaluation(String string, String subject){
            
            String retVal = "";
            String[] stringArray = string.split("\\r?\\n");
            
            write("Getting evaluation for " + subject);
            
            for(int i = 0; i < stringArray.length;){
                if(stringArray[i].equalsIgnoreCase(subject)){
                    i++;
                    while(i < stringArray.length && !isSubject(stringArray[i])){
                        retVal += stringArray[i] + "\n";
                        i++;
                    }
                    break;
                }
                i++;
            }
            return retVal;
            
        }
        
        public static String removeQuestions(String string){
            
            String retVal = "";
            String[] stringArray = string.split("\\r?\\n");
            
            write("Removing questions");
            
            for(String line : stringArray){
                if(!ifQuestion(line)){
                    retVal += line + "\n";
                }
            }
            
            return retVal;
            
        }
        
        public static String removeCarets(String string){
            
            String retVal = "";
            String[] stringArray = string.split("\\r?\\n");
            
            write("Removing carets");
            for(String line : stringArray){
                
                retVal += line.replaceAll("> ", "") + "\n";
                
            }
            
            return retVal;
            
        }
        
        public static List<String> NERtagging(String string){
            
            String[] stringArray = string.split("\\r?\\n");
            List<String> retVal = new ArrayList<String>();
            
            write("NER tagging");
            
            for(String line : stringArray){
                if(ifQuestion(line)){
                    write(line);
                }
                else{
                    List<String> NERLines = LanguageProcess.getNER(line);
                    for(String NERLine : NERLines){
                       retVal.add(NERLine);
                       write("NER tag of '" + line + "'" + NERLine);
                    }
                }
            }
            
            write("NER tagging done");
            
            return retVal; 
            
        }
        
        private static void write(String string, boolean newLine){
            if(newLine){
                System.out.print(string);
            }
            else{
                System.out.println(string);
            }
        }
        
	private static void write(String string){
		
            System.out.println(string);
		
	}
        
        private static void write(Tree tree){
            
            System.out.println(tree);
            
        }
	
}
