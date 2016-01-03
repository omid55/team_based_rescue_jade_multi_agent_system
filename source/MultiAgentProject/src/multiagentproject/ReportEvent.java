/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

/**
 *
 * @author omid
 */
public class ReportEvent
{
    public String name;         // event name for example FIRE
    public String eventName;    // for example Event1
    public long strengthNeeded;
    public long staminaNeeded;
    public long speedNeeded;
    public long intelligenceNeeded;
    public int numAgent;    // numberOfAgentsWorkedOnIt
    public long duration;
    public boolean succeed;

    public ReportEvent()
    {
        numAgent=0;
    }

    public ReportEvent(String name, String eventName, int strengthNeeded, int staminaNeeded, int speedNeeded, int intelligenceNeeded)
    {
        this.name = name;
        this.eventName = eventName;
        this.strengthNeeded = strengthNeeded;
        this.staminaNeeded = staminaNeeded;
        this.speedNeeded = speedNeeded;
        this.intelligenceNeeded = intelligenceNeeded;
        numAgent=0;
    }

    public ReportEvent(Event ev)
    {
        this.name = ev.name;
        this.eventName = ev.eventname;
        this.strengthNeeded = ev.strengthNeeded;
        this.staminaNeeded = ev.staminaNeeded;
        this.speedNeeded = ev.speedNeeded;
        this.intelligenceNeeded = ev.intelligenceNeeded;
        numAgent=0;
    }

    public ReportEvent(String name, String eventName, boolean succeed, int strengthNeeded, int staminaNeeded, int speedNeeded, int intelligenceNeeded, int numAgent, long duration)
    {
        this.name = name;
        this.eventName = eventName;
        this.succeed = succeed;
        this.strengthNeeded = strengthNeeded;
        this.staminaNeeded = staminaNeeded;
        this.speedNeeded = speedNeeded;
        this.intelligenceNeeded = intelligenceNeeded;
        this.numAgent = numAgent;
        this.duration = duration;
    }
    
    @Override
    public String toString()
    {
        return name+","+eventName+","+strengthNeeded+","+staminaNeeded+","+speedNeeded+","+intelligenceNeeded+","+numAgent+","+duration+","+succeed;
    }

}
