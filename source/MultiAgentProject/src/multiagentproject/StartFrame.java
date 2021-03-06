/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StartFrame.java
 *
 * Created on Apr 4, 2010, 5:32:58 PM
 */

package multiagentproject;

import jade.wrapper.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import javax.swing.JOptionPane;

/**
 *
 * @author omid
 */
public class StartFrame extends javax.swing.JFrame {

    /** Creates new form StartFrame */
    public StartFrame() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        WithJadeGuiCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Multi Agent Based Environment Project");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                Window_Closed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Comic Sans MS", 1, 14));
        jLabel1.setForeground(new java.awt.Color(51, 51, 255));
        jLabel1.setText("Multi Agent Based Environment Project");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multiagentproject/Run.png"))); // NOI18N
        jButton1.setText("Run Jade Server And My Project Automatically");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multiagentproject/Jade.jpg"))); // NOI18N

        WithJadeGuiCheckBox.setText("With Jade Gui");
        WithJadeGuiCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WithJadeGuiCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(166, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(156, 156, 156))
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(WithJadeGuiCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(199, 199, 199))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addContainerGap(44, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(WithJadeGuiCheckBox)
                        .addGap(48, 48, 48)))
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if(!WithJadeGuiCheckBox.isSelected())
        {
            Profile profile=new ProfileImpl("localhost", 1099, Profile.PLATFORM_ID);
            //profile.setParameter("gui", "true");
            profile.setParameter(Profile.PLATFORM_ID, "MyMainPlatform");
            AgentContainer container=Runtime.instance().createMainContainer(profile);
            try
            {
                container.start();
                container.createNewAgent("MAGE", MainAgent.class.getName(), null).start();    // MAGE == Main Agent
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(rootPane, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            String[] args = new String[] {"-gui","-agents","MAGE:"+MainAgent.class.getName()};
            jade.Boot.main(args);
        }
        this.hide();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void Window_Closed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_Window_Closed
        System.exit(0);
    }//GEN-LAST:event_Window_Closed

    private void WithJadeGuiCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WithJadeGuiCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_WithJadeGuiCheckBoxActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StartFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox WithJadeGuiCheckBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

}
