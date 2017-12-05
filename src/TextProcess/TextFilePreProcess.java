package TextProcess;

import edu.stanford.nlp.trees.Tree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ContainerClasses.LemmaSentenceWithPOStag;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TextFilePreProcess {

    static private String currSubject = "";
    static private String[] subjects;
    static private String teacherName = "";
    
    static public List<String> questions = new ArrayList<String>();
    
    private static String getTeacherName(BufferedReader text) {

        String name = "";
        String line;
        try {
            line = text.readLine();

//            while (!line.equalsIgnoreCase("Faculty Evaluation Report Tool")) {
//                line = text.readLine();
//            }
            for (int i = 1; i <= 4; i++) {
                line = text.readLine();
            }
            name = line.trim();
            teacherName = name;
            write("Teacher name identified.");
        } catch (IOException e) {
            write("No text read");
        }

        return teacherName;

    }
    
    public static void outputSentencesCSV(String string) throws FileNotFoundException{
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
        
        PrintWriter pw = new PrintWriter(new File("sentence.csv"));
        StringBuilder sb = new StringBuilder();
        
        Document docu = new Document(noQuestions);
        List <Sentence> sentences = docu.sentences();
        
        for(int i = 0; i < sentences.size(); i++){
            sb.append(sentences.get(i).text());
            if(i < sentences.size()-1){
                sb.append('\n');
            }
        }
        
//        sb.append("id");
//        sb.append(',');
//        sb.append("Name");
//        sb.append('\n');

        pw.write(sb.toString());
        pw.close();
        System.out.println("output sentences done!");
    }
    
    public static String cleanTextFromEvaluation(String text){
        
        String retVal = "";
                
        retVal = TextFilePreProcess.removeQuestions(text);
        retVal = TextFilePreProcess.removeCarets(retVal);
        retVal = TextFilePreProcess.convertAllCAPSTolowerCase(retVal);
        retVal = TextFilePreProcess.putPeriodsForNoPeriod(retVal);
        
        return retVal;
        
    }
    
    public static void outputSentencesText(String string) throws FileNotFoundException{
        
        String noQuestions = TextFilePreProcess.removeQuestions(string);
        noQuestions = TextFilePreProcess.removeCarets(noQuestions);
        noQuestions = TextFilePreProcess.convertAllCAPSTolowerCase(noQuestions);
        noQuestions = TextFilePreProcess.putPeriodsForNoPeriod(noQuestions);
        
        PrintWriter pw = new PrintWriter(new File("cleanedWithBreaks.txt"));
        StringBuilder sb = new StringBuilder();
        
        Document docu = new Document(noQuestions);
        List <Sentence> sentences = docu.sentences();
        
        for(int i = 0; i < sentences.size(); i++){
            sb.append(sentences.get(i).text());
            if(i < sentences.size()-1){
                sb.append('\n');
            }
        }
        
//        sb.append("id");
//        sb.append(',');
//        sb.append("Name");
//        sb.append('\n');

        pw.write(sb.toString());
        pw.close();
        System.out.println("output sentences done!");
    }
    
    public static String convertAllCAPSTolowerCase(String string){
        write("removing all caps");
        String retVal = "";
        String[] stringArray = string.split("\r?\n|\n");
        
        for(String stringElem : stringArray){
            
            String[] words = stringElem.split(" ");
            for(int i = 0; i < words.length; i++){
                if(words[i].matches("([A-Z]){2,}(\\.*|!*|\\?*)")){
                    retVal += words[i].toLowerCase();
                }
                else{
                    retVal += words[i];
                }
                if(i < words.length-1){
                    retVal += " ";
                }
            }
            retVal += "\n";
        }
        
        
        return retVal;
        
    }
    
    public static String getTeacherName() {

        return teacherName;

    }

    //Removed for errors
    private static String removeRedundantSubjectLabel(String string) {

        String retVal = "";
        String[] array = string.split("\r?\n|\n");
        String currSubject = array[0];

//        retVal += currSubject.trim() + "\n";

        write("Removing redundant subject labels");

        for (int i = 0; i < array.length; i++) {
//            if (i < array.length - 1 && newSubject(array[i + 1])) {
//                currSubject = array[i].trim();
//                retVal += currSubject + "\n";
////                write(currSubject + " is new subject");
//            }
//            else if (!currSubject.trim().equalsIgnoreCase(array[i].trim())) {
//                retVal += array[i].trim() + "\n";
//            }
            if(i < array.length - 1 && newSubject(array[i + 1])){
                write(array[i]);
                currSubject = array[i].trim(); //just set again maybe it is a different section
                retVal += array[i].trim() + "\n";
            }
            else if(i < array.length - 1 && !newSubject(array[i + 1]) && !array[i].equalsIgnoreCase(currSubject) ){
                retVal += array[i].trim() + "\n";
            }
//            retVal += "\n";
        }

        return retVal;

    }

    public static String[] getSubjects() {
        return subjects;
    }

    private static String[] setSubjects(String string) {

        ArrayList<String> retVal = new ArrayList<>();

        String[] stringArray = string.split("\\r?\\n|\\n|\\n?\\f");

        for (int i = 0; i < stringArray.length; i++) {
            if (i < stringArray.length - 1 && newSubject(stringArray[i + 1])) {
                
                int numberOfOccurence = itemExistsInList(stringArray[i].trim(), retVal) + 1;
                if (numberOfOccurence == 0) {
                    retVal.add(stringArray[i].trim() + "_1");
                }
                else{
                    retVal.add(stringArray[i].trim() + "_" + numberOfOccurence);
                }

            }
        }

        return retVal.toArray(new String[0]);

    }

    public static int itemExistsInList(String string, List<String> list) {

        int retVal = 0;
        if (list != null) {
            for (String item : list) {
                String itemRemoveUnderScore = item.split("_")[0];
                write(item +": "+ itemRemoveUnderScore);
                if (string.equalsIgnoreCase(itemRemoveUnderScore)) {
                    retVal++;
                }
            }
        }

        return retVal;

    }

    public static boolean subjectExists(String string) {

        boolean retVal = false;
        if (subjects != null) {
            for (String subject : subjects) {
                if (string.equalsIgnoreCase(subject.split("_")[0])) {
                    retVal = true;
                    break;
                }
            }
        }

        return retVal;

    }

    private static String putTogetherOneCommment(String string) {

        String[] stringArray = string.split("\\r?\\n|\\n|\\n?\\f");
        String retVal = stringArray[0];

        write("Combining separate comments");

        for (int i = 1; i < stringArray.length; i++) {
            if (stringArray[i].charAt(0) != '>') {
                if (!ifQuestion(stringArray[i]) && !subjectExists(stringArray[i])) {
                    retVal += " " + stringArray[i];
                } else {
                    retVal += "\n" + stringArray[i];
                }
            } else {
                retVal += "\n" + stringArray[i];
            }
        }

        return retVal;

    }

    public static boolean ifQuestion(String string) {

        boolean retVal = false;
        if (string.matches("As a teacher, what are his/her strengths\\?")){
            retVal = true;
        }
        if (string.matches("As a teacher, what areas should s/he improve\\?")){
            retVal = true;
        }
        if (string.matches("What do you like best about this course\\?")){
            retVal = true;
        }
        if (string.matches("What do you like least about this course\\?")){
            retVal = true;
        }
        if (string.matches("Comment")){
            retVal = true;
        }

        return retVal;
        
    }

    private static String removeExtraCarriageReturn(String string) {

        String retVal = "";
        String[] array = string.split("\\r?\\n|\\n|\\n?\\f");

        for (String line : array) {
            if (!line.trim().equalsIgnoreCase("")) {
                retVal += line.trim() + "\n";
            }
        }

        return retVal;

    }

    public static boolean newSubject(String string) {

        return string.equalsIgnoreCase("As a teacher, what are his/her strengths?");

    }

    public static String newSubject(BufferedReader text) {

        String newSubject = "";
        String line;

        try {
            line = text.readLine();
            if (!line.equalsIgnoreCase("As a teacher, what are his/her strengths?")) {
                currSubject = line.trim();
                text.mark(20);
            }
            else {
                newSubject = currSubject;
            }
        } catch (IOException e) {
            write("No text read" + "\n");
        }
//        write(newSubject +" New Subject Function");
        return newSubject;

    }

    private static String removeDate(String string) {

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

    private static String removePageNumber(String string) {

        String[] array = string.split("\\r?\\n|\\r");
        String retVal = "";

        write("Removing page numbers");

        for (String line : array) {
            if (!isNumber(line.trim())) {

                retVal += line + "\r\n";
            } else {
                write("page " + line.trim());
            }
        }

        return retVal;

    }

    private static boolean isNumber(String string) {
        return string.matches("\\d+");
    }

    private static boolean isDate(String string) {
        return string.matches("(Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday), (.*)");
    }

    public static String removeNoComment(String string) {

        String retVal = "";
        String[] stringArray = string.split("\\r?\\n|\\r");

        write("Removing no comments");

        for (String lineArray : stringArray) {
                if (hasComment(lineArray)) {
                retVal += lineArray + "\n";
            }
        }

        return retVal;

    }

    public static boolean isEmptyComment(String string) {

        return string.matches(">* *>*-*>*.*");

    }

    public static boolean hasComment(String string) {
        boolean hasComment = true;
        String line = string.trim().replaceFirst(">", "");
        line = line.trim();
        if (line.matches("\\W+\\.*")) {
            hasComment = false;
        }
        if (line.matches("((n|N)(o|O)(n|N)(e|E))\\.*")) {
            hasComment = false;
        }
        if (line.matches("((n|N)\\/?(a|A))\\.*")) {
            hasComment = false;
        }
        if (line.matches("((n|N)o? ?(c|C)(omment)?)\\.*")) {
            hasComment = false;
        }
        if (line.matches("((n|N)othing)\\.*")){
            hasComment = false;
        }
        if (line.matches("\\d+\\.*")){
            hasComment = false;
        }
        if ("".equals(line)){
            hasComment = false;
        }
        
//        if (!hasComment){
//            write(line);
//        }
        return hasComment;

    }
    
    public static String replaceAllPageBreaks(String string){
        return string.replaceAll("<br />", "\\n");
    }
    
    public static String getImportantText(File file) {

        String retVal = "";
        BufferedReader evaluationText;
        String newSubject = "";

        write("Getting evaluation");

        try {
            evaluationText = new BufferedReader(new FileReader(file.getAbsolutePath()));
            String line;

            //Set Teacher name
//            retVal = getTeacherName(evaluationText) + "\n";
            getTeacherName(evaluationText);
            //Remove other info
            while ((newSubject = newSubject(evaluationText)).equalsIgnoreCase("")) {
            }
            retVal += newSubject + "\n";

            evaluationText.reset();

            line = evaluationText.readLine();
            
            while (line != null) {
                retVal += line + "\n";
                line = evaluationText.readLine();
            }
            retVal = removeDate(retVal);
            retVal = removePageNumber(retVal);
            retVal = removeExtraCarriageReturn(retVal);
            retVal = removeRedundantSubjectLabel(retVal);
            subjects = setSubjects(retVal);
            retVal = putTogetherOneCommment(retVal);
            retVal = removeNoComment(retVal);
            questions.add("As a teacher, what are his/her strengths?");
            questions.add("As a teacher, what areas should s/he improve?");
            questions.add("What do you like best about this course?");
            questions.add("What do you like least about this course?");
            questions.add("Comment");
        } catch (IOException e) {
            write("No file read");
        }

        return retVal;

    }

    public static String getSubjectEvaluation(String string, String subject) {

        String retVal = "";
        String[] stringArray = string.split("\\r?\\n");

        write("Getting evaluation for " + subject);
        
        String[] subjectNameSplit = subject.split("_");
        String subjectWithoutSectionNumber = subjectNameSplit[0];
        int sectionNumber =  Integer.parseInt(subjectNameSplit[1]);
        write("Section Number: " + sectionNumber);
        int sectionFinderCount = 0;
        
        for (int i = 0; i < stringArray.length;) {
            write("Line count: " + i);
            if (stringArray[i].equalsIgnoreCase(subjectWithoutSectionNumber)){
                sectionFinderCount++;
            }
            if(stringArray[i].equalsIgnoreCase(subjectWithoutSectionNumber) && sectionFinderCount == sectionNumber){
                write(stringArray[i]);
                write(subjectWithoutSectionNumber + " " + sectionFinderCount);
                i++;
                while (i < stringArray.length && (i < stringArray.length - 1 && !newSubject(stringArray[i + 1]))) {
                    retVal += stringArray[i] + "\n";
                    i++;
                }
                if(i == stringArray.length - 1){
                    retVal += stringArray[i];
                }
                
                sectionFinderCount = 0;
                
            } 
            else{
                i++;
            }
            
        }
        write("loop done");
        write(retVal);
        return retVal;

    }

    public static String removeQuestions(String string) {

        String retVal = "";
        String[] stringArray = string.split("\\r?\\n");

        write("Removing questions");

        for (String line : stringArray) {
            if (!ifQuestion(line.trim().replaceFirst(">",""))) {
                retVal += line + "\n";
            }
        }

        return retVal;

    }

    public static String removeCarets(String string) {

        String retVal = "";
        String[] stringArray = string.split("\\r?\\n");

        write("Removing carets");
        for (String line : stringArray) {
            retVal += line.trim().replaceFirst(">", "").trim() + "\n";

        }

        return retVal;

    }
    
    public static String correctPeriodsPutSpaceAfter(String string){
        return string.replaceAll(".*\\.\\w", ". ");
    }
    
    public static List<String> NERtagging(String string) {

        String[] stringArray = string.split("\\r?\\n");
        List<String> retVal = new ArrayList<String>();

        write("NER tagging");

        for (String line : stringArray) {
            if (ifQuestion(line)) {
                write(line);
            } else {
                List<String> NERLines = LanguageProcess.getNER(line);
                for (String NERLine : NERLines) {
                    retVal.add(NERLine);
                    write("NER tag of '" + line + "'" + NERLine);
                }
            }
        }

        write("NER tagging done");

        return retVal;

    }

    public static String putPeriodsForNoPeriod(String string){
        
        String retVal = "";
        String[] stringArray = string.split("\\r?\\n|\\r");
        
        for(String element : stringArray){
            if(!hasPeriod(element.trim())){
                retVal += element + "." + "\n";
            }
            else{
                retVal += element + "\n";
            }
        }
        
        return retVal;
        
    }
    
    private static boolean hasPeriod(String string){
        
        return string.matches(".*(\\.|!|\\?)");
        
    }
    
    private static void write(String string, boolean newLine) {
        if (newLine) {
            System.out.print(string);
        } else {
            System.out.println(string);
        }
    }

    private static void write(String string) {

        System.out.println(string);

    }

    private static void write(Tree tree) {

        System.out.println(tree);

    }
    
//    public static String getSubjectEvaluation(String string, String subject){
//
//        String retVal = "";
//        String[] stringArray = string.split("\\r?\\n");
//
//        write("Getting evaluation for " + subject);
//
//        for(int i = 0; i < stringArray.length;){
//            if(stringArray[i].equalsIgnoreCase(subject)){
//                i++;
//                while(i < stringArray.length && !subjectExists(stringArray[i])){
//                    retVal += stringArray[i] + "\n";
//                    i++;
//                }
//                break;
//            }
//            i++;
//        }
//        return retVal;
//
//    }
    
//    public static String getSubjectEvaluation(String string, String subject){
//
//        String retVal = "";
//        String[] stringArray = string.split("\\r?\\n");
//
//        write("Getting evaluation for " + subject);
//
//        for(int i = 0; i < stringArray.length;){
//            if(stringArray[i].equalsIgnoreCase(subject)){
//                i++;
//                while(i < stringArray.length && !isSubject(stringArray[i])){
//                    retVal += stringArray[i] + "\n";
//                    i++;
//                }
//                break;
//            }
//            i++;
//        }
//        return retVal;
//
//    }    

}
