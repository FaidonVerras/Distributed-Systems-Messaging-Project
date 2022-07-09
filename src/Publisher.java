import java.io.*;
import java.net.Socket;
import java.util.*;

public class Publisher extends Thread
{
    private final User user;
    private Socket socket = null;
    private ObjectOutputStream out = null;


    public Publisher(Socket socket, User user, boolean change) {

        this.user = user;

        try
        {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());

            push(new Chunk(new MultimediaFile(" ", user.username, "text")," "));
            //Sends the user's username when he connects to the topic

            if(change)
            {
                push(new Chunk("!enter", user.username, this.user.activeTopicId));
                this.user.isInChat = true;
            }
        } catch (IOException e) {closeEverything();}
    }

    @Override
    public void run()
    {
        while (socket.isConnected()) {

            Scanner scanner = new Scanner(System.in);

            if(!this.user.isInChat)
            {
                boolean b = menu();
                if(!b){break;}
            }

            if(this.user.isInChat)
            {
                String input = scanner.nextLine();

                if (!(input.equals(" ") || input.equals("")))
                {
                    if(input.equals("!menu"))
                    {
                        menu();
                    }
                    else
                    {
                        File file = new File(input);

                        if (file.isFile() && !file.isDirectory())//if message is a photo or video
                        {
                            String fileName = input.replaceAll("files/","");

                            List<byte[]> listOfPieces;

                            try
                            {
                                listOfPieces = getFileChunks(file);

                                for(int i = 0; i < listOfPieces.size(); i++)
                                {
                                    byte [] b = listOfPieces.get(i);

                                    String type;

                                    int length = fileName.length();
                                    String ext = fileName.substring(length-4);

                                    if(ext.equals(".png")||ext.equals(".jpg)")){type = "photo";}
                                    else if(ext.equals(".mp4")||ext.equals(".mov")||ext.equals(".mkv")){type = "video";}
                                    else{type = "file";}

                                    MultimediaFile multimediaFile = new MultimediaFile(fileName, b, this.user.username, type);

                                    Chunk chunk = new Chunk(multimediaFile, i+1, listOfPieces.size(), this.user.activeTopicId);
                                    chunk.setMoreChunks(b.length >= 512 * 1024);//true if >512KB, false if <512KB
                                    //moreChunks is a boolean field that indicates if this is the last file chunk

                                    push(chunk);
                                }
                                System.out.println("File sent into " + listOfPieces.size() + " chunks");
                            } catch (IOException e) {e.printStackTrace();}

                        } else//if message is text
                        {
                            push(new Chunk(new MultimediaFile(input, this.user.username, "text"), this.user.activeTopicId));
                        }
                    }
                }
            }
        }
    }

    private boolean menu()
    {
        this.user.isInChat = false;

        push(new Chunk("!exitchat", this.user.username, null));

        Scanner scanner = new Scanner(System.in);
        while(true)
        {
            System.out.println("---------------MENU------------------");
            System.out.println("1. Subscribe to topic");
            System.out.println("2. Unsubscribe from topic");
            System.out.println("3. Enter chat");
            System.out.println("4. Exit menu");
            System.out.println("5. Post a story");
            System.out.println("6. See stories");
            System.out.println("-------------------------------------");

            System.out.print("Enter choice: ");
            String c0 = scanner.nextLine();

            switch (c0)
            {
                case "1":

                    push(new Chunk("!topics", user.username, null));

                    try {
                        System.out.print("requesting topics.");
                        Thread.sleep(300);
                        System.out.print(".");
                        Thread.sleep(300);
                        System.out.println(".");
                        Thread.sleep(300);

                    } catch (InterruptedException e) {
                        closeEverything();
                    }
                    printTopics(this.user.allTopics,"All topics");

                    System.out.print("Select a topic to subscribe to: ");
                    String c1 = scanner.nextLine();

                    if (this.user.allTopics.containsKey(c1))
                    {
                        Topic chosenTopic = this.user.allTopics.get(c1);

                        if (!user.subscribedTopics.containsValue(chosenTopic)) {
                            addTopic(chosenTopic);
                        } else {
                            System.out.println("You are already subscribed to " + chosenTopic.getName());
                        }

                        push(new Chunk("!subscribe", user.username, c1));
                        return true;
                    }
                    break;

                case "2":
                    printTopics(this.user.subscribedTopics,"My topics");
                    System.out.print("Select a topic to unsubscribe from: ");
                    String c2 = scanner.nextLine();

                    Topic selectedTopic2;
                    try {
                        if (this.user.subscribedTopics.containsKey(c2))
                        {
                            selectedTopic2 = user.subscribedTopics.get(c2);
                            Thread.sleep(200);
                            System.out.println("You left " + selectedTopic2.getName());

                            push(new Chunk("!unsubscribe", user.username,c2));

                            this.user.isInChat = false;
                            user.subscribedTopics.remove(c2);
                            return true;
                        }
                    }catch (NullPointerException e)
                    {
                        System.out.println("Not a valid input");
                        break;
                    }catch (InterruptedException e) {
                        closeEverything();
                    }

                case "3":
                    printTopics(this.user.subscribedTopics,"My topics");
                    System.out.print("Select one of your topics: ");
                    String c3 = scanner.nextLine();

                    Topic selectedTopic3;
                    try {
                        if (this.user.subscribedTopics.containsKey(c3))
                        {
                            selectedTopic3 = user.subscribedTopics.get(c3);
                            this.user.activeTopicName = selectedTopic3.getName();
                            this.user.activeTopicId = c3;

                            push(new Chunk("!check", user.username,c3));

                            Thread.sleep(500);
                            if(user.string.equals("!stay"))
                            {
                                push(new Chunk("!enter", user.username,this.user.activeTopicId));
                                this.user.isInChat = true;
                                return true;
                            }
                            else if(user.string.equals("!change"))
                            {
                                return false;
                            }

                        }
                    }catch (NullPointerException e)
                    {
                        System.out.println("Not a valid input");
                        break;
                    }catch (InterruptedException e) {
                        closeEverything();
                    }

                case "4":
                    return true;

                case "5":

                    System.out.print("Enter your story's path: ");
                    String storyPath = scanner.nextLine();
                    File file = new File(storyPath);

                    if (file.isFile() && !file.isDirectory())//if message is a photo or video
                    {
                        String fileName = storyPath.replaceAll("files/","");
                        List<byte[]> listOfPieces;
                        try
                        {
                            listOfPieces = getFileChunks(file);

                            for(int i = 0; i < listOfPieces.size(); i++) {
                                byte[] b = listOfPieces.get(i);

                                MultimediaFile multimediaFile = new MultimediaFile(fileName, b, this.user.username, "story");

                                Chunk chunk = new Chunk(multimediaFile, i + 1, listOfPieces.size(), this.user.activeTopicId);
                                chunk.setMoreChunks(b.length >= 512 * 1024);//true if >512KB, false if <512KB
                                //moreChunks is a boolean field that indicates if this is the last file chunk

                                push(chunk);
                            }
                            System.out.println("Story uploaded into " + listOfPieces.size() + " chunks");
                        } catch (IOException e) {e.printStackTrace();}
                    }
                    else {
                        System.out.println("There is no such file");
                    }
                    return true;

                case "6":

                    push(new Chunk("!getStories", user.username,null));
                    System.out.println("Stories will be downloaded to your userStories folder");
                    return true;

                default:
                    System.out.println("Not a valid input");
                    continue;
            }
        }
    }

    private byte[] fileToBytes(File file) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[(int)file.length()];
        fis.read(byteArray);
        fis.close();
        return byteArray;
    }

    private List<byte[]> getFileChunks(File file) throws IOException
    {
        byte[] byteArray = fileToBytes(file);
        List<byte[]> pieces = new ArrayList<>();

        for (int i = 0; i < byteArray.length; )
        {
            int size = 512 * 1024;
            byte[] piece = new byte[Math.min(size, byteArray.length - i)];
            for (int j = 0; j < piece.length; j++, i++)
            {
                piece[j] = byteArray[i];
            }
            pieces.add(piece);
        }
        return pieces;
    }

    private void push(Chunk message)
    {
        try
        {
            out.writeObject(message);
            out.flush();

        } catch (IOException e) {closeEverything();}
    }

    public void addTopic(Topic topic)
    {
        this.user.subscribedTopics.put(topic.getID(), topic);
        System.out.println("Subscribed to: " + this.user.activeTopicName);
        printTopics(this.user.subscribedTopics,"My topics");
    }

    private void printTopics(HashMap<String, Topic> topics, String str)
    {
        System.out.println("\n--------"+str+"--------");
        for (Map.Entry<String, Topic> entry : topics.entrySet())
        {
            System.out.println(entry.getKey() + ". " + entry.getValue().getName());
        }
        System.out.println("------------------------------");

        System.out.print('\n');
    }

    public void closeEverything()
    {
        try
        {
            this.out.close();
            this.socket.close();
        } catch (IOException ioException) { ioException.printStackTrace(); }
    }
}