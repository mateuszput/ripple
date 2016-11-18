/**
 * Created by Multivac on 2016-11-17.
 */
public class Link
{
    public int toNode;
    public int totalLatency;
    public int lmSendTime;
    public int lmRecvTime;
    public Message lm;

    public Link(int t, int tl)
    {
        this.toNode = t;
        this.totalLatency = tl;
        this.lmSendTime = 0;
        this.lmRecvTime = 0;
        this.lm = null;
    }

}
