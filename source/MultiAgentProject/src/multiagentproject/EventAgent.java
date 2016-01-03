/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author omid
 */
public class EventAgent extends jade.core.Agent
{
    private Event ev;
    private boolean handled=false;
    private float incrementCoefficient;
    private float decrementCoefficient;
    private boolean showCommunication;
    private AID MainAgentID;
    private boolean isKilled=false;

    public boolean tell2CounterAgent=false;
    public AID counterAgentID;
    private int time;
    private int incrementPercentOfEvent;
    private Timer timer;

    @Override
    protected void setup()
    {
        Object[] args=getArguments();
        ev=(Event)args[0];
        showCommunication=(Boolean)args[1];
        MainAgentID=(AID)args[2];
        tell2CounterAgent=(Boolean)args[3];
        counterAgentID=(AID)args[4];
        time=(Integer)args[5];
        incrementPercentOfEvent=(Integer)args[6];

        ActionListener al=new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                applyIncrement();
            }
        };
        timer=new Timer(time, al);
        timer.setRepeats(true);
        timer.start();

        ContentManager manager=getContentManager();
        Codec lang=new SLCodec();
        manager.registerLanguage(lang);
        DFAgentDescription desc=new DFAgentDescription();
        desc.addLanguages(lang.getName());
        desc.setName(getAID());
        ServiceDescription sd=new ServiceDescription();
        sd.addLanguages(lang.getName());
        sd.setName(getLocalName());
        sd.setType("Event-Agent");
        desc.addServices(sd);
        try
        {
            DFService.register(this, desc);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
            doDelete();
            return ;
        }

        addBehaviour(new GiveHandlingSignal());
        addBehaviour(new FailedInHandling());
        addBehaviour(new GiveIncDecPercent());
        addBehaviour(new SendUpdatedInfo());
        addBehaviour(new SendUpdatedInfoAfterHandling());
        addBehaviour(new YouHandled());
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
            JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR In Shutting Down Event Agent",JOptionPane.ERROR_MESSAGE);
        }
        if(showCommunication)
        {
            ACLMessage mes=new ACLMessage(MyPerformatives.ChangeColorCommunicationLines);
            mes.setContent("Change The Color With Insert Default Points To Vector");
            mes.addReceiver(MainAgentID);
            this.send(mes);
            if(tell2CounterAgent)
            {
                ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                tell.addReceiver(counterAgentID);
                tell.setContent("1");
                this.send(tell);
            }
        }
        //super.takeDown();
        MyAgent.killAgent(this, getAID());
        System.out.println("Event Agent Of "+ev.name+" Terminated Now ...");
    }

    private void applyIncrement()
    {
        if(isKilled || (ev.intelligenceNeeded<=0 && ev.speedNeeded<=0 && ev.staminaNeeded<=0 && ev.strengthNeeded<=0))
        {
            timer.stop();
            return;
        }
        ev.intelligenceNeeded+=ev.intelligenceNeeded*((double)incrementPercentOfEvent/100.0);
        ev.speedNeeded+=ev.speedNeeded*((double)incrementPercentOfEvent/100.0);
        ev.staminaNeeded+=ev.staminaNeeded*((double)incrementPercentOfEvent/100.0);
        ev.strengthNeeded+=ev.strengthNeeded*((double)incrementPercentOfEvent/100.0);
        System.out.println(ev.eventname+"=>");
        System.out.println(ev.intelligenceNeeded);
        System.out.println(ev.speedNeeded);
        System.out.println(ev.staminaNeeded);
        System.out.println(ev.strengthNeeded+"\n\n");
    }

    private boolean handleThisEvent(AgentCapability ac)     // this return event is finished or no
    {
        if((ev.intelligenceNeeded<=0 && ev.speedNeeded<=0 && ev.staminaNeeded<=0 && ev.strengthNeeded<=0)) return true;  // because it had been handled
        ev.intelligenceNeeded-=ac.intelligence;
        ev.speedNeeded-=ac.speed;
        ev.staminaNeeded-=ac.stamina;
        ev.strengthNeeded-=ac.strength;
        float r=ac.intelligence*incrementCoefficient;
        ac.intelligence+=r;        //  improvement
        r=ac.stamina*incrementCoefficient;
        ac.stamina+=r;             //  improvement
        r=ac.speed*decrementCoefficient;
        ac.speed-=r;               //  reduce
        r=ac.strength*decrementCoefficient;
        ac.strength-=r;            //  reduce
        if(ac.intelligence>100) ac.intelligence=100;
        if(ac.stamina>100) ac.stamina=100;
        if(ac.speed<0) ac.speed=0;
        if(ac.strength<0) ac.strength=0;
        if(ev.intelligenceNeeded<0) ev.intelligenceNeeded=0;
        if(ev.speedNeeded<0) ev.speedNeeded=0;
        if(ev.strengthNeeded<0) ev.strengthNeeded=0;
        if(ev.staminaNeeded<0) ev.staminaNeeded=0;
        return (ev.intelligenceNeeded<=0 && ev.speedNeeded<=0 && ev.staminaNeeded<=0 && ev.strengthNeeded<=0);
    }

    private class GiveIncDecPercent extends Behaviour
    {
        private boolean received=false;
       
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.IncDecCoefficient);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                String incDec=reply.getContent();
                int l=0;
                while(incDec.charAt(l)!=',')
                {
                    l++;
                }
                int inc=Integer.parseInt(incDec.subSequence(0, l).toString());
                int dec=Integer.parseInt(incDec.subSequence(l+1, incDec.length()).toString());
                incrementCoefficient=(float)inc/100;
                decrementCoefficient=(float)dec/100;
                received=true;
                ACLMessage ack=new ACLMessage(MyPerformatives.IncDecAcknowledge);
                ack.addReceiver(MainAgentID);
                ack.setContent("This is an ack for continue procedure checkAroundAgents.");
                myAgent.send(ack);
            }
            else
            {
                block();
            }
        }

        @Override
        public boolean done()
        {
            return received;
        }
        
    }

    private class GiveHandlingSignal extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                if(handled) return;
                Envelope env=reply.getEnvelope();
                Property pr=(Property)env.getAllProperties().next();
                AgentCapability ac=(AgentCapability)pr.getValue();
                // OK do it now with event it means now handling this
                boolean b=handleThisEvent(ac);
                if(b)
                {
                    handled=true;
                    // it means event is finished
                    //probablity 2
                    ACLMessage msg=new ACLMessage(ACLMessage.SUBSCRIBE);
                    msg.setContent(ev.eventname+" In Other Words '"+ev.name+"' Handled And Finished In ");
                    DFAgentDescription res[];
                    DFAgentDescription dfa=new DFAgentDescription();
                    ServiceDescription sd=new ServiceDescription();
                    sd.setType("Main-Agent");
                    dfa.addServices(sd);
                    AID id=null;
                    try
                    {
                        res = DFService.search(myAgent, dfa);
                        id=res[0].getName();
                    }
                    catch (FIPAException ex)
                    {
                        JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
                        block();
                    }
                    msg.addReceiver(id);
                    myAgent.send(msg);
                    if(tell2CounterAgent)
                    {
                        ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                        tell.addReceiver(counterAgentID);
                        tell.setContent("1");
                        myAgent.send(tell);
                    }
                    takeDown();
                }
                ACLMessage agentsreply=reply.createReply();
                agentsreply.setPerformative(MyPerformatives.NewCapabilitiesOfMyAgent);
                Envelope enve=new Envelope();
                enve.addProperties(new Property("NewCapabilities", ac));
                agentsreply.setEnvelope(enve);
                myAgent.send(agentsreply);
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

    private class YouHandled extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.EventYouHandled);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                ACLMessage msg=new ACLMessage(ACLMessage.SUBSCRIBE);
                msg.setContent(ev.eventname+" In Other Words '"+ev.name+"' Handled And Finished In ");
                DFAgentDescription res[];
                DFAgentDescription dfa=new DFAgentDescription();
                ServiceDescription sd=new ServiceDescription();
                sd.setType("Main-Agent");
                dfa.addServices(sd);
                AID id=null;
                try
                {
                    res = DFService.search(myAgent, dfa);
                    id=res[0].getName();
                }
                catch (FIPAException ex)
                {
                    JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
                    block();
                }
                msg.addReceiver(id);
                myAgent.send(msg);
                if(tell2CounterAgent)
                {
                    ACLMessage tell=new ACLMessage(MyPerformatives.Tell2MessageCounterAgent);
                    tell.addReceiver(counterAgentID);
                    tell.setContent("1");
                    myAgent.send(tell);
                }
                takeDown();
            }
            else
            {
                block();
            }
        }

    }

    private class FailedInHandling extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
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

    private class SendUpdatedInfo extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.UpdateEventInfo);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                Envelope env=new Envelope();
                env.addProperties(new Property("MyRequirements", ev));
                ACLMessage msg=new ACLMessage(MyPerformatives.UpdateEventInfoAck);
                msg.setEnvelope(env);
                msg.addReceiver(reply.getSender());     // like create reply ;)
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

    private class SendUpdatedInfoAfterHandling extends CyclicBehaviour    // after handling of one group that its supervisor wants to know this event handled or not
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.UpdateEventInfoAfterHandling);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                Envelope env=new Envelope();
                env.addProperties(new Property("MyRequirements", ev));
                ACLMessage msg=new ACLMessage(MyPerformatives.UpdateEventInfoAfterHandlingAck);
                msg.setEnvelope(env);
                msg.addReceiver(reply.getSender());     // like create reply ;)
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
}
