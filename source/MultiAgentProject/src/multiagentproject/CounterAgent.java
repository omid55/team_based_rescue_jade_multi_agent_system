/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javax.swing.JOptionPane;
import jade.domain.JADEAgentManagement.KillAgent;
/**
 *
 * @author omid
 */
public class CounterAgent extends Agent
{
    private long count=0;
    private CounterAgentFrame frame;

    @Override
    protected void setup()
    {
        frame=new CounterAgentFrame();
        frame.show();

        DFAgentDescription dfd=new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd=new ServiceDescription();
        sd.setName("MyMessageCounterAgent");
        sd.setType("Counter-Agent");
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
            doDelete();
            return ;
        }

        addBehaviour(new GiveIncrementMessage());
        addBehaviour(new GiveKillSignal());
    }

    @Override
    protected void takeDown()
    {
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException ex)
        {
            //JOptionPane.showMessageDialog(null,ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }

        frame.dispose();
	System.out.println("Counter Agent With Name '"+getName()+"' terminated ...");
        killAgent(this);
    }

    public void killAgent(Agent ag)
    {
        try
        {
            KillAgent kill=new KillAgent();
            kill.setAgent(getAID());
            // create and send the message to the ams
            ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
            ag.getContentManager().registerOntology(JADEManagementOntology.getInstance(),JADEManagementOntology.NAME);
            msg.setOntology(JADEManagementOntology.NAME);
            ag.getContentManager().registerLanguage(new SLCodec());
            msg.setLanguage(ag.getContentManager().getLanguageNames()[0]);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            ag.getContentManager().fillContent(msg, new Action(ag.getAMS(), kill));
            msg.addReceiver(ag.getAMS());
            ag.send(msg);
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

    private class GiveIncrementMessage extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.Tell2MessageCounterAgent);
            ACLMessage reply=myAgent.receive(mt);
            if(reply!=null)
            {
                int inc=Integer.parseInt(reply.getContent());
                count+=inc;
                frame.jLabel3.setText(String.valueOf(count));
                frame.jLabel4.setText(String.valueOf(count*2));
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

}
