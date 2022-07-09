import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StoryManager extends Thread
{
    public ArrayList<Story> stories = new ArrayList<>();


    @Override
    public void run()
    {
        System.out.println("jnskdnkjcnsd"+stories.size());
        while(true)
        {
            for(Story story : stories)
            {
                if(System.currentTimeMillis() - story.time > 3 * 60000 )
                {
                    System.out.println(System.currentTimeMillis()+" "+ story.time);
                    stories.remove(story);
                    print(story.fileName+" story expired");
                }
            }
        }
    }

    public boolean addStory(ArrayList<Chunk> chunks)
    {
        try {
            constructFile(chunks);
        }catch (IOException e){e.printStackTrace();}
        Chunk c = chunks.get(0);
        MultimediaFile m = c.getMultimediaFile();
        stories.add(new Story(m.content, c.username, chunks));
        return true;
    }

    private void print(String str)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date currentDate = new Date();
        System.out.println(formatter.format(currentDate) +" | " +str);
    }

    private void constructFile(ArrayList<Chunk> chunks) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String fileName ="";

        for (Chunk chunk : chunks)
        {
            MultimediaFile mf = chunk.getMultimediaFile();
            byte[] b = mf.getPiece();
            os.write(b);

            if(fileName.equals("")){fileName = mf.getContent();}
        }

        byte[] bytes = os.toByteArray();

        FileOutputStream fos = new FileOutputStream("./stories/" + fileName);
        fos.write(bytes);
        fos.close();os.close();
    }
}