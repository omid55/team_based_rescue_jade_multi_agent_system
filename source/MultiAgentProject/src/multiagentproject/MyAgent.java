/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
/**
 *
 * @author omid
 */
public class MyAgent extends Agent
{
    private double x;
    private double y;

    //AgentCapability =>
    private int strength;
    private int stamina;
    private int speed;
    private int intelligence;

    private int numberOfFacedEvents;
    private String nameOfFacedEvents="";
    private Group myGroup;
    private boolean isConcernedAbout;     // about any event
    private Event knownEvent;             // when any event occured agents fill this
    private Vector<AgentInfo> agsInfo;    // when any event occured supervisor agents fill this because of think about handling event
    private Vector<String> agsFinished;
    private String nextSupervisorName;    // when an event handling doesn't complete in a group that supervisor must send a help message to another supervisor but when 2 condition have been satisfied :
    private Envelope envNextSupervisor;   // conditions =>  1- all of agents of that group sends that they finished their job   2- main agent tell the name and event infos so i keep them in 2 attribute
    private boolean isKilled=false;

    private AgentFrame agFrame;
    private AID MainAgentID;

    public boolean showCommunication=false;
    public static boolean tell2CounterAgent=false;
    public static AID counterAgentID;
    public String myPointsFilePath=null;
    public boolean told2MainAgent=false;
    public int numberOfUsefullAgents=0;

    @Override
    protected void setup()
    {
        agFrame=new AgentFrame(this);
        Object[] obj=getArguments();
        MainAgentID=(AID)obj[0];
        tell2CounterAgent=(Boolean)obj[1];
        if(tell2CounterAgent)
        {
            counterAgentID=(AID)obj[2];
        }
        if(obj[3]!=null)
        {
            myPointsFilePath=(String)obj[3];
            try
            {
                RandomAccessFile raf=new RandomAccessFile(myPointsFilePath, "rws");
                String name=getName();
                int ind=name.indexOf('@');
                String prefix="Agent";
                String num=name.substring(prefix.length(), ind);
                int number=Integer.parseInt(num);
                raf.seek(6*8*number);
                
                x=raf.readDouble();
                y=raf.readDouble();
                //x=100+raf.readDouble()*10;
                //y=100+raf.readDouble()*10;

                strength=(int)raf.readDouble();
                stamina=(int)raf.readDouble();
                speed=(int)raf.readDouble();
                intelligence=(int)raf.readDouble();
            }
            catch(IOException ex)
            {
                createInfoByRand();
            }
        }
        else
        {
            createInfoByRand();
        }

        agFrame.jLabel5.setText(String.valueOf(strength));
        agFrame.jProgressBar1.setMaximum(100);
        agFrame.jProgressBar1.setValue(strength);
        agFrame.jLabel6.setText(String.valueOf(stamina));
        agFrame.jProgressBar2.setMaximum(100);
        agFrame.jProgressBar2.setValue(stamina);
        agFrame.jLabel8.setText(String.valueOf(speed));
        agFrame.jProgressBar4.setMaximum(100);
        agFrame.jProgressBar4.setValue(speed);
        agFrame.jLabel7.setText(String.valueOf(intelligence));
        agFrame.jProgressBar3.setMaximum(100);
        agFrame.jProgressBar3.setValue(intelligence);
        agFrame.jLabel11.setText(String.valueOf(x));
        agFrame.jLabel12.setText(String.valueOf(y));
        agFrame.jLabel13.setText(this.getName());
        String name=getName();
        int l=name.indexOf('@');
        agFrame.setTitle(name.substring(0,l));

        isConcernedAbout=false;
        numberOfFacedEvents=0;
        nextSupervisorName="";

        ContentManager manager=getContentManager();
        Codec lang=new SLCodec();
        manager.registerLanguage(lang);
        DFAgentDescription desc=new DFAgentDescription();
        desc.setName(getAID());
        desc.addLanguages(lang.getName());
        ServiceDescription service=new ServiceDescription();
        service.addLanguages(lang.getName());
        service.setName(getLocalName());
        service.setType("My-Agent");
        desc.addServices(service);
        /*try
        {
            DFService.register(this, desc);
        }
        catch(FIPAException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
            doDelete();
            return ;
        }*/

        //finding AID of main agent :   MainAgentID
        /*DFAgentDescription res[];
        DFAgentDescription dfa=new DFAgentDescription();
        ServiceDescription sd=new ServiceDescription();
        sd.setType("Main-Agent");
        dfa.addServices(sd);
        try
        {
            res = DFService.search(this, dfa);
            MainAgentID=res[0].getName();
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR In Main Agent ID Retrieving",JOptionPane.ERROR_MESSAGE);
        }*/

        addBehaviour(new sendCoordinate());
        addBehaviour(new SendMsgWithSenario());
        addBehaviour(new GiveMsgWithSenario());
        addBehaviour(new GiveGroupInfo());
        addBehaviour(new GiveEventInfo());
        addBehaviour(new SendInfoToSenderAgent());
        addBehaviour(new GiveAgentInfo());
        addBehaviour(new SendForHandling());
        addBehaviour(new GiveNewCapabilities());
        addBehaviour(new GiveSignalNoConcern());
        addBehaviour(new SendHelpSignalToAdjacent());
        addBehaviour(new GiveKillSignal());
        addBehaviour(new GiveFinishedSignal());
        addBehaviour(new SendToNextSupervisor());
        addBehaviour(new GiveShowCommunication());
        addBehaviour(new GiveUpToDateEventInfo());
        addBehaviour(new GiveUpdatedInfoAfterHandling());
        
        agFrame.show();
    }

