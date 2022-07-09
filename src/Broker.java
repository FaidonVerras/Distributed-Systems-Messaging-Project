import java.io.*;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Broker {

    ServerSocket providerSocket;
    Socket socket = null;

    private String ip;
    private int id;
    private int port;
    int numberOfTopics = 0; //Number of topics of THIS broker

    protected static ArrayList<Broker> brokers = new ArrayList<>();

    protected HashMap<String, Topic> allTopics = new HashMap<>();//topics that exist
    protected HashMap<String, Topic> myTopics = new HashMap<>();//topics this broker has

    public StoryManager storyManager = new StoryManager();


    private boolean debugMode = true;// STHN EXETASH THA GINEI FALSE WSTE NA PAREI PORTS APO TO ARXEIO

    public static void main(String[] args)
    {
        Broker broker = new Broker("127.0.0.1");
        brokers.add(broker);
        broker.importTopics("./config/topics.txt");
        broker.printTopics();
        broker.initialize();
    }

    public Broker(String ip)
    {
        this.ip = ip;

        if(debugMode)
        {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter port: ");
            String p = scanner.nextLine();
            this.port = Integer.parseInt(p);
        }else {
            this.port = this.importConfiguration("./config/config.txt", ip);
        }

        this.id = this.port - 6000;

        System.out.println("+++++++++++++++++++");
        System.out.println("----- BROKER ------");
        System.out.println("IP: " + this.ip);
        System.out.println("PORT: " + this.port);
        System.out.println("ID: " + this.id);
        System.out.println("+++++++++++++++++++");


        try{this.providerSocket = new ServerSocket(this.port);}
        catch(IOException e){e.printStackTrace();}
    }

    void initialize() {

        while (!providerSocket.isClosed())
        {
            try
            {
                this.socket = providerSocket.accept();
            }
            catch (IOException e){e.printStackTrace();}

            UserHandler handler = new UserHandler(socket, this);
            handler.start();
        }
    }

    private void importTopics(String fileName)
    {
        try 
        {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine())
            {
                String topic = scanner.nextLine();
                this.addTopic(topic);
            }
            scanner.close();
        } catch (FileNotFoundException e) {e.printStackTrace();}
    }

    private void addTopic(String name)
    {
        Topic topic = new Topic(name);
        int s = allTopics.size()+1;

        String key = Integer.toString(s);
        topic.setID(key);

        BigInteger b = sha1(topic.getName());
        int remainder = b.mod(new BigInteger("3")).intValue();

        if(remainder == this.id)
        {
            topic.brokerID = this.id;
            topic.brokerPort = this.port;
            this.myTopics.put(key,topic);
        }else{
            topic.brokerID = remainder;
            topic.brokerPort = 6000+remainder;
        }
        this.allTopics.put(key,topic);
        this.numberOfTopics = myTopics.size();
    }

    private int importConfiguration(String fileName, String ip)
    {
        int port = 0;
        try
        {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine())
            {
                String string = scanner.nextLine();

                String[] parts = string.split(":");
                String part1 = parts[0];
                String part2 = parts[1];

                if(part1.equals(ip))
                {
                    port = Integer.parseInt(part2);
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {e.printStackTrace();}
        return port;
    }

    private void printTopics()
    {
        System.out.println("Current topics");
        for (Map.Entry<String, Topic> entry : this.myTopics.entrySet())
        {
            System.out.println(entry.getKey() + ". " + entry.getValue().getName());
        }
        System.out.print('\n');
    }

    private static BigInteger sha1(String input)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger n = new BigInteger(1, messageDigest);
            return n;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public int getID(){return this.id;}
}