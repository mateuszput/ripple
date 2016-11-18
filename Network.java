import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Multivac on 2016-11-17.
 */
public class Network
{
    public int masterTime;
    public Map<Integer, Event> messages;

    public Network()
    {
        this.masterTime = 0;
        messages = new HashMap<Integer, Event>();
    }

    public void sendMessage(final Message message, final Link link, int sendTime)
    {
        if(message.toNode != link.toNode) return;
        link.lmSendTime = sendTime;
        link.lmRecvTime = sendTime + link.totalLatency;
        if(messages.get(link.lmRecvTime) == null)
        {
            messages.put(link.lmRecvTime, new Event());
        }
        link.lm = messages.get(link.lmRecvTime).addMessage(message); //check that
    }

}