    @Override
    protected void takeDown()
    {
        if(isKilled==true)
        {
            return;
        }
        isKilled=true;

        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException ex)
        {
            //JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("Agent "+getName()+" Terminated ...");
        agFrame.dispose();
        ACLMessage msg=new ACLMessage(MyPerformatives.ImGoingToBeKilled);
        msg.addReceiver(MainAgentID);
        msg.setContent("I Am Going To Be Killed In Next Few Moments");

        killAgent(this, getAID());      // i can swap the location this 2 line if i need
        this.send(msg);
        if(tell2CounterAgent)
        {
            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
            tell.addReceiver(counterAgentID);
            tell.setContent("1");
            this.send(tell);
        }
    }

    public void createInfoByRand()
    {
        Random r=new Random();
        
        x=99+r.nextInt(400);     // because i want to be out of area
        y=99+r.nextInt(400);
        
        strength=r.nextInt(101);
        stamina=r.nextInt(101);
        speed=r.nextInt(101);
        intelligence=r.nextInt(101);
    }

    public static void killAgent(Agent ag,AID aid)
    {
        try
        {
            KillAgent kill=new KillAgent();
            kill.setAgent(aid);
            // create and send the message to the ams
            ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
            ag.getContentManager().registerOntology(JADEManagementOntology.getInstance(),JADEManagementOntology.NAME);
            msg.setOntology(JADEManagementOntology.NAME);
            msg.setLanguage(ag.getContentManager().lookupLanguage("fipa-sl").getName());
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            ag.getContentManager().fillContent(msg, new Action(ag.getAMS(), kill));
            msg.addReceiver(ag.getAMS());
            ag.send(msg);
            if(tell2CounterAgent)
            {
                ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                tell.addReceiver(counterAgentID);
                tell.setContent("1");
                ag.send(tell);
            }
        }
        catch (CodecException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        catch (OntologyException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public AgentInfo getAgWithMostIntelligence(Vector<AgentInfo> v)
    {
        int max=-9999999;
        int index=-1;
        for(int i=0;i<v.size();i++)
        {
            if(max<v.elementAt(i).intelligence)
            {
                max=v.elementAt(i).intelligence;
                index=i;
            }
        }
        return v.elementAt(index);
    }

    public AgentInfo getAgWithMostSpeed(Vector<AgentInfo> v)
    {
        int max=-9999999;
        int index=-1;
        for(int i=0;i<v.size();i++)
        {
            if(max<v.elementAt(i).speed)
            {
                max=v.elementAt(i).speed;
                index=i;
            }
        }
        return v.elementAt(index);
    }
        
    public AgentInfo getAgWithMostStamina(Vector<AgentInfo> v)
    {
        int max=-9999999;
        int index=-1;
        for(int i=0;i<v.size();i++)
        {
            if(max<v.elementAt(i).stamina)
            {
                max=v.elementAt(i).stamina;
                index=i;
            }
        }
        return v.elementAt(index);
    }
            
    public AgentInfo getAgWithMostStrength(Vector<AgentInfo> v)
    {
        int max=-9999999;
        int index=-1;
        for(int i=0;i<v.size();i++)
        {
            if(max<v.elementAt(i).strength)
            {
                max=v.elementAt(i).strength;
                index=i;
            }
        }
        return v.elementAt(index);
    }

    public long getMax(long a1,long a2,long a3,long a4)
    {
        if(a1>=a2 && a1>=a3 && a1>=a4) return a1;
        if(a2>=a1 && a2>=a3 && a2>=a4) return a2;
        if(a3>=a1 && a3>=a2 && a3>=a4) return a3;
        if(a4>=a1 && a4>=a2 && a4>=a3) return a4;
        return -1;                                    // it means error
    }

    public boolean amISuperVisor()       // am I super visor of my group
    {
        return myGroup.supervisor.agentID.getName().equalsIgnoreCase(getName());
    }

    public Group getMyGroup()
    {
        return myGroup;
    }

    private class sendCoordinate extends Behaviour  // or OneShotBehaviour
    {
        private boolean isitdone=false;
        //private MessageTemplate mt;

        @Override
        public void action()
        {
            ACLMessage msg=new ACLMessage(MyPerformatives.CoordinateInfo);
            msg.addReceiver(MainAgentID);
            String cont=String.valueOf(x)+","+String.valueOf(y)+","+String.valueOf(strength)+","+String.valueOf(stamina)+","+String.valueOf(speed)+","+String.valueOf(intelligence);
            msg.setContent(cont);
            msg.setConversationId("My-Coordinate");
            //msg.setReplyWith("coordinate"+System.currentTimeMillis());  // unique value
            //mt=MessageTemplate.and(MessageTemplate.MatchConversationId("My-Coordinate"),MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
            myAgent.send(msg);
            isitdone=true;
            if(tell2CounterAgent)
            {
                ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                tell.addReceiver(counterAgentID);
                tell.setContent("1");
                myAgent.send(tell);
            }
        }

        @Override
        public boolean done()
        {
            return isitdone;
        }
    }

    private class SendMsgWithSenario extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage senarioMsg=myAgent.receive(mt);
            if(senarioMsg!=null)
            {
                String str=senarioMsg.getContent();
                int l=0;
                while(str.charAt(l)!='*')
                {
                    l++;
                }
                String name=str.substring(0, l);
                String cont=str.substring(l+1);
                AID agn=new AID(name, AID.ISGUID);                    // AID agn=new AID(name, AID.ISLOCALNAME);
                ACLMessage msg=new ACLMessage(ACLMessage.CONFIRM);
                msg.addReceiver(agn);
                msg.setContent(cont);
                msg.setConversationId("My-Agent-Send-Info");
                myAgent.send(msg);
                agFrame.jLabel15.setText("1- This Agent Sent The Msg ;)");
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }
    }

    private class GiveMsgWithSenario extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                String cont=reply.getContent();
                agFrame.jLabel15.setText("2- Received Msg=> "+cont);
            }
            else
            {
                block();
            }
        }
    }

    private class GiveGroupInfo extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GivingGroupInfo);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                String isSimulationInMain=msg.getContent();
                myGroup=(Group)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                if(myGroup.supervisor.agentID.getName().equalsIgnoreCase(getName()))
                {
                    agFrame.jLabel16.setVisible(true);
                }
                agFrame.jButton3.setEnabled(true);
                msg.getEnvelope().clearAllProperties();
                msg.getEnvelope().clearAllTo();
                if(isSimulationInMain.compareTo("1")==0)
                {
                    ACLMessage reply=msg.createReply();    // send it to main agent
                    reply.setPerformative(MyPerformatives.GotGroupInfo);
                    reply.setContent("I Got MyGroup Info Now");
                    myAgent.send(reply);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }
            }
            else
            {
                block();
            }
        }
    }

    private class GiveEventInfo extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(!amISuperVisor())
                {
                    //isConcernedAbout=true;
                    ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);
                    Envelope env=reply.getEnvelope();
                    Event ev=(Event)((Property)env.getAllProperties().next()).getValue();
                    knownEvent=new Event(ev);
                    msg.setEnvelope(env);
                    msg.addReceiver(myGroup.supervisor.agentID);
                    myAgent.send(msg);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                    if(showCommunication)
                    {
                        ACLMessage mes=new ACLMessage(MyPerformatives.CommunicationLine);
                        mes.addReceiver(MainAgentID);
                        mes.setContent(getAID().getName()+","+myGroup.supervisor.agentID.getName());
                        myAgent.send(mes);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent("1");
                            myAgent.send(tell);
                        }
                    }
                }
                else
                {
                    if(!isConcernedAbout)
                    {
                        isConcernedAbout=true;
                        ACLMessage work=new ACLMessage(MyPerformatives.MyGroupWorksOnNowEvent);
                        work.setContent(myGroup.name); // My Group Is Going To Work On Now Event
                        work.addReceiver(MainAgentID);
                        myAgent.send(work);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent(String.valueOf(myGroup.ags.size()));
                            myAgent.send(tell);
                        }

                        Envelope env=reply.getEnvelope();
                        Event ev=(Event)((Property)env.getAllProperties().next()).getValue();
                        knownEvent=new Event(ev);

                        // now we must think about the handling event
                        //System.out.println(getAID());
                        ACLMessage msg=new ACLMessage(ACLMessage.PROPAGATE);
                        for(int i=0;i<myGroup.ags.size();i++)
                        {
                            msg.addReceiver(myGroup.ags.elementAt(i).agentID);
                        }
                        myAgent.send(msg);
                        reply.getEnvelope().clearAllProperties();
                        reply.getEnvelope().clearAllTo();
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent(String.valueOf(myGroup.ags.size()));
                            myAgent.send(tell);
                        }
                    }
                }
            }
            else
            {
                block();
            }
        }

    }

    private class SendInfoToSenderAgent extends CyclicBehaviour     // send info to supervisor
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                ACLMessage msg=new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                Envelope en=new Envelope();
                AgentInfo info=new AgentInfo(x, y, getAID(), strength, stamina, speed, intelligence);
                en.addProperties(new Property("AgentInfo", info));
                msg.setEnvelope(en);
                msg.addReceiver(reply.getSender());
                myAgent.send(msg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
                /*if(showCommunication)
                {
                    ACLMessage mes=new ACLMessage(MyPerformatives.CommunicationLine);
                    mes.addReceiver(MainAgentID);
                    mes.setContent(getAID().getName()+","+reply.getSender().getName());
                    myAgent.send(mes);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }*/
            }
            else
            {
                block();
            }
        }

    }

    private class GiveAgentInfo extends CyclicBehaviour     // give agent info and place in vector agInfo by supervisor agent
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(agsInfo==null)
                {
                    agsInfo=new Vector<AgentInfo>();
                }
                Envelope en=reply.getEnvelope();
                Property pr=(Property)en.getAllProperties().next();
                AgentInfo info=(AgentInfo)pr.getValue();
                if(agsInfo.contains(info)==false)
                {
                    agsInfo.add(info);
                }
                reply.getEnvelope().clearAllProperties();
                reply.getEnvelope().clearAllTo();
                if(agsInfo.size()!=myGroup.ags.size())
                {
                    return ;
                }
                else
                {
                    String ss=MainAgentID.getName();
                    int idx=ss.indexOf("@");
                    String tl=ss.substring(idx);
                    String eventName=knownEvent.eventname+tl;
                    AID evenId=new AID(eventName, AID.ISGUID);
                    ACLMessage upd=new ACLMessage(MyPerformatives.UpdateEventInfo);
                    upd.addReceiver(evenId);
                    upd.setContent("Give Me Your Up To Date Requirements ...");
                    myAgent.send(upd);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }
            }
            else
            {
                block();
            }
        }

    }

    private class GiveUpToDateEventInfo extends CyclicBehaviour
    {

        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.UpdateEventInfoAck);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                Envelope enve=reply.getEnvelope();
                Property p=(Property)enve.getAllProperties().next();
                Event ev=(Event)p.getValue();
                knownEvent=new Event(ev);

                // *** My Handling Algorithm ***
                //    * An Greedy Algorithm * 
                Vector<AgentInfo> agz=new Vector<AgentInfo>();
                for(int i=0;i<agsInfo.size();i++)
                {
                    agz.add(agsInfo.elementAt(i));
                }
                Vector<AgentInfo> usefull=new Vector<AgentInfo>();
                ACLMessage ms=new ACLMessage(ACLMessage.REQUEST_WHEN);
                ms.setContent("\nNow "+getName()+" Makes A Decision In "+myGroup.name+" ::\n");
                ms.addReceiver(MainAgentID);
                myAgent.send(ms);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }

                while(true)
                {
                    long max=getMax(knownEvent.intelligenceNeeded, knownEvent.speedNeeded, knownEvent.staminaNeeded, knownEvent.strengthNeeded);
                    if(max==knownEvent.intelligenceNeeded)
                    {
                        AgentInfo ai=getAgWithMostIntelligence(agz);
                        knownEvent.intelligenceNeeded-=ai.intelligence;
                        knownEvent.speedNeeded-=ai.speed;
                        knownEvent.staminaNeeded-=ai.stamina;
                        knownEvent.strengthNeeded-=ai.strength;
                        usefull.add(ai);
                        agz.remove(ai);
                    }
                    else if(max==knownEvent.speedNeeded)
                    {
                        AgentInfo ai=getAgWithMostSpeed(agz);
                        knownEvent.intelligenceNeeded-=ai.intelligence;
                        knownEvent.speedNeeded-=ai.speed;
                        knownEvent.staminaNeeded-=ai.stamina;
                        knownEvent.strengthNeeded-=ai.strength;
                        usefull.add(ai);
                        agz.remove(ai);
                    }
                    else if(max==knownEvent.staminaNeeded)
                    {
                        AgentInfo ai=getAgWithMostStamina(agz);
                        knownEvent.intelligenceNeeded-=ai.intelligence;
                        knownEvent.speedNeeded-=ai.speed;
                        knownEvent.staminaNeeded-=ai.stamina;
                        knownEvent.strengthNeeded-=ai.strength;
                        usefull.add(ai);
                        agz.remove(ai);
                    }
                    else         // (max==knownEvent.strengthNeeded)
                    {
                        AgentInfo ai=getAgWithMostStrength(agz);
                        knownEvent.intelligenceNeeded-=ai.intelligence;
                        knownEvent.speedNeeded-=ai.speed;
                        knownEvent.staminaNeeded-=ai.stamina;
                        knownEvent.strengthNeeded-=ai.strength;
                        usefull.add(ai);
                        agz.remove(ai);
                    }
                    if(knownEvent.intelligenceNeeded<0) knownEvent.intelligenceNeeded=0;
                    if(knownEvent.speedNeeded<0) knownEvent.speedNeeded=0;
                    if(knownEvent.staminaNeeded<0) knownEvent.staminaNeeded=0;
                    if(knownEvent.strengthNeeded<0) knownEvent.strengthNeeded=0;
                    if(knownEvent.intelligenceNeeded==0 && knownEvent.speedNeeded==0 && knownEvent.staminaNeeded==0 && knownEvent.strengthNeeded==0) break;
                    if(agz.size()==0) break;
                }
                //OK this group made it well and made a list of best agents for handling this event

                //for show its log on main frame
                ACLMessage mesg=new ACLMessage(ACLMessage.REQUEST_WHEN);
                String contmesg="\nFor Handling "+knownEvent.name+" These Agents Were Sent By Command Of Their Supervisiors =>  \n";
                mesg.addReceiver(MainAgentID);

                //now send them :  name of event for example  Event1
                ACLMessage msg=new ACLMessage(ACLMessage.QUERY_IF);
                msg.setContent(knownEvent.eventname);
                for(int i=0;i<usefull.size();i++)
                {
                    AID agid=usefull.elementAt(i).agentID;
                    contmesg+=agid.getName()+"\n";
                    msg.addReceiver(agid);
                    if(showCommunication)
                    {
                        ACLMessage mes=new ACLMessage(MyPerformatives.CommunicationLine);
                        mes.addReceiver(MainAgentID);
                        mes.setContent(getAID().getName()+","+agid.getName());
                        myAgent.send(mes);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent("1");
                            myAgent.send(tell);
                        }
                    }
                }
                myAgent.send(msg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent(String.valueOf(usefull.size()));
                    myAgent.send(tell);
                }
                mesg.setContent(contmesg);
                myAgent.send(mesg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
                if(knownEvent.intelligenceNeeded>0 || knownEvent.speedNeeded>0 || knownEvent.staminaNeeded>0 || knownEvent.strengthNeeded>0)
                {
                    told2MainAgent=true;
                    System.out.println("\nfailure to complete the handling of event in this group members ...\n\n");
                    // must send help signal to other adjacent groups
                    ACLMessage mssg=new ACLMessage(ACLMessage.NOT_UNDERSTOOD);       // this group can't handle completely
                    mssg.setContent(myGroup.name);
                    mssg.addReceiver(MainAgentID);
                    Envelope env=new Envelope();
                    if(knownEvent.groupsWhoHelp.contains(myGroup.name)==false)
                    {
                        knownEvent.groupsWhoHelp.add(myGroup.name);
                    }
                    env.addProperties(new Property("NowEvent", knownEvent));
                    mssg.setEnvelope(env);
                    myAgent.send(mssg);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }
                else
                {
                    told2MainAgent=false;   // it was false but for being sure ;)
                    numberOfUsefullAgents=usefull.size();
                }
                agsInfo.clear();
                ACLMessage message=new ACLMessage(ACLMessage.PROXY);
                message.addReceiver(MainAgentID);
                /*if(knownEvent.intelligenceNeeded>0 || knownEvent.speedNeeded>0 || knownEvent.staminaNeeded!=0 || knownEvent.strengthNeeded>0)
                {
                    message.setContent("-"+String.valueOf(usefull.size()));    // number of agents who handle this event and - it is completed
                }
                else
                {
                    message.setContent("+"+String.valueOf(usefull.size()));    // number of agents who handle this event and + it means it continues and it is not completed
                }*/
                int ind=knownEvent.eventname.indexOf('t');
                int eventNum=0;
                try
                {
                    eventNum=Integer.parseInt(knownEvent.eventname.substring(ind+1));
                }
                catch(Exception ex)
                {
                    JOptionPane.showMessageDialog(agFrame, "ERROR Occured In Parsing Event Name ...","ERROR",JOptionPane.ERROR_MESSAGE);
                }
                message.setContent(String.valueOf(eventNum-1)+","+String.valueOf(usefull.size()));
                myAgent.send(message);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }
    }

    private class SendForHandling extends CyclicBehaviour
    {
        
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                //this agent understood that he/she must go to handle an event
                String str=getAID().getName();            // << check here for event >>
                int index=str.indexOf("@");
                String name=str.substring(index);
                name=reply.getContent()+name;
                AID evag=new AID(name, AID.ISGUID);
                ACLMessage msg=new ACLMessage(ACLMessage.QUERY_REF);
                msg.addReceiver(evag);
                AgentCapability ac=new AgentCapability(strength, stamina, speed, intelligence);
                Envelope ev=new Envelope();
                ev.addProperties(new Property("AgentCapablities", ac));
                msg.setEnvelope(ev);
                myAgent.send(msg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class GiveNewCapabilities extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.NewCapabilitiesOfMyAgent);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                // updating capabilities
                Envelope env=reply.getEnvelope();
                Property p=null;
                try
                {
                    p=(Property)env.getAllProperties().next();
                }
                catch(Exception exc)
                {
                    System.out.println(exc.getLocalizedMessage());
                }
                AgentCapability ac=null;
                try
                {
                    ac=(AgentCapability)p.getValue();
                }
                catch(Exception exce)
                {
                    System.out.println("#1#\n"+exce.getLocalizedMessage());
                    /*ACLMessage mss=new ACLMessage(MyPerformatives.FinishedHandling);
                    mss.setContent("I Finished My Job On Known Event Now");
                    mss.addReceiver(myGroup.supervisor.agentID);
                    myAgent.send(mss);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }*/
                    return;
                }
                intelligence=(ac.intelligence>=100) ? 100 : ac.intelligence;
                stamina=(ac.stamina>=100) ? 100 : ac.stamina;
                speed=(ac.speed<=0) ? 0 : ac.speed;
                strength=(ac.strength<=0) ? 0 : ac.strength;
                System.out.println(getName()+" Updated");
                numberOfFacedEvents++;
                nameOfFacedEvents+=reply.getSender().getName()+"\n";
                // updating the agent frame =>
                agFrame.jLabel5.setText(String.valueOf(strength));
                agFrame.jProgressBar1.setValue(strength);
                agFrame.jLabel6.setText(String.valueOf(stamina));
                agFrame.jProgressBar2.setValue(stamina);
                agFrame.jLabel8.setText(String.valueOf(speed));
                agFrame.jProgressBar4.setValue(speed);
                agFrame.jLabel7.setText(String.valueOf(intelligence));
                agFrame.jProgressBar3.setValue(intelligence);
                agFrame.jLabel18.setText(String.valueOf(numberOfFacedEvents));
                agFrame.jTextArea1.setText(nameOfFacedEvents);
                // it doesn't need to update x and y because they don't change up to now
                //agFrame.jLabel11.setText(String.valueOf(x));
                //agFrame.jLabel12.setText(String.valueOf(y));
                ACLMessage message=new ACLMessage(MyPerformatives.FinishedHandling);
                message.setContent("I Finished My Job On Known Event Now");
                message.addReceiver(myGroup.supervisor.agentID);
                myAgent.send(message);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class GiveFinishedSignal extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.FinishedHandling);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                String name=reply.getSender().getName();
                if(agsFinished==null)
                {
                    agsFinished=new Vector<String>();
                }
                if(agsFinished.contains(name)==false)
                {
                    agsFinished.add(name);
                }
                if(told2MainAgent)
                {
                    if(agsFinished.size()==myGroup.ags.size())   // ok all of agents of my group sent that they finished their jobs on this event
                    {
                        agsFinished.clear();    // for next times
                        ACLMessage msg=new ACLMessage(MyPerformatives.TellNextIf2Conditions);
                        msg.setContent("1-All Of Agents Of My Group Has Finished Their Job On This Event");
                        msg.addReceiver(getAID());
                        myAgent.send(msg);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent("1");
                            myAgent.send(tell);
                        }
                    }
                }
                else    // not told2MainAgent
                {
                    if(agsFinished.size()==numberOfUsefullAgents)   // ok all of agents of my group sent that they finished their jobs on this event
                    {
                        agsFinished.clear();    // for next times
                        ACLMessage msg=new ACLMessage(MyPerformatives.TellNextIf2Conditions);
                        msg.setContent("1-My Usefull Agents Of My Group Has Finished Their Job On This Event");
                        msg.addReceiver(getAID());
                        myAgent.send(msg);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent("1");
                            myAgent.send(tell);
                        }

                        String ss=MainAgentID.getName();
                        int idx=ss.indexOf("@");
                        String tl=ss.substring(idx);
                        String eventName=knownEvent.eventname+tl;
                        AID evenId=new AID(eventName, AID.ISGUID);
                        ACLMessage upd=new ACLMessage(MyPerformatives.UpdateEventInfoAfterHandling);
                        upd.addReceiver(evenId);
                        upd.setContent("Tell Me Your Updated Capabilities");
                        myAgent.send(upd);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent("1");
                            myAgent.send(tell);
                        }
                    }
                }
            }
            else
            {
                block();
            }
        }
    
    }

    private class GiveUpdatedInfoAfterHandling extends CyclicBehaviour
    {

        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.UpdateEventInfoAfterHandlingAck);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                Envelope enve=reply.getEnvelope();
                Property p=(Property)enve.getAllProperties().next();
                Event ev=(Event)p.getValue();
                knownEvent=new Event(ev);

                if(ev.intelligenceNeeded>0 || ev.speedNeeded>0 || ev.staminaNeeded>0 || ev.strengthNeeded>0)
                {
                    told2MainAgent=true;
                    System.out.println("\nfailure to complete the handling of event in this group members ...\n\n");
                    // must send help signal to other adjacent groups
                    ACLMessage mssg=new ACLMessage(ACLMessage.NOT_UNDERSTOOD);       // this group can't handle completely
                    mssg.setContent(myGroup.name);
                    mssg.addReceiver(MainAgentID);
                    Envelope env=new Envelope();
                    if(knownEvent.groupsWhoHelp.contains(myGroup.name)==false)
                    {
                        knownEvent.groupsWhoHelp.add(myGroup.name);
                    }
                    env.addProperties(new Property("NowEvent", knownEvent));
                    mssg.setEnvelope(env);
                    myAgent.send(mssg);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }
                else
                {
                    ACLMessage msg=new ACLMessage(MyPerformatives.EventYouHandled);
                    String ss=MainAgentID.getName();
                    int idx=ss.indexOf("@");
                    String tl=ss.substring(idx);
                    String eventName=knownEvent.eventname+tl;
                    AID eventId=new AID(eventName, AID.ISGUID);
                    msg.addReceiver(eventId);
                    msg.setContent("You Handled , So TakeDown Now ...");
                    myAgent.send(msg);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }
            }
            else
            {
                block();
            }
        }
        
    }

    private class GiveSignalNoConcern extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHENEVER);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                isConcernedAbout=false;
                told2MainAgent=false;

                ACLMessage msg=reply.createReply();
                msg.setPerformative(MyPerformatives.NoConcernAck);
                msg.setContent("OK I Am Not Concerned About This Event Any More");
                myAgent.send(msg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class SendHelpSignalToAdjacent extends CyclicBehaviour
    {

        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.AGREE);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                Envelope envelope=reply.getEnvelope();
                Property p=null;
                try
                {
                    p=(Property)envelope.getAllProperties().next();
                }
                catch(Exception exce)
                {
                    System.out.println("#2#\n"+exce.getLocalizedMessage());
                    return;
                }
                knownEvent=new Event((Event)p.getValue());
                nextSupervisorName=reply.getContent();
                envNextSupervisor=(Envelope)envelope.clone();
                ACLMessage msg=new ACLMessage(MyPerformatives.TellNextIf2Conditions);
                msg.setContent("2-Main Sent Infos About Next Supervisor And Event now");
                msg.addReceiver(getAID());
                myAgent.send(msg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }
        
    }

    private class SendToNextSupervisor extends CyclicBehaviour
    {
        private boolean allfinished=false;
        private boolean mainsent=false;

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.TellNextIf2Conditions);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(reply.getContent().charAt(0)=='1')
                {
                    allfinished=true;
                }
                if(reply.getContent().charAt(0)=='2')
                {
                    mainsent=true;
                }
                if(allfinished==true && mainsent==true)   // ok 2 conditions satisfied now
                {
                    allfinished=mainsent=false;  // for next times
                    AID id=new AID(nextSupervisorName, AID.ISGUID);
                    ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);       // for purpose of that this is going to be send to Supervisor for handling like every events with this difference that it is reduced with last group
                    msg.addReceiver(id);
                    msg.setEnvelope(envNextSupervisor);
                    msg.setContent("Please Help Us In Handling The Event");
                    myAgent.send(msg);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                    String str="Now "+getName()+" The Supervisor Of "+myGroup.name+" Sent A Help Message To "+nextSupervisorName+" For Helping Them In Handling The "+knownEvent.eventname+" Now.";
                    System.out.println(str);
                    ACLMessage message=new ACLMessage(ACLMessage.REQUEST_WHEN);
                    message.addReceiver(MainAgentID);
                    message.setContent(str);
                    myAgent.send(message);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                    if(showCommunication)
                    {
                        ACLMessage mes=new ACLMessage(MyPerformatives.CommunicationLine);
                        mes.addReceiver(MainAgentID);
                        mes.setContent(getAID().getName()+","+id.getName());
                        myAgent.send(mes);
                        if(tell2CounterAgent)
                        {
                            ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                            tell.addReceiver(counterAgentID);
                            tell.setContent("1");
                            myAgent.send(tell);
                        }
                    }
                }
            }
            else
            {
                block();
            }
        }

    }

    private class GiveKillSignal extends CyclicBehaviour
    {
        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.KillMyAgent);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                takeDown();
            }
            else
            {
                block();
            }
        }
        
    }

    private class GiveShowCommunication extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.ShowCommunication);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                String content=reply.getContent();
                if(content.compareTo("1")==0)   // true
                {
                    showCommunication=true;
                }
                else
                {
                    showCommunication=false;
                }
            }
            else
            {
                block();
            }
        }

    }
}
