package TextProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TextFileProcess {
	
	static private String currSubject = "";
        static private String[] subjects;
	
	public static String getTeacherName(BufferedReader text){
		
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
			write("Teacher name identified.\n");
		}
		catch(IOException e){
			write("No text read" + "\n");
		}
		
		return name;
		
	}
        
        public static String getTeacherName(String string){
            
            return string.split("\r?\n|\n")[0];
            
        }
        
        public static String removeRedundantSubjectLabel(String string){
            
            String retVal = "";
            String[] array = string.split("\r?\n|\n");
            String currSubject = array[1];
            
            retVal += getTeacherName(string) + "\n";
            retVal += currSubject.trim() + "\n";
            for(int i = 2; i < array.length; i++){
                if(!currSubject.trim().equalsIgnoreCase(array[i].trim())){
                    if(TextFileProcess.newSubject(array[i])){
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
        
        public static String putTogetherOneCommment(String string){
            
            String[] stringArray = string.split("\\r?\\n|\\n|\\n?\\f");
            String retVal = stringArray[0] ;
            
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
        
        public static String removeExtraCarriageReturn(String string){
            
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
        
        public static String removeDate(String string){
            
            String[] array = string.split("\\r?\\n|\\r");
            String retVal = "";
            
            for (String line : array) {
                if (!isDate(line)) {
                    retVal += line + "\r\n";
                }
            }
            
            return retVal;
            
        }
        
        public static String removePageNumber(String string){
            
            String[] array = string.split("\\r?\\n|\\r");
            String retVal = "";
            
            for (String line : array) {
                if (!isNumber(line)) {
                    retVal += line + "\r\n";
                }
            }
            
            return retVal;
            
        }
        
        public static boolean isNumber(String string){
            return string.matches("(1|..|50)");
        }
        
        private static boolean isDate(String string){
            return string.matches("(.*)(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)(.*)");
        }
        
        public static String getImportantText(File file){
            
            String retVal = "";
            BufferedReader evaluationText;
            String newSubject = "";
            
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
                }
                retVal = removeDate(retVal);
                retVal = removePageNumber(retVal);
                retVal = removeRedundantSubjectLabel(retVal);
                retVal = removeExtraCarriageReturn(retVal);
                subjects = getSubjects(retVal);
                retVal = putTogetherOneCommment(retVal);
            }
            catch(IOException e){
                write("No file read");
            }
            
            return retVal;
            
        }
	
        private static void writeNoR(String string){
            System.out.print(string);
        }
        
	private static void write(String string){
		
		System.out.println(string);
		
	}
	
}
