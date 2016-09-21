/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TextProcess;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/**
 *
 * @author anter_000
 */
public class LanguageProcess {
    
    public static void getWords(String string){
        
        Document doc = new Document(string);
         for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            // We're only asking for words -- no need to load any models yet
            write("The second word of the sentence '" + sent + "' is " + sent.word(1));
            // When we ask for the lemma, it will load and run the part of speech tagger
            write("The third lemma of the sentence '" + sent + "' is " + sent.lemma(1));
            // When we ask for the parse, it will load and run the parser
            write("The parse of the sentence '" + sent + "' is " + sent.parse());
            // ...
        }
        
    }
    
    private static void write(String string){
        
        System.out.println(string);
        
    }
    
}
