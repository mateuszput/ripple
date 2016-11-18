import java.util.LinkedList;
import java.util.List;

/**
 * Created by Multivac on 2016-11-17.
 */
public class Event {
    public LinkedList<Message> messages;

    public Event()
    {
        messages = new LinkedList<Message>();
    }

    Message addMessage(final Message message)
    {
        this.messages.add(message);
        return messages.getFirst();
    }
}
