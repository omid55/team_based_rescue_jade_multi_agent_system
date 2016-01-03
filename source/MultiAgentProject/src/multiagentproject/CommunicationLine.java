/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multiagentproject;

/**
 *
 * @author omid
 */
public class CommunicationLine
{
    double x1;
    double y1;
    double x2;
    double y2;

    public CommunicationLine()
    {
    }

    public CommunicationLine(double x1, double y1, double x2, double y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public CommunicationLine(Point p1,Point p2)
    {
        this.x1 = p1.x;
        this.y1 = p1.y;
        this.x2 = p2.x;
        this.y2 = p2.y;
    }
}
