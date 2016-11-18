import java.util.*;

/**
 * Created by Multivac on 2016-11-17.
 */
public class Node
{
    public int n;
    public int e2cLatency;

    public List<Integer> unl = new ArrayList<Integer>();
    public List<Link> links = new ArrayList<Link>();

    public List<Integer> nts;
    public List<Character> knowledge;

    public int messagesSent;
    public int messagesReceived;

    public Node(int nn, int mm)
    {
        nts = new ArrayList<Integer>(mm);
        knowledge = new ArrayList<Character>(mm);

        for (int i = 0; i < mm; i++) {
            nts.add(0);
            knowledge.add((char)0);
        }

        this.n = nn;
        this.messagesSent = 0;
        this.messagesReceived = 0;
    }

    public boolean isOnUNL(int j)
    {
        return unl.contains(j);
    }

    public boolean hasLinkTo(int j)
    {
        for(Link link : links)
        {
            if (link.toNode == j) return true;
        }
        return false;
    }

    public void receiveMessage(final Message m, Network network)
    {
        ++messagesReceived;

        // If we were going to send any of this data to that node, skip it
        for(Link link : links)
        {
            if((link.toNode == m.fromNode) && (link.lmSendTime >= network.masterTime))
            {
                link.lm.subPositions(m.data);
                break;
            }
        }

        HashMap<Integer, NodeState> changes = new HashMap<Integer, NodeState>();
        for(Map.Entry<Integer, NodeState> entry : m.data.entrySet())
        {
            if((entry.getKey() != n) && (knowledge.get(entry.getKey()) !=  entry.getValue().state) &&
                    entry.getValue().ts > nts.get(entry.getKey()))
            {
                // This gives us new information about a node
                knowledge.set(entry.getKey(), entry.getValue().state);
                nts.set(entry.getKey(), entry.getValue().ts);
                changes.put(entry.getKey(), entry.getValue());
            }
        }

        if(changes.isEmpty()) return; // nothing changed

        // 2) Choose our position change, if any
        int unlCount = 0;
        int unlBalance = 0;

        for(int node : unl)
        {
            if(knowledge.get(node) == (char)1)
            {
                unlCount++;
                unlBalance++;
            }
            if(knowledge.get(node) == (char)-1)
            {
                unlCount++;
                unlBalance--;
            }
        }
       
        if(n < Main.NUM_MALICIOUS_NODES)
            unlBalance = -unlBalance; // if we are a malicious node, be contrarian
        unlBalance -= (network.masterTime/250.0);
        boolean posChange = false;

        if(unlCount >= Main.UNL_THRESH)
        {
            if((knowledge.get(n) == (char)1) && (unlBalance <-Main.SELF_WEIGHT))
            {
                knowledge.set(n, (char)-1);
                --Main.nodesPositive;
                ++Main.nodesNegative;
                int tmp = nts.get(n); tmp++; nts.set(n, tmp);
                changes.put(n, new NodeState(n, nts.get(n), (char)-1));
                posChange = true;
            }
            else if((knowledge.get(n) == (char)-1) && (unlBalance > Main.SELF_WEIGHT))
            {
                knowledge.set(n, (char)1);
                ++Main.nodesPositive;
                --Main.nodesNegative;
                int tmp = nts.get(n); tmp++; nts.set(n, tmp);
                changes.put(n, new NodeState(n, nts.get(n), (char)1));
                posChange = true;
            }
        }

        // 3) Broadcast the message
        for(Link link : links)
        {
            if(posChange || (link.toNode != m.fromNode))
            {
                if(link.lmSendTime > network.masterTime) link.lm.addPositions(changes);
                else
                {
                    int sentTime = network.masterTime;
                    if(!posChange)
                    {
                        sentTime += 1; //BASE delay
                        if(link.lmRecvTime > sentTime) sentTime += (link.totalLatency / 3.0); //per packets on wire
                    }
                    network.sendMessage(new Message(n, link.toNode, changes), link, sentTime);
                    messagesSent++;
                }
            }
        }
    }
}
