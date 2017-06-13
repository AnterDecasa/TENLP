/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import TextProcess.SummarizeText;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.ListModel;

/**
 *
 * @author user
 */
public class OpenMultipleFiles extends javax.swing.JFrame {

    /**
     * Creates new form OpenMultipleFiles
     */
    public OpenMultipleFiles() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList<>();
        selectFiles = new javax.swing.JButton();
        getSentimentFromFiles = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(fileList);

        selectFiles.setText("Select Files");
        selectFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFilesActionPerformed(evt);
            }
        });

        getSentimentFromFiles.setText("Get Sentiment From Files");
        getSentimentFromFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getSentimentFromFilesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectFiles)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(getSentimentFromFiles)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectFiles)
                .addGap(22, 22, 22)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(getSentimentFromFiles)
                .addContainerGap(55, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
//    String folderpath = "C:\\Users\\user\\Desktop\\IMDB\\aclImdb\\train\\pos\\";
//    String folderpath = "C:\\Users\\user\\Desktop\\IMDB\\aclImdb\\test\\pos\\";
    String folderpath = "C:\\Users\\user\\Desktop\\Working IMDB Files\\train\\pos\\";
//    String folderpath = "C:\\Users\\user\\Desktop\\Working IMDB Files\\test\\neg\\";
//    String folderpath = "C:\\Users\\user\\Google Drive\\Current Thesis\\Resources\\Text Files\\text files for each teacher(for each subject)\\";
    
    private void selectFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFilesActionPerformed
        JFileChooser fileChooser;
        fileChooser = new JFileChooser(new File(folderpath));
        fileChooser.setMultiSelectionEnabled(true);
        int returnVal = fileChooser.showOpenDialog(fileChooser.getParent());
        
        final DefaultListModel model = new DefaultListModel();
        
        if(returnVal == JFileChooser.APPROVE_OPTION){
            File[] files = fileChooser.getSelectedFiles();
            for(int i = 0; i < files.length; i++){
                model.addElement(files[i].getName());
            }
            fileList.setModel(model);
        }
    }//GEN-LAST:event_selectFilesActionPerformed

    private void getSentimentFromFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getSentimentFromFilesActionPerformed
        ListModel model = fileList.getModel();
        
        
        String text = "";
        BufferedReader reader;
        
        try{
            PrintWriter pw = new PrintWriter(new File("documentScores.csv"));
            StringBuilder sb = new StringBuilder();
            sb.append("FileName,Positive,Negative\n");
            
            for(int i = 0 ; i < model.getSize(); i++){
                Object o = model.getElementAt(i);

                System.out.print("Opening: " + o.toString() + "." + "\n");
                reader = new BufferedReader(new FileReader(folderpath + o.toString()));
                System.out.println(o.toString());

                String line = "";

                line = reader.readLine();
                while (line != null) {
                    text += line + "\n";
                    line = reader.readLine();
                }
                SummarizeText.getSentimentofWholeDocumentWithNegationWithDisambiguationForCleanTextRecordInAFile(folderpath + o.toString(), text, pw, sb);
                text = "";
                System.out.println("Done");
                                
            }
            pw.write(sb.toString());
            pw.close();
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
    }//GEN-LAST:event_getSentimentFromFilesActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OpenMultipleFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OpenMultipleFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OpenMultipleFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OpenMultipleFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OpenMultipleFiles().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> fileList;
    private javax.swing.JButton getSentimentFromFiles;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton selectFiles;
    // End of variables declaration//GEN-END:variables
}