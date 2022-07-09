import java.io.*;
import java.net.*;
import java.util.*;

public class User
{
    String username;
    String activeTopicName;
    String activeTopicId;
    HashMap<String, Topic> allTopics = new HashMap<>();//this hashmap is just used to display the topics that are received from the broker
    HashMap<String, Topic> subscribedTopics = new HashMap<>();//Topics this user is subscribed to
    boolean isInChat = false;
    public String string;

    Consumer consumer;
    Publisher publisher;

    public User(String username)
    {
        this.username = username;
    }

    void initialize(Socket socket, boolean change)
    {
        if(publisher!=null){publisher.closeEverything();}
        if(consumer!=null){consumer.closeEverything();}

        this.consumer = new Consumer(socket,this);
        this.publisher = new Publisher(socket,this, change);

        this.consumer.start();
        this.publisher.start();
    }

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String name = scanner.nextLine();

        boolean valid = false;

        for(int i = 0; i < name.length(); i ++)
        {
            if(name.charAt(i) != ' ')
            {
                valid = true;
                break;
            }
        }

        if(!valid)//check if username is valid
        {
            while(!valid)
            {
                System.out.print("Invalid input > Try again: ");
                name = scanner.nextLine();

                for(int i = 0; i < name.length(); i ++)
                {
                    if (name.charAt(i) != ' ')
                    {
                        valid = true;
                        break;
                    }
                }
            }
        }
        User user = new User(name);
        Socket socket = new Socket("127.0.0.1", 6000);
        user.initialize(socket, false);
    }
}