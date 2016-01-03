/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

/**
 *
 * @author omid
 */
public class Report
{
    private int success;
    private int failure;

    public Report()
    {
        success=failure=0;
    }

    public Report(int success, int failure)
    {
        this.success = success;
        this.failure = failure;
    }

    
    public int getFailure()
    {
        return failure;
    }

    public int getSuccess()
    {
        return success;
    }

    public void setFailure(int failure)
    {
        this.failure = failure;
    }

    public void setSuccess(int success)
    {
        this.success = success;
    }
    
    public void increaseSuccess()
    {
        success++;
    }

    public void increaseFailure()
    {
        failure++;
    }

    public double successPercent()
    {
        double n=failure+success;
        double sp=100*(double)success/n;
        return sp;
    }

    public double failurePercent()
    {
        double n=failure+success;
        double fp=100*(double)failure/n;
        return fp;
    }

    @Override
    public String toString()
    {
        double n=failure+success;
        double sp=100*(double)success/n;
        double fp=100*(double)failure/n;
        String res="\n\nSuccesses :  "+success+"\nFailures :  "+failure+"\n\nSuccess Percent :  "+sp+"\nFailure Percent :  "+fp+"\n\n";
        return res;
    }


}
