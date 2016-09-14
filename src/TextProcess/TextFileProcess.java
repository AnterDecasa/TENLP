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
			e.printStackTrace();
		}
		
		return name;
		
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
			e.printStackTrace();
		}
		
		return newSubject;
		
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
                while("" == (subject = checkIfNewSubject(evaluationText)));
                retVal += subject + "\n";
                    
                evaluationText.reset();
                    
                line = evaluationText.readLine();
                while(line != null){
                    retVal += line + "\n";
                    line = evaluationText.readLine();
                }
            }
            catch(IOException e){
                
            }
            
            return retVal;
            
        }
	
	private static void write(String string){
		
		System.out.println(string);
		
	}
	
}
