
import java.io.Serializable;
import java.util.*;

public class Chunk implements Serializable
{
    private MultimediaFile mFile;
    public String username;

    public boolean moreChunks = false;
    public int fileID = 0;
    public long size;
    public String topic;
    public String type;
    public String command;
    public String str;

    protected HashMap<String, Topic> topics;

    public Chunk(String command, String username, String str)
    {
        this.command = command;
        this.type = "command";
        this.username = username;
        this.str = str;
    }

    public Chunk(HashMap<String, Topic> topics, String username, String str)
    {
        this.topics = new HashMap<>();
        this.topics.putAll(topics);
        this.type = "command";
        this.username = username;
        this.str = str;
    }

    public Chunk(MultimediaFile m, String topic)
    {
        this.mFile = m;
        this.type = "Multimedia";
        this.topic = topic;
    }

    public Chunk(MultimediaFile m, int id, long size, String topic)
    {
        this.mFile = m;
        this.username = m.getProfileName();
        this.fileID=id;
        this.size = size;
        this.type = "Multimedia";
        this.topic = topic;
    }

    MultimediaFile getMultimediaFile(){return mFile;}
    void setMoreChunks(boolean m){this.moreChunks = m;}
}