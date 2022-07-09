import java.util.ArrayList;

public class Story
{
    public String fileName;
    public String userName;
    public long time;
    public Story(String fileName, String userName,  ArrayList<Chunk> chunks)
    {
        this.fileName = fileName;
        this.userName = userName;
        this.time = System.currentTimeMillis();
        this.chunkArrayList = chunks;
    }
    public ArrayList<Chunk> chunkArrayList;
}