/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainFrame.java
 *
 * Created on Nov 16, 2009, 12:34:17 AM
 */

package multiagentproject;


import jade.core.AID;
import jade.core.ProfileImpl;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.ElementIterator;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;
import javax.swing.text.html.parser.Element;

/**
 *
 * @author omid
 */
public class MainFrame extends javax.swing.JFrame {

    private MainAgent mag;    // main agent
    public Vector<AgentPoint> coordinates;
    public Group[][] groups;
    public SendingSenario sending;
    public Vector<Event> events;
    private boolean refreshPressed=false;             // for when press refresh button that doesn't log any new contens
    private Hashtable<AID,String> agentGroupName;     // i put aid of each agent with his/her group name in this hashtable
    public boolean autoMode=false;
    private long beginAllAutoTime;
    public Vector<ReportEvent> repev;
    public int repevindex;
    private long allDuration;
    public boolean isSimulation=false;
    private int numberOfSimulations=0;
    private int indexOfSimulations=1;
    public boolean weHaveAgents=false;
    public Vector<CommunicationLine> comLines;
    public Hashtable<AID,Point> agentLocation;
    public Color colorOfLines=Color.PINK;
    public SwingWorker worker;
    //public int indexComLines=0;
    public String myPointsFilePath=null;
    public Vector<String> groupsWhoWorkOnNowEvent;
    public Vector<AgentInfo> initiallizedAgentsInfo;
    public Vector<Event> initiallizedEventsInfo;

    public MainFrame(MainAgent ma)
    {
        mag=ma;
        sending=new SendingSenario();
        initComponents();
    }

    public void addText(String t)
    {
        // this is going to use a lot of heap memory if you want to create a lot of agents just comment two bellow lines
        if(jCheckBox3.isSelected())
        {
            String str=jTextArea1.getText();
            jTextArea1.setText(str+t+"\n");
        }
    }

    private void drawPoint(double x,double y)
    {
        Graphics2D g=(Graphics2D)getGraphics();
        Shape circle=new Ellipse2D.Double(x-4, y-4, 8, 8);
        g.draw(circle);
        g.setPaint(Color.BLUE);
        g.fill(circle);
    }

    private void drawSuperVisorPoint(double x,double y)
    {
        Graphics2D g=(Graphics2D)getGraphics();
        Shape circle=new Ellipse2D.Double(x-5, y-5, 10, 10);
        g.draw(circle);
        g.setPaint(Color.RED);
        g.fill(circle);
    }

    private void drawEventPoint(double x,double y)
    {
        Graphics2D g=(Graphics2D)getGraphics();
        Shape circle=new Ellipse2D.Double(x-4.5, y-4.5, 9, 9);
        g.draw(circle);
        g.setPaint(Color.YELLOW);
        g.fill(circle);
    }

    private void drawRectangle(int x,int y,int z,int t)
    {
        /*Graphics2D g=(Graphics2D)getGraphics();
        Shape rec=new Rectangle2D.Double(x,y,z,t);
        g.draw(rec);*/

        drawMyLine(x, y, z, y);
        drawMyLine(x, t, z, t);
        drawMyLine(x, y, x, t);
        drawMyLine(z, y, z, t);
    }

    private void drawMyLine(int x,int y,int z,int t)
    {
        Graphics2D g=(Graphics2D)getGraphics();
        g.drawLine(x, y, z, t);
    }

    private void drawCommunicationLine(CommunicationLine cl)
    {
        Graphics2D g=(Graphics2D)getGraphics();
        if(cl.x1==-1 && cl.y1==-1 && cl.x2==-1 && cl.y2==-1)    // change the color
        {
            if(colorOfLines==Color.PINK)
            {
                colorOfLines=Color.GREEN;
            }
            else if(colorOfLines==Color.GREEN)
            {
                colorOfLines=Color.MAGENTA;
            }
            else if(colorOfLines==Color.MAGENTA)
            {
                colorOfLines=Color.ORANGE;
            }
            else if(colorOfLines==Color.ORANGE)
            {
                colorOfLines=Color.GRAY;
            }
            else if(colorOfLines==Color.GRAY)
            {
                colorOfLines=Color.CYAN;
            }
            else
            {
                colorOfLines=Color.PINK;
            }
            return;
        }
        g.setColor(colorOfLines);
        g.drawLine((int)cl.x1, (int)cl.y1, (int)cl.x2, (int)cl.y2);
    }

