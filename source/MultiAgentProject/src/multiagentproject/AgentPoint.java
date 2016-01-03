/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

import jade.core.AID;

/**
 *
 * @author omid
 */
public class AgentPoint
{
    public double x;
    public double y;
    public AID agentID;

    public AgentPoint(double a,double b)
    {
        x=a;
        y=b;
    }
    
    public AgentPoint(double a,double b,AID s)
    {
        x=a;
        y=b;
        agentID=s;
    }

    public void copy(AgentPoint ap)
    {
        x=ap.x;
        y=ap.y;
        agentID=ap.agentID;
    }

}
