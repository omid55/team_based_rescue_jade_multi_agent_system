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
public class AgentInfo
{
    public double x;
    public double y;

    public AID agentID;

    // AgentCapability =>
    public int strength;
    public int stamina;
    public int speed;
    public int intelligence;

    public AgentInfo()
    {
    }

    public AgentInfo(double x, double y, AID agentID, int strength, int stamina, int speed, int intelligence)
    {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.stamina = stamina;
        this.speed = speed;
        this.intelligence = intelligence;
        this.agentID=agentID;
    }

    @Override
    public String toString()
    {
        return x+","+y+","+strength+","+stamina+","+speed+","+intelligence;
    }


}
