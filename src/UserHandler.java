import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.text.SimpleDateFormat;

public class UserHandler extends Thread {
    static Broker broker;
    Socket socket;
    String username;

    Topic activeTopic;
    boolean isInChat = false;

    ObjectInputStream in;
    ObjectOutputStream out;

    static public ArrayList<UserHandler> userHandlers = new ArrayList<>();
    private ArrayList<Chunk> storyChunks = new ArrayList<>();

    public UserHandler(Socket socket, Broker b)
    {
        this.socket = socket;
        broker = b;

        try {

            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            Chunk chunk = (Chunk) in.readObject();

            MultimediaFile username_Multimedia = chunk.getMultimediaFile();
            this.username = username_Multimedia.getProfileName();

            userHandlers.add(this);
            print(this.username + " connected to this broker", null);

        } catch (ClassNotFoundException m) {
            closeEverything();
        } catch (IOException e) {
            closeEverything();
            e.printStackTrace();
        }
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
                    if (chunk.command.equals("!topics"))
                    {
                        Chunk topicsChunk = new Chunk(broker.allTopics,"Broker " + broker.getID(), "!topicsHM");
                        sendBack(topicsChunk);
                    }
                    if (chunk.command.equals("!subscribe"))
                    {
                        broker.allTopics.get(chunk.str).addSub(chunk.username);
                        //this.activeTopic = null;

                        String entryMessage = this.username + " subscribed to " + broker.allTopics.get(chunk.str).getName();
                        print(entryMessage, null);
                    }
                    if (chunk.command.equals("!unsubscribe")) {
                        broker.allTopics.get(chunk.str).removeSub(chunk.username);

                        //this.activeTopic = null;

                        String exitMessage = this.username + " unsubscribed from " + broker.allTopics.get(chunk.str).getName();
                        print(exitMessage, null);
                    }
                    if(chunk.command.equals("!exitchat"))
                    {
                        if(isInChat)
                        print(chunk.username+" exited chat " + this.activeTopic, null);
                        this.activeTopic = null;
                        this.isInChat = false;
                    }
                    if(chunk.command.equals("!enter"))
                    {
                        this.activeTopic = broker.allTopics.get(chunk.str);
                        this.isInChat = true;

                        sendBack(new Chunk (new MultimediaFile("You entered "+ this.activeTopic.getName() + " chat","Broker", "text")
                                ,this.activeTopic.getName()));

                        String entryMessage = this.username + " joined " + broker.allTopics.get(chunk.str).getName();
                        print(entryMessage, null);
                        broadcastMessage(new Chunk(new MultimediaFile(entryMessage, "Broker ", "text")
                                , broker.allTopics.get(chunk.str).getName()));

                        sendHistory(broker.allTopics.get(this.activeTopic.getID()));
                    }
                    if(chunk.command.equals("!check"))
                    {
                        if (broker.myTopics.containsKey(broker.allTopics.get(chunk.str).getID()))
                        {
                            sendBack(new Chunk("", "Broker " + broker.getID(), "!stay"));
                            String message = chunk.username + " stays at this Broker " + broker.allTopics.get(chunk.str).brokerID
                                    + " (port: " + broker.allTopics.get(chunk.str).brokerPort + ")";
                            print(message, null);

                        }
                        if(chunk.command.equals("!getStories"))
                        {
                            print("fesfesf",null);
                            ArrayList<Story> stories = broker.storyManager.stories;

                            for(Story story : stories)
                            {

                                ArrayList<Chunk> chunks = story.chunkArrayList;
                                for(Chunk c : chunks){sendBack(c);}

                            }

                        }
                        else {
                            String message = chunk.username + " will change to Broker " + broker.allTopics.get(chunk.str).brokerID
                                    + " (port: " + broker.allTopics.get(chunk.str).brokerPort + ")";
                            print(message, null);

                            String portString = Integer.toString(broker.allTopics.get(chunk.str).brokerPort);
                            sendBack(new Chunk(portString, "Broker " + broker.getID(), "!change"));

                            closeEverything();
                            break;
                        }
                    }

                }
                else if(chunk.getMultimediaFile().getType().equals("story"))//////////////////////////////////
                {

                    storyChunks.add(chunk);

                    if(!chunk.moreChunks)//==false
                    {
                        boolean a = broker.storyManager.addStory(storyChunks);
                        if(a)
                        {
                            print(chunk.getMultimediaFile().getProfileName()+" uploaded a "+chunk.type
                                +" story in " + storyChunks.size()+ " chunks.", null);
                        }
                        storyChunks.clear();
                    }
                }
                else
                {
                    //this.activeTopic = broker.allTopics.get(chunk.topic);
                    //this.activeTopic.saveMessage(chunk);//save message in topic's history
                    saveMessage(chunk);
                    broadcastMessage(chunk);

                    MultimediaFile message = chunk.getMultimediaFile();

                    if (message.getType().equals("text"))
                    {
                        print(username + ": " + message.getContent(), broker.myTopics.get(chunk.topic).getName());

                    } else {
                        if(!chunk.moreChunks)
                        {
                            print(username + " sent a "+message.getType()+" "+message.getContent()
                                    +" into "+chunk.size +" chunks", broker.myTopics.get(chunk.topic).getName());

                        }
                    }
                }

            } catch (ClassNotFoundException m) {
                closeEverything();break;
            } catch (IOException e) {
                closeEverything();
                e.printStackTrace();break;
            } catch (ClassCastException c) {
                c.printStackTrace();break;
            }
        }
    }

    public void sendHistory(Topic topic)
    {
        ArrayList<Chunk> messages = topic.getHistory();

        for (Chunk message : messages)
        {
            sendBack(message);
        }
    }

    public synchronized void saveMessage(Chunk chunk)
    {
        this.activeTopic = broker.allTopics.get(chunk.topic);
        this.activeTopic.saveMessage(chunk);//save message in topic's history
    }

    public void broadcastMessage(Chunk message)
    {
        for (UserHandler handler : userHandlers)
        {
            try {
                if (!handler.username.equals(this.username) && handler.activeTopic!=null)
                {
                    if(handler.activeTopic.equals(this.activeTopic))
                    {
                        handler.out.writeObject(message);
                        handler.out.flush();
                    }
                }
            } catch (IOException e) {closeEverything();}
        }
    }

    public void sendBack(Chunk message)
    {
        try
        {
           this.out.writeObject(message);
           this.out.flush();

        } catch (IOException e) {closeEverything();}
    }

    private void print(String str, String topicName)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date currentDate = new Date();
        if(topicName!=null){System.out.println(formatter.format(currentDate) + " | " +str+ " | " + topicName);}
        else{System.out.println(formatter.format(currentDate) +" | " +str);}
    }

    public void closeEverything()
    {
        try
        {
            userHandlers.remove(this);
            if(out !=null){this.out.close();}
            if (in != null){this.in.close();}
            if(socket!=null){this.socket.close();}
        } catch (IOException ioException) { ioException.printStackTrace(); }
    }
}