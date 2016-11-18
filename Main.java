import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by Multivac on 2016-11-17.
 */
public class Main {

    public static int nodesPositive = 0;
    public static int nodesNegative = 0;

    public static final int LEDGER_CONVERGE = 4;
    public static final int LEDGER_FORCE_CONVERGE = 7;
    public static final int AV_MIN_CONSENSUS = 50;
    public static final int AV_AVG_CONSENSUS = 60;
    public static final int AV_MAX_CONSENSUS = 70;

    public static final int NUM_NODES = 1000;
    public static final int NUM_MALICIOUS_NODES = 15;
    public static final int CONSENSUS_PERCENT = 80;

    public static final int MIN_E2C_LATENCY = 5;
    public static final int MAX_E2C_LATENCY = 50;
    public static final int MIN_C2C_LATENCY = 5;
    public static final int MAX_C2C_LATENCY = 200;

    public static final int NUM_OUTBOUND_LINKS = 10;

    public static final int UNL_MIN = 20;
    public static final int UNL_MAX = 30;
    public static final int UNL_THRESH = (UNL_MIN/2);

    public static final int BASE_DELAY = 1 ;

    public static final int SELF_WEIGHT = 1 ;

    public static final int PACKETS_ON_WIRE = 3 ;

    public static final Random random = new Random();

    //TODO: implement
    public static void main(String[] args) {
        Node[] nodes = new Node[NUM_NODES];
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String Id = "Transation from " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timestamp);
        System.out.println("Creating nodes");

        for(int i = 0; i < NUM_NODES; i++)
        {
            nodes[i] = new Node(i, NUM_NODES);
            nodes[i].e2cLatency = random.nextInt(MAX_E2C_LATENCY - MIN_E2C_LATENCY) + MIN_E2C_LATENCY;

            if (i%2 == 0)
            {
            	nodes[i].knowledge.set(i, (char) 1);
                nodes[i].nts.set(i, 1);
                ++Main.nodesPositive;
            }
            else
            {
            	nodes[i].knowledge.set(i, (char) -1);
                nodes[i].nts.set(i, 1);
                ++Main.nodesNegative;      
            }

            int unlCount = random.nextInt(UNL_MAX - UNL_MIN) + UNL_MIN;
            
            while(unlCount > 0)
            {
                int cn = random.nextInt(NUM_NODES - 1);
                if((cn != i && !nodes[i].isOnUNL(cn)))
                {
                    nodes[i].unl.add(cn);
                    --unlCount;
                }
            }
        }

        //create links
        System.out.println("Creating links");
        for(int i = 0; i < NUM_NODES; i++)
        {
            int links = NUM_OUTBOUND_LINKS;
            while(links > 0)
            {
                int lt = random.nextInt(NUM_NODES - 1);
                if((lt != i) && !nodes[i].hasLinkTo(lt))
                {
                    int ll = nodes[i].e2cLatency + nodes[lt].e2cLatency +
                            random.nextInt(MAX_C2C_LATENCY - MIN_C2C_LATENCY) + MIN_C2C_LATENCY;
                    nodes[i].links.add(new Link(lt, ll));
                    nodes[lt].links.add(new Link(i, ll));
                    --links;
                }
            }
        }

        Network network = new Network();

        System.out.println("Creating initial messages");
        for(int i = 0; i < NUM_NODES; i++)
        {
            for(Link l :nodes[i].links)
            {
                Message m = new Message(i, l.toNode);
                m.data.put(i, new NodeState(i, 1, nodes[i].knowledge.get(i)));
                network.sendMessage(m,l,0);
            }
        }
        System.out.println("Created " + network.messages.size() + " events");

        //do simulation
        do
        {
            if(Main.nodesPositive > (NUM_NODES * CONSENSUS_PERCENT/100.0)) break;
            if(Main.nodesNegative > (NUM_NODES * CONSENSUS_PERCENT/100.0)) break;
            Iterator<Map.Entry<Integer, Event>> it = network.messages.entrySet().iterator();
            if(it.hasNext() == false)
            {
                System.err.println("Fatal: Radio silence");
                System.exit(0);
            }
            Map.Entry<Integer, Event> entry = it.next();
            if(entry.getKey()/100.0 > network.masterTime/100.0)
                System.out.println("Time: " + entry.getKey() + "ms " + Main.nodesPositive + "/" + Main.nodesNegative);
            network.masterTime = entry.getKey();

            for(final Message m : entry.getValue().messages)
            {
                if(m.data.isEmpty())
                    --nodes[m.fromNode].messagesSent;
                else
                    nodes[m.toNode].receiveMessage(m, network);
            }
            network.messages.remove(entry.getKey());

        } while(true);

        int mc = 0;
        for (Map.Entry<Integer, Event> entry : network.messages.entrySet())
        {
            mc += entry.getValue().messages.size();
        }
        System.out.println("Consensus reached in " + network.masterTime + " ms with " + mc + " messages on the wire");

        long totalMessagesSend = 0;
        for(int i = 0; i < NUM_NODES; i++) totalMessagesSend += nodes[i].messagesSent;
        System.out.println("The average node sent " + totalMessagesSend / NUM_NODES + " messages");
        Jedis jedis = new Jedis("redis1.mapu.usermd.net", 9000);
        String result = (Main.nodesPositive > Main.nodesNegative) ? "positive":"negative";
        System.out.println("Connected to Redis");
        jedis.set(Id, result);
        String value = jedis.get(Id);
        System.out.println("Saved transation: "+Id+ " result: " + value);
       
        
    }
}
