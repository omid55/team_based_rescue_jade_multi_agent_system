/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author omid
 */
public class MainAgent extends Agent
{
    private MainFrame frame;
    private int numberOfAgents;
    public int eventIndex=1;
    public int firstEventIndex=1;
    public Report rep;
    public long begintime;
    private boolean isKilled=false;

    public AID counterAgentID;

    @Override
    protected void setup()
    {
        frame=new MainFrame(this);
        frame.show();
        numberOfAgents=0;
        rep=new Report();

        ContentManager manager=getContentManager();
        Codec lang=new SLCodec();
        manager.registerLanguage(lang);
        DFAgentDescription desc=new DFAgentDescription();
        desc.setName(getAID());
        desc.addLanguages(lang.getName());
        ServiceDescription service=new ServiceDescription();
        service.addLanguages(lang.getName());
        service.setName(getLocalName());
        service.setType("Main-Agent");
        desc.addServices(service);
        try
        {
            DFService.register(this, desc);
        }
        catch(FIPAException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
            doDelete();
            return ;
        }
        addBehaviour(new GetCoordinates());
        addBehaviour(new NewsAboutMyAgents());
        addBehaviour(new EventHandled());
        addBehaviour(new SendAdjacentSuperVisorsID());
        addBehaviour(new RunNextEvent());
        addBehaviour(new GiveNumberOfAgentsWhoHandling());
        addBehaviour(new GiveSignalGotGroupsInfo());
        addBehaviour(new ReduceNumberOfAgents());
        addBehaviour(new TakeCommunicationLine());
        addBehaviour(new ChangeColorOfCommunicaionLines());
        addBehaviour(new GiveAcknowledgeIncDec());
        addBehaviour(new GiveAllNoConcernAcks());
        addBehaviour(new GiveWorkOnNowEventSignal());
    }

    public void setNumberOfAgents(int n)
    {
        numberOfAgents=n;
    }

    public int getNumberOfAgents()
    {
        return numberOfAgents;
    }

