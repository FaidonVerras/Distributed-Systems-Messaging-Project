import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Consumer extends Thread
{
    private User user;
    private Socket socket = null;
    private ObjectInputStream in = null;

    private ArrayList<Chunk> chunkArrayList = new ArrayList<>();

    public Consumer(Socket socket, User user)
    {
        try
        {
            this.user = user;
            this.socket = socket;
            this.in = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {closeEverything();}
    }

    @Override
    public void run()
    {
        while (socket.isConnected())
        {
            try
            {
                Chunk chunk = (Chunk) in.readObject();

                if (chunk.type.equals("command"))
                {
                    if(chunk.str.equals("!topicsHM"))
                    {
                        user.allTopics.clear();
                        user.allTopics.putAll(chunk.topics);
                    }
                    else if(chunk.str.equals("!change"))
                    {
                        user.string = "!change";
                        int port = Integer.parseInt(chunk.command);
                        user.initialize(new Socket("127.0.0.1", port), true);
                        break;
                    }
                    else if(chunk.str.equals("!stay"))
                    {
                        user.string = "!stay";
                    }
                }
                else
                {
                    MultimediaFile message = chunk.getMultimediaFile();

                    if (message.getType().equals("text"))
                    {
                        System.out.println(message.getProfileName() + ": " + message.getContent());

                    }
                    else if(chunk.getMultimediaFile().getType().equals("story"))
                    {
                        System.out.println("received " + chunk.username + "'s story " + chunk.getMultimediaFile().getContent());

                        this.chunkArrayList.add(chunk);
                        if(!chunk.moreChunks)
                        {
                            constructFile("./userStories");//"downloads" file
                            chunkArrayList.clear();
                        }
                    }
                    else //if message is a file or file piece
                    {
                        this.chunkArrayList.add(chunk);
                        if(!chunk.moreChunks)
                        {
                            System.out.println(message.getProfileName()+" sent a "+chunk.type+" in " + chunkArrayList.size()+ " chunks." );
                            constructFile("./downloads/");//"downloads" file
                            chunkArrayList.clear();
                        }
                    }
                }
            }
            catch(ClassNotFoundException m){closeEverything();}
            catch (IOException e) {closeEverything(); e.printStackTrace();}
        }
    }

    private void constructFile(String path) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String fileName ="";

        for (Chunk chunk:this.chunkArrayList)
        {
            MultimediaFile mf = chunk.getMultimediaFile();
            byte[] b = mf.getPiece();
            os.write(b);

            if(fileName.equals("")){fileName = mf.getContent();}
        }

        byte[] bytes = os.toByteArray();

        FileOutputStream fos = new FileOutputStream(path + fileName);
        fos.write(bytes);
        fos.close();os.close();
    }

    public void closeEverything()
    {
        try
        {
            this.in.close();
            this.socket.close();
        } catch (IOException ioException) { ioException.printStackTrace(); }
    }
}