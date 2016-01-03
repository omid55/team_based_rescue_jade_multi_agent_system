/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import java.util.Vector;

/**
 *
 * @author omid
 */
public class Group
{
    public String name;
    public Vector<AgentPoint> ags;
    public int x1;
    public int y1;
    public int x2;
    public int y2;
    public AgentPoint supervisor;

    public Group()
    {
        ags=new Vector<AgentPoint>();
        supervisor=new AgentPoint(-1, -1);  // it means null
    }

    public Group(String s)
    {
        ags=new Vector<AgentPoint>();
        name=s;
        supervisor=new AgentPoint(-1, -1);  // it means null
    }

    public Group(String s,int x1,int y1,int x2,int y2)
    {
        ags=new Vector<AgentPoint>();
        name=s;
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
        supervisor=new AgentPoint(-1, -1);  // it means null
    }

    private int getNumber(AgentPoint ap)
    {
        int ind=5;
        String name=ap.agentID.getName();
        for(;name.charAt(ind)!='@';ind++){}
        int number=Integer.parseInt(name.substring(5,ind));
        return number;
    }

    public void addAgentPoint(AgentPoint ap)
    {
        ags.add(ap);
        if(supervisor.x==-1)   // it means it is null now
        {
            supervisor.copy(ap);
        }
        else
        {
            int snum=getNumber(supervisor);
            int apnum=getNumber(ap);
            if(snum>apnum)     // it means new agent is older than supervisor
            {
                supervisor.copy(ap);
            }
        }
    }
}
