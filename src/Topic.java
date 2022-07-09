import java.io.Serializable;
import java.util.ArrayList;

public class Topic implements Serializable
{
    public int brokerPort;
    public int brokerID;
    private String id;
    private String name;
    public ArrayList<String> subscribers = new ArrayList<>();
    public ArrayList<Chunk> history = new ArrayList<>();

    Topic(String name){this.name = name;}

    public void saveMessage(Chunk c)
    {
        history.add(c);
    }

    public void addSub(String user)
    {
        subscribers.add(user);
    }

    public void removeSub(String user)
    {
        subscribers.remove(user);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public ArrayList<String> getSubscribers() {
        return subscribers;
    }

    public ArrayList<Chunk> getHistory() {
        return history;
    }

    public String getName() {
        return name;
    }

}