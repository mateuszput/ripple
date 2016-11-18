
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Multivac on 2016-11-17.
 */
public class Message
{
    public int fromNode;
    public int toNode;
    public Map<Integer, NodeState> data;

    public Message(int fromNode, int toNode)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
        data = new HashMap<Integer, NodeState>();
    }

    public Message(int fromNode, int toNode, final HashMap<Integer, NodeState> data)
    {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.data = data;
    }

    //TODO: implement
    public void addPositions(final Map<Integer, NodeState> map)
    {
        for (Map.Entry<Integer, NodeState> entry : map.entrySet())
        {
            int entry_key = entry.getKey();
            NodeState entry_value = entry.getValue();
            if (entry_key != toNode)
            {
                if(data.containsKey(entry_key))
                {
                    NodeState value = data.get(entry_key);
                    if (entry_value.ts > value.ts)
                    {
                        value.ts = entry_value.ts;
                        value.state = entry_value.state;
                    }
                }
                else{
                    data.put(entry_key, entry_value);
                }
            }

        }
    }
    //TODO: implement
    public void subPositions(final Map<Integer, NodeState> map)
    {
        // we received this information from this node, so no need to send it
        for(Iterator<Map.Entry<Integer, NodeState>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, NodeState> entry = it.next();
            int entry_key = entry.getKey();
            NodeState entry_value = entry.getValue();
            if (entry_key != toNode)
            {
                if(data.containsKey(entry_key))
                {
                    NodeState value = data.get(entry_key);
                    if (entry_value.ts > value.ts){
                        it.remove();
                    }
                }

            }
        }
    }

}
