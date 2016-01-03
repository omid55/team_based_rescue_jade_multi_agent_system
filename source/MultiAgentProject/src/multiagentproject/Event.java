/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import jade.core.AID;
import java.util.Vector;

/**
 *
 * @author omid
 */
public class Event
{
    public String eventname;             // for example :  Event1
    public String name;                  // for example :  Fire
    public long strengthNeeded;
    public long staminaNeeded;
    public long speedNeeded;
    public long intelligenceNeeded;
    public double x;
    public double y;
    public Vector<AID> agentsWhoKnows;
    public Vector<String> groupsWhoHelp;

    public Event(double a,double b)
    {
        x=a;
        y=b;
        agentsWhoKnows=new Vector<AID>();
        groupsWhoHelp=new Vector<String>();
    }

    public Event()
    {
        agentsWhoKnows=new Vector<AID>();
        groupsWhoHelp=new Vector<String>();
    }

    public Event(Event ev)
    {
        this.eventname = ev.eventname;
        this.name = ev.name;
        this.strengthNeeded = ev.strengthNeeded;
        this.staminaNeeded = ev.staminaNeeded;
        this.speedNeeded = ev.speedNeeded;
        this.intelligenceNeeded = ev.intelligenceNeeded;
        this.x = ev.x;
        this.y = ev.y;
        agentsWhoKnows=new Vector<AID>();
        for(int i=0;i<ev.agentsWhoKnows.size();i++)
        {
            agentsWhoKnows.add(ev.agentsWhoKnows.elementAt(i));
        }
        groupsWhoHelp=new Vector<String>();
        for(int i=0;i<ev.groupsWhoHelp.size();i++)
        {
            groupsWhoHelp.add(ev.groupsWhoHelp.elementAt(i));
        }
    }

    public String toShortString()
    {
        return x+","+y;
    }

}
