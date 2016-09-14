package TextProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TextFileProcess {
	
	static private String subject = "";
	
	public static String getTeacherName(BufferedReader text){
		
		String name = "";
		String line = "";
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
        
        public static String removeRedundantSubject(String string){
            
            String retVal = "";
            String[] array = string.split("\r?\n|\n");
            String currSubject = array[1];
            
            retVal += getTeacherName(string) + "\n";
            retVal += currSubject.trim() + "\n";
            for(int i = 2; i < array.length; i++){
                if(!currSubject.trim().equalsIgnoreCase(array[i].trim())){
                    if(checkIfNewSubject(array[i])){
                        currSubject = array[i-1].trim();
                    }
                    retVal += array[i].trim() + "\n";
                }
                
            }
            
            return retVal;
            
        }
        
        public static boolean checkIfNewSubject(String string){
            
            return string.equalsIgnoreCase("As a teacher, what are his/her strengths?");
            
        }
	
	public static String checkIfNewSubject(BufferedReader text){
		
		String newSubject = "";
		String line = "";
		
		try{
			line = text.readLine();
			if(!line.equalsIgnoreCase("As a teacher, what are his/her strengths?")){
				subject = line.trim();
				text.mark(20);
			}
			else{
				newSubject = subject;
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
            return string.matches("(1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20)");
        }
        
        private static boolean isDate(String string){
            return string.matches("(.*)(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday)(.*)");
        }
        
        public static String getImportantText(File file){
            
            String retVal = "";
            BufferedReader evaluationText;
            String subject = "";
            
            try{
                evaluationText = new BufferedReader(new FileReader(file.getAbsolutePath()));
                String line = "";
                    
                //Get Teacher name
                retVal += getTeacherName(evaluationText) + "\n";
                    
                //Remove other info
                while((subject = checkIfNewSubject(evaluationText)).equalsIgnoreCase("")){}
                retVal += subject + "\n";
                    
                evaluationText.reset();
                    
                line = evaluationText.readLine();
                while(line != null){
                    retVal += line + "\n";
                    line = evaluationText.readLine();
                }
                retVal = removeDate(retVal);
                retVal = removePageNumber(retVal);
                retVal = removeRedundantSubject(retVal);
            }
            catch(IOException e){
                write("No file read");
            }
            
            return retVal;
            
        }
	
	private static void write(String string){
		
		System.out.println(string);
		
	}
	
}