    private void createHtmlFromDataSource(String[] headers,String[] resultHeaders)
    {
        try
        {
            JFileChooser jfc=new JFileChooser(new File(".").getAbsolutePath());
            jfc.showOpenDialog(null);
            if(jfc.getSelectedFile()==null) return;

            RandomAccessFile raf=new RandomAccessFile(jfc.getSelectedFile(), "rws");
            String html="<html>\n<table style=\"width: 100%; border-style: dashed; border-width: thin\" >";

            html+="\n<tr>";
            for(int i=0;i<headers.length;i++)
            {
                html+="\n<td style=\"font-size: x-large; border-style: dashed; border-width: medium\">"+headers[i]+"</td>";
            }
            html+="\n</tr>";

            while(true)
            {
                String line=raf.readLine();
                if(line==null || line.trim().equals("")) break;
                
                html+="\n<tr>";
                String[] elems=line.split(",");
                for(int i=0;i<elems.length;i++)
                {
                    html+="\n<td style=\"border-style: dashed; border-width: thin\">"+elems[i]+"</td>";
                }
                html+="\n</tr>";
            }
            
            html+="\n</table>";

            String diffLine=raf.readLine();
            if(diffLine!=null && !diffLine.trim().equals("") && resultHeaders!=null)
            {
                html+="\n<br /><br /><br />\n<table style=\"width: 100%; border-style: dashed; border-width: thin\" >\n<tr>";
                for(int i=0;i<resultHeaders.length;i++)
                {
                    html+="\n<td style=\"font-size: x-large; border-style: dashed; border-width: medium\">"+resultHeaders[i]+"</td>";
                }
                html+="\n</tr>\n<tr>";
                String[] elems=diffLine.split(",");
                for(int i=0;i<elems.length;i++)
                {
                    html+="\n<td style=\"border-style: dashed; border-width: thin\">"+elems[i]+"</td>";
                }
                html+="\n</tr>";
            }
            html+="\n</table>\n</html>";

            JFileChooser outFile=new JFileChooser(jfc.getCurrentDirectory().getCanonicalPath());
            while(true)
            {
                outFile.showSaveDialog(null);
                if(outFile.getSelectedFile()==null)
                {
                    JOptionPane.showMessageDialog(rootPane, "Please Select A File For Saving Html ...");
                }
                else
                {
                    break;
                }
            }

            FileWriter fw=new FileWriter(outFile.getSelectedFile());
            fw.write(html);
            fw.flush();
            fw.close();

            JOptionPane.showMessageDialog(rootPane, "Your Html File Was Saved Successfully .","Save",JOptionPane.INFORMATION_MESSAGE);
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendFromAg1ToAg2(AID ag1,AID ag2,String str)      // very high level and important method
    {
        String cont=ag2.getName()+"*"+str;
        ACLMessage msg=new ACLMessage(ACLMessage.CFP);
        msg.setContent(cont);
        msg.addReceiver(ag1);
        mag.send(msg);
        addText("OK Main Agent Sent The Senario Of The Message To The Agents With Names :"
               +ag1.getName()+" And "+ag2.getName()+" Successfully.");
        addText(ag1.getName()+" Sent Below Message To "+ag2.getName()+"\nmsg =  "+str);
        if(jCheckBox2.isSelected())
        {
            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
            tell.addReceiver(mag.counterAgentID);
            tell.setContent("1");
            mag.send(tell);
        }
    }

    public void killAllMyAgents()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.KillMyAgent);
        for(int i=0;i<coordinates.size();i++)
        {
            msg.addReceiver(coordinates.elementAt(i).agentID);
        }
        msg.addReceiver(mag.counterAgentID);
        msg.setContent("Kill Yourself With Your Takedown Function Please !!!");
        addText("Kill Messages Sent For All Agents Now .");
        coordinates.clear();
        mag.send(msg);
        if(jCheckBox2.isSelected())
        {
            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
            tell.addReceiver(mag.counterAgentID);
            tell.setContent(String.valueOf(coordinates.size()+1));
            mag.send(tell);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTextField4 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jTextField5 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jTextField6 = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jTextField7 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jButton12 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        jTextField12 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jButton14 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton15 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        jButton16 = new javax.swing.JButton();
        jTextField13 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextField14 = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Multi Agent Project");

        jButton1.setText("Create Agents");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setToolTipText("Enter number of agents that you want to create here (if your number is so big please first make sure that with logs checkbox is not checked)");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 24));
        jLabel1.setForeground(new java.awt.Color(153, 153, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Multi Agent Project");

        jButton2.setText("Exit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Show Agents");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multiagentproject/Community Help.png"))); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multiagentproject/Camino.png"))); // NOI18N

        jTextField2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField2.setToolTipText("Enter a number like x for having x rows");

        jTextField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField3.setToolTipText("Enter a number like y for having y columns");

        jLabel4.setText(": Parts In Height");

        jLabel5.setText("Parts In Width :");

        jButton4.setText("Show SuperVisors");
        jButton4.setEnabled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Send Info");
        jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField4.setToolTipText("Enter message contents here");

        jButton6.setText("Add New Event");
        jButton6.setEnabled(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("Refresh");
        jButton7.setEnabled(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Run Events");
        jButton8.setEnabled(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.setToolTipText("Radius of seeing of each agent that would be by pixel scale");

        jLabel6.setText("Sight Radius Of Agents :");

        jButton9.setText("Show Groups");
        jButton9.setEnabled(false);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jTextField6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField6.setToolTipText("Enter a number for number of events that you want to create");

        jButton10.setText("Add Automatic Events");
        jButton10.setEnabled(false);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jTextField7.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField7.setToolTipText("Enter the maximum number that any event could have every items with this maximum number");

        jLabel7.setText("Maximum Cost Of Each Event : ");

        jLabel8.setText("Number Of Events : ");

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/multiagentproject/access.png"))); // NOI18N

        jButton11.setText("Report");
        jButton11.setEnabled(false);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel10.setText("Number Of Agents : ");

        jLabel11.setText("From Agent : ");

        jLabel12.setText("To Agent : ");

        jTextField8.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField8.setToolTipText("Enter agent's number id that is sender of message (only a number)");

        jTextField9.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField9.setToolTipText("Enter agent's number id that is receiver of message (only a number)");

        jButton12.setText("Kill All Agents");
        jButton12.setEnabled(false);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jLabel13.setText("Increment Percent :");

        jTextField10.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField10.setToolTipText("Increment percent of intelligence and stamina of each agent after handling of each event(you can setted 0 for none)");

        jTextField11.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField11.setToolTipText("Decrement percent of speed and strength of each agent after handling of each event(you can setted 0 for none)");

        jLabel14.setText("Decrement Percent :");

        jLabel15.setText("(just a number please)");

        jButton13.setText("Simulate");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jTextField12.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField12.setToolTipText("Fill bellow fields then enter a number for times of simulations and press the simulate button");

        jLabel16.setText("Number Of Simulation :");

        jButton14.setText("Show One By One");
        jButton14.setEnabled(false);
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Show Communications Of One Event");
        jCheckBox1.setToolTipText("Check this if you want to see communication lines after finishing events ");

        jButton15.setText("Show All");
        jButton15.setEnabled(false);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jCheckBox2.setText("With Message Counter Agent");
        jCheckBox2.setToolTipText("Check it if you want to have an agent for counting all messages of system and show that to you");

        jButton16.setText("Browse A File Of Points");
        jButton16.setToolTipText("If you want to create agents in specific locations please browse a file here and then press Create Agents button (if number of agents in this file was less than the number of agents that you entered then rest of them will be create with random numbers)");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jTextField13.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField13.setToolTipText("Percent of increment in each event after above time for growth of events");

        jLabel17.setText("Interval Time (ms) :");

        jTextField14.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField14.setToolTipText("Time (delay) between growth of each event with ms(milli second) scale");

        jLabel18.setText("Increment Percent Of Event :");

        jCheckBox3.setSelected(true);
        jCheckBox3.setText("With Log");
        jCheckBox3.setToolTipText("Check it if you want to have some detailed logs of running of system (this can be a little time wastable)(if you want to create huge number of agents you should check off this)");

        jButton17.setText("Fill Auto");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jButton18.setText("Browse & Create Events");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jMenuBar1.setBackground(new java.awt.Color(255, 204, 204));

        jMenu1.setBackground(new java.awt.Color(255, 204, 204));
        jMenu1.setText("Configure Agents");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Create New Agents Now");
        jMenuItem1.setActionCommand("jMenuItem1");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAgentsMenuItemPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("Save All Configs & Results Now");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem4.setText("Create Html From Agents DataSet");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem5.setText("Create Html From Events DataSet");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuBar1.add(jMenu1);

        jMenu2.setBackground(new java.awt.Color(255, 204, 204));
        jMenu2.setText("Show Agents");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Refresh Board");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton17)
                        .addGap(18, 18, 18)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jCheckBox3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addGap(74, 74, 74))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel4))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(40, 40, 40)
                                        .addComponent(jLabel3)))
                                .addGap(84, 84, 84)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(jButton15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jButton14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(43, 43, 43))
                                            .addComponent(jCheckBox1))
                                        .addGap(8, 8, 8))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                        .addGap(10, 10, 10)
                                                        .addComponent(jButton9))
                                                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.LEADING))
                                                .addGap(33, 33, 33))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                .addGap(8, 8, 8)
                                                                .addComponent(jLabel12)
                                                                .addGap(18, 18, 18)
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                    .addComponent(jTextField8)
                                                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(jLabel15))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(29, 29, 29)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton16)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addGap(30, 30, 30)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addComponent(jLabel8)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(layout.createSequentialGroup()
                                                                    .addComponent(jLabel16)
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                    .addComponent(jButton13)
                                                                    .addGap(13, 13, 13)))
                                                            .addComponent(jLabel7))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(jTextField7)
                                                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(94, 94, 94)
                                                        .addComponent(jButton10)
                                                        .addGap(14, 14, 14))))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(23, 23, 23)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(14, 14, 14)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jButton12)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                                                                .addComponent(jButton18)
                                                                .addGap(2, 2, 2))
                                                            .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jCheckBox2)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE))))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addGroup(layout.createSequentialGroup()
                                                                .addGap(32, 32, 32)
                                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addGroup(layout.createSequentialGroup()
                                                                .addComponent(jLabel10)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                            .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 137, Short.MAX_VALUE)))))
                                        .addGap(10, 10, 10))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel17)
                                            .addComponent(jLabel18))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jTextField13)
                                            .addComponent(jTextField14, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jLabel13)
                                                    .addComponent(jLabel6)
                                                    .addComponent(jLabel14))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                                                    .addComponent(jTextField10)
                                                    .addComponent(jTextField11)))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(jButton8)
                                                .addGap(36, 36, 36)))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jButton6)
                                .addGap(42, 42, 42)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel9))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton7)
                                    .addComponent(jCheckBox3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel16))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jButton17)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton15)
                                .addGap(33, 33, 33)
                                .addComponent(jButton6)
                                .addGap(73, 73, 73)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(146, 146, 146)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(3, 3, 3)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel5))
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel4))))
                                            .addComponent(jButton3)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(70, 70, 70)
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel11)
                                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel12)
                                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(17, 17, 17)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jButton12)
                                                    .addComponent(jButton18))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jCheckBox2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel10)
                                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton5)
                                                .addGap(18, 18, 18)
                                                .addComponent(jButton4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jButton2))))))
                                .addGap(106, 106, 106))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(274, 274, 274)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13))
                                .addGap(4, 4, 4)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton8))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel17))
                                .addGap(4, 4, 4)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel18))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        //initializing =>
        coordinates=new Vector<AgentPoint>();
        initiallizedAgentsInfo=new Vector<AgentInfo>();
        events=new Vector<Event>();
        agentGroupName=new Hashtable<AID, String>();
        repev=new Vector<ReportEvent>();
        repevindex=0;
        mag.eventIndex=mag.firstEventIndex=1;
        mag.rep=new Report();
        
        String str=jTextField1.getText();
        int n=0;
        try
        {
            n=Integer.parseInt(str);
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, "Please Only Enter A Number In This Field .","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }
        mag.setNumberOfAgents(n);
        mag.createAgents();
        //jButton1.setEnabled(false);
        //jTextField1.setEnabled(false);
        jButton3.setEnabled(true);
        jButton12.setEnabled(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        System.exit(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private boolean isInRange(int x1,int y1,int x2,int y2,int x,int y)
    {
        return (x1<=x && x<=x2 && y1<=y && y<=y2);
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        for(int i=0;i<groups.length;i++)
        {
            for(int j=0;j<groups[0].length;j++)
            {
                if(groups[i][j].supervisor.x!=-1)   // if it is not null
                {
                    if(!refreshPressed)
                    {
                        addText(groups[i][j].supervisor.agentID.getName()+" Is Going To Be Supervisor Of "+groups[i][j].name+" .");
                    }
                    drawSuperVisorPoint(groups[i][j].supervisor.x, groups[i][j].supervisor.y);
                }
            }
        }
        jButton5.setEnabled(true);
        jButton6.setEnabled(true);
        jButton10.setEnabled(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String str=jTextField4.getText();
        String st=mag.getName();
        int index=st.indexOf("@");
        String tail=st.substring(index);
        String name1="Agent"+jTextField8.getText()+tail;
        String name2="Agent"+jTextField9.getText()+tail;
        AID a1=new AID(name1,AID.ISGUID);
        AID a2=new AID(name2,AID.ISGUID);
        sendFromAg1ToAg2(a1, a2, str);
        addText("Message Sent For Sending Senario From\n"+a1.getName()+"To\n"+a2.getName()+"\nSuccessfully Now .");
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        EventFrame ef=new EventFrame();
        ef.events=events;
        ef.repev=repev;
        ef.initiallizedEventsInfo=initiallizedEventsInfo;
        ef.eventIndex=mag.eventIndex++;
        autoMode=false;
        ef.show();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        refreshPressed=true;
        jButton3ActionPerformed(evt);
        jButton4ActionPerformed(evt);
        for(int i=0;i<events.size();i++)
        {
            drawEventPoint(events.elementAt(i).x, events.elementAt(i).y);
        }
        jButton8.setEnabled(true);
        refreshPressed=false;
        if(coordinates.size()!=0)
        {
            weHaveAgents=true;
        }
        else
        {
            weHaveAgents=false;
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        if(events.size()==0)
        {
            JOptionPane.showMessageDialog(rootPane, "Please Enter Some Events And After That Press Run Events Button ...","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }
        int r=-1,inc=0,dec=0;    // it means not set
        try
        {
            r=Integer.parseInt(jTextField5.getText());
            inc=Integer.parseInt(jTextField10.getText());
            dec=Integer.parseInt(jTextField11.getText());
            int dummy=Integer.parseInt(jTextField14.getText());   // only for check
            dummy=Integer.parseInt(jTextField13.getText());       // only for check
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, "Please Only Enter Three Number In These Fields For Radius Of Sight Of Agents And Increment & Decrement Percent & Time .","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }
        if(jCheckBox1.isSelected())    // it means user wants to see 1 communicaion line senario between my agents
        {
            comLines=new Vector<CommunicationLine>();
            agentLocation=new Hashtable<AID, Point>();
            for(AgentPoint ap : coordinates)
            {
                Point po=new Point(ap.x,ap.y);
                agentLocation.put(ap.agentID, po);
            }
            //indexComLines=0;
            jButton14.setEnabled(true);
            jButton15.setEnabled(true);
            ACLMessage message=new ACLMessage(MyPerformatives.ShowCommunication);
            for(AgentPoint ap : coordinates)
            {
                message.addReceiver(ap.agentID);
            }
            message.setContent("1");   // show agent's communicaions
            mag.send(message);
            if(jCheckBox2.isSelected())
            {
                ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                tell.addReceiver(mag.counterAgentID);
                tell.setContent(String.valueOf(coordinates.size()));
                mag.send(tell);
            }
        }
        if(autoMode)
        {
            beginAllAutoTime=System.currentTimeMillis();
        }
        mag.createEventAgents(inc,dec);     // now creating event agents
    }//GEN-LAST:event_jButton8ActionPerformed

    public void checkAroundAgents()
    {
        int r=Integer.parseInt(jTextField5.getText());
        double r2=Math.pow(r, 2);    // r^2
        for(int i=0;i<events.size();i++)
        {
            double ex=events.elementAt(i).x;
            double ey=events.elementAt(i).y;
            boolean firsttime=true;
            String firstgroupwhounderstand="nothing";
            for(int j=0;j<coordinates.size();j++)
            {
                double a=ex-coordinates.elementAt(j).x;
                double b=ey-coordinates.elementAt(j).y;
                double res=Math.pow(a,2)+Math.pow(b, 2);
                if(res<r2)
                {
                    AID ag=coordinates.elementAt(j).agentID;
                    String gname=agentGroupName.get(ag);    // his/her group name
                    if(firsttime)
                    {
                        firstgroupwhounderstand=gname;
                        firsttime=false;
                    }
                    if(firstgroupwhounderstand.compareToIgnoreCase(gname)==0)
                    {
                        events.elementAt(i).agentsWhoKnows.add(ag);
                    }
                    addText(ag.getName()+" Makes Sense About "+events.elementAt(i).eventname+" Now .");
                }
            }
        }
        runNextEvent();
        jButton8.setEnabled(false);
        jButton11.setEnabled(true);
    }

    //int counter=1;
    public void runNextEvent()
    {
        //JOptionPane.showMessageDialog(rootPane, "own =>  " + counter++);
        if(events.isEmpty())
        {
            if(isSimulation)
            {
                killAllMyAgents();     // it takes a long time and this is going to be harder
            }
            allDuration=System.currentTimeMillis()-beginAllAutoTime;
            if(autoMode)      // autoMode && !isSimulation
            {
                JOptionPane.showMessageDialog(rootPane, "\n<= System Report In "+allDuration+" ms =>"+mag.rep,"Report",JOptionPane.INFORMATION_MESSAGE);
            }
            if(isSimulation)
            {
                doWholeSimulation(5);   // ok simulation ended now go to report that on pdf
            }
            return ;
        }
        mag.begintime=System.currentTimeMillis();
        int nextEvent=0;    // top of that because last events removed
        Event ev=events.elementAt(nextEvent);
        if(ev.agentsWhoKnows.size()!=0)
        {
            groupsWhoWorkOnNowEvent=new Vector<String>();
            ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);
            Envelope en=new Envelope();
            en.addProperties(new Property("Event "+ev.name, ev));
            msg.setEnvelope(en);
            String groupName=agentGroupName.get(ev.agentsWhoKnows.elementAt(0));
            ev.groupsWhoHelp.add(groupName);
            for(int j=0;j<ev.agentsWhoKnows.size();j++)
            {
                msg.addReceiver(ev.agentsWhoKnows.elementAt(j));
            }
            mag.send(msg);
            addText("Propose Messages For Handling Event "+events.elementAt(nextEvent++).name+" Sent By Now .");
            if(jCheckBox2.isSelected())
            {
                ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                tell.addReceiver(mag.counterAgentID);
                tell.setContent(String.valueOf(ev.agentsWhoKnows.size()));
                mag.send(tell);
            }
        }
        else
        {
            //probablity 1
            // no one can see that
            String str=mag.getName();
            int index=str.indexOf("@");
            String name=str.substring(index);
            name=events.elementAt(nextEvent).eventname+name;
            AID evag=new AID(name, AID.ISGUID);
            addText("Event "+events.elementAt(nextEvent).name+" Or In Other Word '"+events.elementAt(nextEvent).eventname+"' Was Not Be Seen By Any Agents , So Failed And Terminated ...");
            long endtime=System.currentTimeMillis();
            long duration=(endtime-mag.begintime);
            if(!autoMode)
            {
                JOptionPane.showMessageDialog(rootPane, "Event '"+events.elementAt(nextEvent).name+"' Or In Other Word "+events.elementAt(nextEvent).eventname+" Handling Failed Because No Agent Saw That , So Terminated Automaticly And This Takes "+duration+" ms .","FAILURE",JOptionPane.INFORMATION_MESSAGE);
            }
            else
            {
                addText("\nFAILURE=> Event '"+events.elementAt(nextEvent).name+"' Or In Other Word "+events.elementAt(nextEvent).eventname+" Handling Failed Because No Agent Saw That , So Terminated Automaticly And This Takes "+duration+" ms .\n");
            }
            repev.elementAt(repevindex).duration=duration;
            repev.elementAt(repevindex).succeed=false;
            repev.elementAt(repevindex++).numAgent=0;    // no one can see that
            ACLMessage failmsg=new ACLMessage(ACLMessage.CANCEL);
            failmsg.setContent("Take Down The Event Agent");
            failmsg.addReceiver(evag);
            events.remove(nextEvent++);
            mag.send(failmsg);
            if(jCheckBox2.isSelected())
            {
                ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                tell.addReceiver(mag.counterAgentID);
                tell.setContent("1");
                mag.send(tell);
            }
            // it doesn't need to message for no concern because no one makes a sense about this event so no one is concerned about it
            mag.rep.increaseFailure();
            runNextEvent();
        }
    }

    public void doWholeSimulation(int part)
    {
        switch(part)
        {
            case 1:    // creating agents
                System.err.println("part 1 simulation started ...");
                jButton1ActionPerformed(null);    // after this creating takes time
                break;

            case 2:
                System.err.println("part 2 simulation started ...");
                jButton7ActionPerformed(null);
                break;
                
            case 3:
                System.err.println("part 3 simulation started ...");
                jButton10ActionPerformed(null);
                part=4;     // go to next part

            case 4:
                System.err.println("part 4 simulation started ...");
                jButton8ActionPerformed(null);
                break;

            case 5:
                System.err.println("part 5 simulation started ...");
                reportThisSimulation();
                break;

            case 6:
                System.err.println("part 6 simulation started ...");
                runNextSimulation();
                break;

            default:
                JOptionPane.showMessageDialog(rootPane, "An Error In doWholeSimulation Swith Case Has Been Occurred .","ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runNextSimulation()
    {
        if(indexOfSimulations>numberOfSimulations)
        {
            indexOfSimulations=1;
            numberOfSimulations=0;
            isSimulation=false;
            JOptionPane.showMessageDialog(rootPane, "All Simulations Have Been Done Successfully Now , You Can See The Pdf Report Files For Detailed Information About Them .","Succeed",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        doWholeSimulation(1);      // 1 == it means part 1 or first episode
    }

    private void reportThisSimulation()
    {
        ReportFrame repform=new ReportFrame();
        repform.repev=repev;
        repform.rep=mag.rep;
        repform.totalDur=allDuration;
        repform.number=coordinates.size();
        repform.showButtonFunction();
        repform.pdfFileName="MultiAgentReport"+indexOfSimulations+".pdf";
        repform.printToPdfFunction();
        //killAllMyAgents();        // it takes a long time and this is going to be harder
        indexOfSimulations++;
        repform.dispose();
    }

    Object doWork()
    {
        try
        {
            while(!comLines.isEmpty())
            {
                drawCommunicationLine(comLines.remove(0));
                Thread.sleep(100);
            }
        }
        catch (InterruptedException e)
        {
            return "Interrupted";  // SwingWorker.get() returns this
        }
        return "All Done";         // or this
    }

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        GroupsFrame gf=new GroupsFrame();
        gf.groups=groups;
        for(int i=0;i<groups.length;i++)
        {
            for(int j=0;j<groups[0].length;j++)
            {
                gf.jComboBox1.addItem(groups[i][j].name);
            }
        }
        gf.show();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        int max=0;
        int number=0;
        try
        {
            number=Integer.parseInt(jTextField6.getText());
            max=Integer.parseInt(jTextField7.getText());
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, "Please Fill The Both Fields Above And Then Press This Button ...","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }

        initiallizedEventsInfo=new Vector<Event>();
        autoMode=true;
        max++;   // because i want to generate a number between 0 and max
        for(int i=0;i<number;i++)
        {
            Event ev=new Event();
            Random r=new Random();
            int x=100+r.nextInt(400);
            int y=100+r.nextInt(400);
            ev.x=x;
            ev.y=y;
            ev.strengthNeeded=r.nextInt(max);
            ev.staminaNeeded=r.nextInt(max);
            ev.speedNeeded=r.nextInt(max);
            ev.intelligenceNeeded=r.nextInt(max);
            ev.name="FIRE"+mag.eventIndex;
            ev.eventname="Event"+mag.eventIndex++;
            events.add(ev);
            repev.add(new ReportEvent(ev));
            initiallizedEventsInfo.add(ev);
        }
        // drawing them after add them for speed of show all of events
        for(int i=0;i<events.size();i++)
        {
            drawEventPoint(events.elementAt(i).x, events.elementAt(i).y);
        }
        jButton8.setEnabled(true);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        ReportFrame rf=new ReportFrame();
        rf.repev=repev;
        rf.rep=mag.rep;
        rf.totalDur=allDuration;
        rf.number=coordinates.size();
        rf.show();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed

        // KILL ALL AGENTS =>
        killAllMyAgents();

    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed

        isSimulation=true;
        String str=jTextField12.getText();
        int n=0;
        try
        {
            n=Integer.parseInt(str);
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, "Please Only Enter A Number In This Field .","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }
        numberOfSimulations=n;
        runNextSimulation();
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed

        if(comLines.isEmpty())
        { 
            jButton14.setEnabled(false);
            jButton15.setEnabled(false);
            //JOptionPane.showMessageDialog(rootPane, "Event Handling Senario Is Finished Now .","FINISHED",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CommunicationLine cl=comLines.remove(0);
        drawCommunicationLine(cl);
        //indexComLines++;

    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed

       worker = new SwingWorker()
       {
            public Object construct()
            {
                return doWork();
            }

            @Override
            public void finished()
            {
                jButton14.setEnabled(false);
                jButton15.setEnabled(false);
            }
        };

        worker.start();
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        JFileChooser jf=new JFileChooser();
        jf.showOpenDialog(null);
        if(jf.getSelectedFile()==null)
        {
            return ;
        }
        String pointsFilePath=jf.getSelectedFile().getPath();
        int i;
        for(i=pointsFilePath.length()-1;i>=0;i--)
        {
            if(pointsFilePath.charAt(i)=='\\')
            {
                break;
            }
        }
        String st=pointsFilePath.substring(0, i);
        myPointsFilePath=st+"\\myFile.dat";
        File myFile=new File(myPointsFilePath);
        try
        {
            myFile.delete();
            myFile.createNewFile();
            RandomAccessFile raf=new RandomAccessFile(pointsFilePath, "rws");
            RandomAccessFile myf=new RandomAccessFile(myFile, "rws");
            int index=1;
            while(true)
            {
                String str=raf.readLine();
                if(str==null)
                {
                    break;
                }
                String[] res=str.split(",");
                double[] dt=new double[6];  // x , y and 4 capabilities of agent
                for(int idx=0;idx<dt.length;idx++)
                {
                    dt[idx]=Double.parseDouble(res[idx]);
                }
                myf.seek(dt.length*8*index);
                for(int idx=0;idx<dt.length;idx++)
                {
                    myf.writeDouble(dt[idx]);
                }
                index++;
            }
            myf.close();
        }
        catch(IOException ex)
        {
            JOptionPane.showMessageDialog(rootPane, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton16ActionPerformed

    private void createAgentsMenuItemPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createAgentsMenuItemPerformed
        jButton1ActionPerformed(evt);
    }//GEN-LAST:event_createAgentsMenuItemPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        jTextField1.setText("100");
        jTextField2.setText("3");
        jTextField3.setText("3");
        jCheckBox2.setSelected(true);
        jCheckBox3.setSelected(false);
        jCheckBox1.setSelected(true);
        jTextField7.setText("2000");
        jTextField6.setText("30");
        jTextField5.setText("50");
        jTextField14.setText("10");
        jTextField13.setText("5");
        jTextField10.setText("10");
        jTextField11.setText("10");
        jTextField12.setText("5");
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if(jButton7.isEnabled())
        {
            jButton7ActionPerformed(evt);
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        final int x1=100;
        final int y1=100;
        final int x2=500;
        final int y2=500;
        int diff=x2-x1;
        String str=jTextField2.getText();
        int h=0;
        try {
            h=Integer.parseInt(str);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(rootPane, "Please Only Enter A Number In This Field For Height .","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }
        str=jTextField3.getText();
        int w=0;
        try {
            w=Integer.parseInt(str);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(rootPane, "Please Only Enter A Number In This Field For Width .","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }

        int p1=diff/h;
        int p2=diff/w;

        groups=new Group[w][h];
        for(int i=0;i<w;i++) {
            for(int j=0;j<h;j++) {
                int xp1=x1+i*p1;
                int yp1=y1+j*p2;
                int xp2=x1+(i+1)*p1;
                int yp2=y1+(j+1)*p2;
                int num=(i*h)+j+1;
                groups[i][j]=new Group("Group "+num, xp1, yp1, xp2, yp2);
            }
        }

        // horizental lines :
        for(int i=y1+p1;i<=y2;i+=p1) {
            drawMyLine(x1, i, x2, i);
        }
        //vertical lines :
        for(int i=x1+p2;i<=x2;i+=p2) {
            drawMyLine(i, y1, i, y2);
        }
        drawRectangle(x1,y1,x2,y2);
        for(int i=0;i<coordinates.size();i++) {
            double xe=coordinates.elementAt(i).x;
            double ye=coordinates.elementAt(i).y;
            AID ai=coordinates.elementAt(i).agentID;
            drawPoint(xe,ye);
            int loc1=(int)(xe-x1)/p2;
            int loc2=(int)(ye-y1)/p1;
            groups[loc1][loc2].addAgentPoint(new AgentPoint(xe,ye,ai));
            agentGroupName.put(ai, groups[loc1][loc2].name);
        }
        for(int i=0;i<groups.length;i++) {
            for(int j=0;j<groups[0].length;j++) {
                String st="Main Agent Sent The Group Information Message To The Agents With Names :  ";
                ACLMessage msg=new ACLMessage(MyPerformatives.GivingGroupInfo);
                for(int k=0;k<groups[i][j].ags.size();k++) {
                    AID ai=groups[i][j].ags.elementAt(k).agentID;
                    msg.addReceiver(ai);
                    st+=ai.getName()+"  ";
                }
                Envelope en=new Envelope();
                en.addProperties(new Property("Group", groups[i][j]));
                msg.setEnvelope(en);
                String content=(isSimulation) ? "1" : "0";
                msg.setContent(content);
                mag.send(msg);
                if(jCheckBox2.isSelected()) {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(mag.counterAgentID);
                    tell.setContent(String.valueOf(groups[i][j].ags.size()));
                    mag.send(tell);
                }
                st+= "Successfully .";
                if(!refreshPressed) {
                    addText(st);
                }
            }
        }
        jButton4.setEnabled(true);
        jButton7.setEnabled(true);
        jButton9.setEnabled(true);
}//GEN-LAST:event_jButton3ActionPerformed

    public void saveConfig()
    {
        try
        {
            // save agents info
            JFileChooser out=new JFileChooser(new File(".").getCanonicalPath());
            out.showSaveDialog(null);

            if(out.getSelectedFile()!=null)
            {
                RandomAccessFile raf=new RandomAccessFile(out.getSelectedFile().getPath(), "rws");
                for(AgentInfo info:initiallizedAgentsInfo)
                {
                    raf.write(info.toString().getBytes());
                    // these two means an Enter
                    raf.write(13);
                    raf.write(10);
                }
                raf.close();
                
                JOptionPane.showMessageDialog(rootPane, "Agents' Config Was Successfully Saved .","Success",JOptionPane.INFORMATION_MESSAGE); 
            }
            

            // save events info
            JFileChooser outEvents=new JFileChooser(out.getSelectedFile().getAbsolutePath());
            outEvents.showSaveDialog(null);

            if(outEvents.getSelectedFile()!=null)
            {
                RandomAccessFile raf=new RandomAccessFile(outEvents.getSelectedFile().getPath(), "rws");
                for(int i=0;i<initiallizedEventsInfo.size();i++)
                {
                    String eventData=initiallizedEventsInfo.elementAt(i).toShortString()+","+repev.elementAt(i).toString();                      
                    raf.write(eventData.getBytes());
                    // these two means an Enter
                    raf.write(13);
                    raf.write(10);
                }
                raf.write(13);
                raf.write(10);
                
                double sum=0;       // sum of time for successful events
                double allsum=0;    // sum of time for all events
                for(ReportEvent re : repev)
                {
                    allsum+=re.duration;
                    if(re.succeed) sum+=re.duration;
                }
                //jTable1.setModel(model);
                double avg=sum/(double)mag.rep.getSuccess();
                double allavg=allsum/(double)repev.size();
                
                String resultOfSim=allDuration+","+coordinates.size()+","+mag.rep.successPercent()+","+mag.rep.failurePercent()+","+avg+","+allavg;
                raf.write(resultOfSim.getBytes());     
                raf.close();
               
                JOptionPane.showMessageDialog(rootPane, "Events' Config Was Successfully Saved .","Success",JOptionPane.INFORMATION_MESSAGE); 
            }
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed

        saveConfig();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        
        try
        {
            JFileChooser jfc=new JFileChooser(new File(".").getCanonicalPath());
            jfc.showOpenDialog(null);
            if(jfc.getSelectedFile()!=null)
            {
                initiallizedEventsInfo=new Vector<Event>();
                RandomAccessFile raf=new RandomAccessFile(jfc.getSelectedFile(), "rws");
                while(true)
                {
                    String line="";
                    try
                    {
                        line=raf.readLine();
                    }
                    catch(Exception ex)
                    {
                        break;
                    }
                    if(line==null || line.trim().length()==0) break;

                    String[] infos=line.split(",");

                    Event ev=new Event();
                    ev.x=Double.parseDouble(infos[0]);
                    ev.y=Double.parseDouble(infos[1]);
                    ev.strengthNeeded=Long.parseLong(infos[2]);
                    ev.staminaNeeded=Long.parseLong(infos[3]);
                    ev.speedNeeded=Long.parseLong(infos[4]);
                    ev.intelligenceNeeded=Long.parseLong(infos[5]);

                    ev.name="FIRE"+mag.eventIndex;
                    ev.eventname="Event"+mag.eventIndex++;
                    events.add(ev);
                    repev.add(new ReportEvent(ev));
                    initiallizedEventsInfo.add(ev);
                }

                // drawing them after add them for speed of show all of events
                for(int i=0;i<events.size();i++)
                {
                    drawEventPoint(events.elementAt(i).x, events.elementAt(i).y);
                }
                jButton8.setEnabled(true);
            }
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed

        createHtmlFromDataSource(new String[]{"Agent's X","Agent's Y","Strength","Stamina","Speed","Intelligence"},null);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed

        createHtmlFromDataSource(new String[]{"Event's X","Event's Y","EventName","NameInSystem","StrengthNeeded","StaminaNeeded","SpeedNeeded","IntelligenceNeeded","NumberOfAgents","Duration","Succeed"},new String[]{"AllDuration","NumberOfAgents","successPercent","failurePercent","Average Success Handling Time","Average Total Handling Time"});
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame(new MainAgent()).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    public javax.swing.JCheckBox jCheckBox1;
    public javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    public javax.swing.JTextField jTextField10;
    public javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    public javax.swing.JTextField jTextField13;
    public javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables

}
