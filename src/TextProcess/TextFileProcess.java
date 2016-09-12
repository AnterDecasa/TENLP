package TextProcess;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;

public class TextFileProcess {

	public static ArrayList<String> getSubjects(BufferedReader text){
		
		ArrayList<String> subjects = new ArrayList<String>();
		
		return subjects;
		
	}
	
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
		}
		catch(IOException e){
			write("No text read");
		}
		
		return name;
		
	}
	
	private static void write(String string){
		
		System.out.println(string);
		
	}
	
}
