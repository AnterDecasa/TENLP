
package GUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;

import TextProcess.TextFileProcess;

public class MainFrame extends JPanel implements ActionListener{

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String teacherName = "";
	ArrayList<String> subjects = new ArrayList<String>();

	static JFrame loadingTextWindow = new JFrame("");
    
    	protected JTextField textField;
    	protected JTextArea textArea;
    	private final static String NEWLINE = "\n";
    	BufferedReader evaluationText = null;
    
    	private JFileChooser fileChooser;
    
    	public MainFrame(){
    		fileChooser = new JFileChooser(new File("C:\\Users\\anter_000\\Google Drive\\Current Thesis\\Resources\\Text Files"));
        
    		FlowLayout layout = new FlowLayout();
    		layout.setAlignment(FlowLayout.LEADING);
        
    		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        
    		JPanel chooseFilePanel = new JPanel();
    		chooseFilePanel.setLayout(new BoxLayout(chooseFilePanel, BoxLayout.LINE_AXIS));
    		chooseFilePanel.setBorder(BorderFactory.createLineBorder(Color.black));
    		JButton chooseFileButton = new JButton("Choose File");
    		chooseFileButton.addActionListener(new OpenFileButton()); 
    		chooseFilePanel.add(chooseFileButton);
        
    		textArea = new JTextArea(5, 20);
    		textArea.setEditable(false);
    		JScrollPane scrollPane = new JScrollPane(textArea);
        
    		add(chooseFilePanel);
    		add(scrollPane);
    	}
    
    	@Override
    	public void actionPerformed(ActionEvent evt) {
    		String text = textField.getText();
    		textArea.append(text + NEWLINE);
    		textField.selectAll();

    		//Make sure the new text is visible, even if there
    		//was a selection in the text area.
    		//textArea.setCaretPosition(textArea.getDocument().getLength());
    	}
    
    	public static void main(String[] args){
        
    		loadingTextWindow.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
                }
    		});
        
    		loadingTextWindow.add(new MainFrame());
    		loadingTextWindow.setVisible(true);
    		loadingTextWindow.setSize(1800, 1000);
    		loadingTextWindow.setResizable(false);
    
    	}
    
    	public class OpenFileButton implements ActionListener{
    
    		@Override
    		public void actionPerformed(ActionEvent e) { 
            int returnVal = fileChooser.showOpenDialog(fileChooser.getParent());
            String cleanedText = null;
            String subject = "";
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                //This is where a real application would open the file.
                System.out.print("Opening: " + file.getName() + "." + NEWLINE);
                System.out.print("" + file.getAbsolutePath() + "" + NEWLINE);
              
                
                textArea.setText(""); //Empty text field
                try{
                    evaluationText = new BufferedReader(new FileReader(file.getAbsolutePath()));
                    String line = "";
                    
                    //Get Teacher name
                    teacherName = TextFileProcess.getTeacherName(evaluationText);
                    cleanedText = teacherName + "\n";
                    textArea.append(teacherName + "\n");
                    
                    //Remove other info
                    while("" == (subject = TextFileProcess.checkIfNewSubject(evaluationText))){}

                    //Add First subject
                    subjects.add(subject);
                    
                    evaluationText.reset();
                    
                    /*while(!line.equalsIgnoreCase("As a teacher, what are his/her strengths?")){
                    	line = evaluationText.readLine();
                    	System.out.println("yyy\n");
                    }*/
                    /*while("" == (subject = TextFileProcess.checkIfNewSubject(evaluationText))){
                    	line = evaluationText.readLine();
                    	System.out.print("zzz\n");
                    }*/
                    line = evaluationText.readLine();
                    System.out.print(line);
                    while(line != null){
                        textArea.append(line + "\n");
                        line = evaluationText.readLine();
                    }
                    textArea.setCaretPosition(0);
                }
                catch(IOException f){
                    
                }
            } else {
                System.out.print("Open command cancelled by user." + NEWLINE);
            }
            
        }
    
    }
	
}