    public void createAgents()
    {
        PlatformController pc=getContainerController();
        AID mainAg=getAID();
        Object[] obj=new Object[4];
        obj[0]=mainAg;
        if(frame.jCheckBox2.isSelected())
        {
            obj[1]=true;
            try
            {
                String counterName="MessageCounterAgent";
                pc.createNewAgent(counterName, multiagentproject.CounterAgent.class.getName(), null).start();
                String mainName=getName();
                int index=mainName.indexOf('@');
                String tail=mainName.substring(index);
                counterName+=tail;
                counterAgentID=new AID(counterName, AID.ISGUID);
                obj[2]=counterAgentID;
            }
            catch (ControllerException ex)
            {
                JOptionPane.showMessageDialog(frame, "An Error While Creating Message Counter Agent Occured\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            obj[1]=false;
            obj[2]=null;
        }
        if(frame.myPointsFilePath!=null)
        {
            obj[3]=frame.myPointsFilePath;
        }
        else
        {
            obj[3]=null;
        }

        try
        {
            if(frame.isSimulation)
            {
                PriorityQueue<String> q=new PriorityQueue<String>();
                for(int i=0;i<numberOfAgents;i++) q.add("Agent"+(i+1));      // 1 to numberOfAgents
                while(q.isEmpty()==false)
                {
                    String agName=q.remove();
                    try
                    {
                        pc.createNewAgent(agName, MyAgent.class.getName(), obj).start();
                        frame.addText(agName+" Created Now In Simulation .");
                    }
                    catch(Exception exc)
                    {
                        q.add(agName);
                    }
                }
            }
            else
            {
                for(int i=0;i<numberOfAgents;i++)
                {
                    pc.createNewAgent("Agent"+(i+1), MyAgent.class.getName(), obj).start();
                    frame.addText("Agent"+(i+1)+" Created Now .");
                }
            }
        }
        catch(ControllerException any)
        {
            any.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An Error While Creating Agents Occured","ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createEventAgents(int inc,int dec)
    {
        PlatformController pc=getContainerController();
        try
        {
            int len=frame.events.size();
            for(int i=0;i<len;i++)
            {
                Event tmp=frame.events.elementAt(i);
                frame.addText("The Agent For Event "+tmp.name+" Which is Event "+(i+1)+" Created Now .");
                Object[] args=new Object[7];
                args[0]=tmp;
                args[1]=frame.jCheckBox1.isSelected();
                args[2]=getAID();
                if(frame.jCheckBox2.isSelected())
                {
                    args[3]=true;
                    args[4]=counterAgentID;
                }
                else
                {
                    args[3]=false;
                    args[4]=null;
                }
                args[5]=Integer.parseInt(frame.jTextField14.getText());
                args[6]=Integer.parseInt(frame.jTextField13.getText());
                pc.createNewAgent("Event"+(firstEventIndex++), EventAgent.class.getName(), args).start();
                String str=getAID().getName();
                int index=str.indexOf("@");
                String name=str.substring(index);
                name="Event"+(firstEventIndex-1)+name;
                AID evag=new AID(name, AID.ISGUID);
                ACLMessage msg=new ACLMessage(MyPerformatives.IncDecCoefficient);
                msg.addReceiver(evag);
                msg.setContent(inc+","+dec);
                frame.addText("Message About Increment And Decrement Percents Sent For"+tmp.name+" Which is Event "+(i+1)+" Now .");
                send(msg);
            }
        }
        catch(ControllerException any)
        {
            any.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An Error While Creating Events' Agents Occured","ERROR",JOptionPane.ERROR_MESSAGE);
        }
        if(firstEventIndex!=eventIndex)
        {
            JOptionPane.showMessageDialog(frame, "ERROR In Event Naming ...","ERROR",JOptionPane.ERROR_MESSAGE);
        }
        frame.checkAroundAgents();
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
            JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        frame.addText("Main Agent Terminated ...");
        System.out.println("Main Agent Terminated ...");
        frame.dispose();
        MyAgent.killAgent(this, getAID());
    }

    private class GetCoordinates extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.CoordinateInfo);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                frame.addText(reply.getSender().getName()+" Sent His Coordinates .");
                AID agentID=reply.getSender();
                String coor=reply.getContent();
                String[] res=coor.split(",");
                try
                {
                    double x=Double.parseDouble(res[0]);
                    double y=Double.parseDouble(res[1]);
                    int c1=Integer.parseInt(res[2]);
                    int c2=Integer.parseInt(res[3]);
                    int c3=Integer.parseInt(res[4]);
                    int c4=Integer.parseInt(res[5]);

                    //if(frame.coordinates.size()==numberOfAgents) frame.coordinates.clear();
                    frame.coordinates.add(new AgentPoint(x,y,agentID));
                    frame.initiallizedAgentsInfo.add(new AgentInfo(x,y,agentID,c1,c2,c3,c4));
                    if(frame.isSimulation && frame.coordinates.size()==numberOfAgents)  // it means all of them sent their coordinates
                    {
                        frame.doWholeSimulation(2);
                    }
                }
                catch(Exception exp)
                {
                    JOptionPane.showMessageDialog(null, reply.getSender().getName()+" Has Invalid Coordinates","ERROR",JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class NewsAboutMyAgents extends CyclicBehaviour
    {

        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                frame.addText(reply.getContent());
            }
            else
            {
                block();
            }
        }
        
    }

    private class EventHandled extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                //probablity 2
                if(frame.events.isEmpty()==false)
                {
                    frame.events.remove(0);
                }
                else
                {
                    System.err.println("frame.events size is 0 in EventHandled ...");
                }
                frame.addText("Event Handled Successfully ...\n");
                long endtime=System.currentTimeMillis();
                long duration=(endtime-begintime);
                String str=reply.getContent()+duration+" ms Successfully.";
                if(!frame.autoMode)
                {
                    JOptionPane.showMessageDialog(frame, str,"Success",JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    frame.addText("\nSuccess=> "+str+"\n");
                }
                frame.repev.elementAt(frame.repevindex).duration=duration;
                frame.repev.elementAt(frame.repevindex).succeed=true;
                frame.repevindex++;
                ACLMessage msg=new ACLMessage(ACLMessage.REQUEST_WHENEVER);         // Event Handled Don't Concern About Any Thing
                msg.setContent(frame.coordinates.lastElement().agentID.getName());    // aid of last agent because i want to run next event after that
                for(int i=0;i<frame.coordinates.size();i++)
                {
                    msg.addReceiver(frame.coordinates.elementAt(i).agentID);
                }
                myAgent.send(msg);
                rep.increaseSuccess();
                //frame.runNextEvent();
                if(frame.jCheckBox2.isSelected())
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent(String.valueOf(frame.coordinates.size()));
                    myAgent.send(tell);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class SendAdjacentSuperVisorsID extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                ACLMessage msg=reply.createReply();
                Envelope env=reply.getEnvelope();
                Property p=(Property)env.getAllProperties().next();
                Event evnt=(Event)p.getValue();
                env.clearAllProperties();
                frame.addText(reply.getSender().getName()+" Couldn't Handle The Event "+evnt.name+" So He/She Is Going To Send Help Signal To Their Adjacent Groups ...");
                String gname=reply.getContent();
                //msg.setEnvelope(env);     this is wrong
                String content="nothing";
                int l1=frame.groups.length;
                int l2=frame.groups[0].length;
                for(int i=0;i<l1;i++)
                {
                    for(int j=0;j<l2;j++)
                    {
                        if(frame.groups[i][j].name.compareToIgnoreCase(gname)==0)
                        {
                            if(i-1>=0 && frame.groupsWhoWorkOnNowEvent.contains(frame.groups[i-1][j].name)==false && frame.groups[i-1][j].supervisor.agentID!=null)
                            {
                                evnt.groupsWhoHelp.add(frame.groups[i-1][j].name);
                                //frame.groupsWhoWorkOnNowEvent.add(frame.groups[i-1][j].name);   because when a supervisor is going to make a decesion send a msg to mainAgent and add itself to this vector in this way
                                content=frame.groups[i-1][j].supervisor.agentID.getName();
                            }
                            else if(i+1<l1 && frame.groupsWhoWorkOnNowEvent.contains(frame.groups[i+1][j].name)==false && frame.groups[i+1][j].supervisor.agentID!=null)
                            {
                                evnt.groupsWhoHelp.add(frame.groups[i+1][j].name);
                                //frame.groupsWhoWorkOnNowEvent.add(frame.groups[i+1][j].name);
                                content=frame.groups[i+1][j].supervisor.agentID.getName();
                            }
                            else if(j-1>=0 && frame.groupsWhoWorkOnNowEvent.contains(frame.groups[i][j-1].name)==false && frame.groups[i][j-1].supervisor.agentID!=null)
                            {
                                evnt.groupsWhoHelp.add(frame.groups[i][j-1].name);
                                //frame.groupsWhoWorkOnNowEvent.add(frame.groups[i][j-1].name);
                                content=frame.groups[i][j-1].supervisor.agentID.getName();
                            }
                            else if(j+1<l2 && frame.groupsWhoWorkOnNowEvent.contains(frame.groups[i][j+1].name)==false && frame.groups[i][j+1].supervisor.agentID!=null)
                            {
                                evnt.groupsWhoHelp.add(frame.groups[i][j+1].name);
                                //frame.groupsWhoWorkOnNowEvent.add(frame.groups[i][j+1].name);
                                content=frame.groups[i][j+1].supervisor.agentID.getName();
                            }
                        }
                        if(content.compareToIgnoreCase("nothing")!=0)
                        {
                            break;
                        }
                    }
                    if(content.compareToIgnoreCase("nothing")!=0)
                    {
                        break;
                    }
                }
                if(content.compareToIgnoreCase("nothing")!=0)
                {
                    msg.setContent(content);
                    msg.setPerformative(ACLMessage.AGREE);
                    Envelope envel=new Envelope();
                    envel.addProperties(new Property("NowEvent", evnt));
                    msg.setEnvelope(envel);
                    myAgent.send(msg);
                    if(frame.jCheckBox2.isSelected())
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                }
                else
                {        
                    // it means agents can not handle it because its needed capabilities are too high to handle ...
                    //probablity 3
                    frame.addText("Failure In Handling Event '"+evnt.name+"' Or In Other Word "+evnt.eventname+" .");
                    long endtime=System.currentTimeMillis();
                    long duration=(endtime-begintime);
                    if(!frame.autoMode)
                    {
                        JOptionPane.showMessageDialog(frame, "Event "+evnt.name+" Can Not Be Handled Even By All Of Agents In The Environment And This Takes "+duration+" ms .","FAILURE",JOptionPane.INFORMATION_MESSAGE);
                    }
                    else
                    {
                        frame.addText("\nFAILURE=> Event "+evnt.name+" Can Not Be Handled Even By All Of Agents In The Environment And This Takes "+duration+" ms .\n");
                    }
                    frame.repev.elementAt(frame.repevindex).duration=duration;
                    frame.repev.elementAt(frame.repevindex).succeed=false;
                    frame.repevindex++;
                    frame.events.remove(0);
                    String str=getName();
                    int index=str.indexOf("@");
                    String name=str.substring(index);
                    name=evnt.eventname+name;
                    AID evag=new AID(name, AID.ISGUID);
                    frame.addText("Event "+evnt.name+" Or In Other Word '"+evnt.eventname+"' Failed And Terminated Automaticly ...");
                    ACLMessage failmsg=new ACLMessage(ACLMessage.CANCEL);
                    failmsg.setContent("Take Down The Event Agent");
                    failmsg.addReceiver(evag);
                    myAgent.send(failmsg);
                    if(frame.jCheckBox2.isSelected())
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                    ACLMessage msgNoConcern=new ACLMessage(ACLMessage.REQUEST_WHENEVER);   // It means Event Handled Don't Concern About Any Thing
                    msgNoConcern.setContent(frame.coordinates.lastElement().agentID.getName());    // aid of last agent because i want to run next event after that
                    for(int i=0;i<frame.coordinates.size();i++)
                    {
                        msgNoConcern.addReceiver(frame.coordinates.elementAt(i).agentID);
                    }
                    myAgent.send(msgNoConcern);
                    rep.increaseFailure();
                    //frame.runNextEvent();
                    if(frame.jCheckBox2.isSelected())
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent(String.valueOf(frame.coordinates.size()));
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

    private class GiveAllNoConcernAcks extends CyclicBehaviour
    {
        private Vector<String> agsSent;

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.NoConcernAck);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                String name=reply.getSender().getName();
                if(agsSent==null)
                {
                    agsSent=new Vector<String>();
                }
                if(agsSent.contains(name)==false)
                {
                    agsSent.add(name);
                }
                if(agsSent.size()==numberOfAgents)     // ok all of agents sent their acks after sending no concern signal
                {
                    agsSent.clear();
                    ACLMessage msg=new ACLMessage(ACLMessage.DISCONFIRM);
                    msg.addReceiver(getAID());
                    msg.setContent("Run Next Event Now");
                    myAgent.send(msg);
                    if(frame.jCheckBox2.isSelected())
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent(String.valueOf(frame.coordinates.size()));
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

    private class RunNextEvent extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                frame.runNextEvent();
            }
            else
            {
                block();
            }
        }

    }

    private class GiveNumberOfAgentsWhoHandling extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.PROXY);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                String str=reply.getContent();
                //int num=Integer.parseInt(str);
                //frame.repev.elementAt(frame.repevindex).numAgent+=num;
                int ind=str.indexOf(',');
                int evIndex=Integer.parseInt(str.substring(0, ind));
                int num=Integer.parseInt(str.substring(ind+1));
                frame.repev.elementAt(evIndex).numAgent+=num;
            }
            else
            {
                block();
            }
        }

    }

    private class GiveSignalGotGroupsInfo extends CyclicBehaviour
    {
        private Vector<String> agsGotGroupInfo;

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GotGroupInfo);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(agsGotGroupInfo==null)
                {
                    agsGotGroupInfo=new Vector<String>();
                }
                String name=reply.getSender().getName();
                if(agsGotGroupInfo.contains(name)==false)
                {
                    agsGotGroupInfo.add(name);
                }
                if(agsGotGroupInfo.size()==numberOfAgents)
                {
                    agsGotGroupInfo.clear();     // for next times
                    frame.doWholeSimulation(3);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class ReduceNumberOfAgents extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.ImGoingToBeKilled);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(frame.weHaveAgents==true)
                {
                    numberOfAgents--;
                }
                if(numberOfAgents==0 && frame.isSimulation==true)
                {
                    frame.weHaveAgents=false;
                    frame.doWholeSimulation(6);
                }
            }
            else
            {
                block();
            }
        }

    }

    private class TakeCommunicationLine extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.CommunicationLine);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                try
                {
                    String content=reply.getContent();
                    int index=content.indexOf(',');
                    String name1=content.substring(0, index);
                    String name2=content.substring(index+1);
                    AID a1=new AID(name1, AID.ISGUID);
                    AID a2=new AID(name2, AID.ISGUID);
                    Point p1=frame.agentLocation.get(a1);
                    Point p2=frame.agentLocation.get(a2);
                    CommunicationLine cl=new CommunicationLine(p1,p2);
                    frame.comLines.add(cl);
                }
                catch(Exception ex)
                {
                    return;
                }
            }
            else
            {
                block();
            }
        }

    }

    private class ChangeColorOfCommunicaionLines extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.ChangeColorCommunicationLines);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(frame.jCheckBox1.isSelected())
                {
                    frame.comLines.add(new CommunicationLine(-1, -1, -1, -1));
                }
            }
            else
            {
                block();
            }
        }

    }

    private class GiveAcknowledgeIncDec extends CyclicBehaviour       // and call the function for check around the agents
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.IncDecAcknowledge);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                System.err.println("From "+msg.getSender().getName()+" :  IncDecAcknowledge Received ...");
                //frame.checkAroundAgents();
            }
            else
            {
                block();
            }
        }
    }

    private class GiveWorkOnNowEventSignal extends CyclicBehaviour       // and call the function for check around the agents
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.MyGroupWorksOnNowEvent);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                String nameOfGroup=msg.getContent();
                if(frame.groupsWhoWorkOnNowEvent.contains(nameOfGroup)==false)
                {
                    frame.groupsWhoWorkOnNowEvent.add(nameOfGroup);
                }
            }
            else
            {
                block();
            }
        }
    }
}
