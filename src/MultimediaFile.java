import java.io.*;

public class MultimediaFile implements Serializable
{
    public String content;
    public String profileName;
    public String type;

    byte[] piece;

    public MultimediaFile(String content,String name, String type)//gia text
    {
        this.content = content;
        this.profileName = name;
        this.type = type;
    }

    public MultimediaFile(String content, byte[] filePieces, String name,String type)//gia file pieces
    {
        this.content = content;
        this.profileName = name;
        this.piece = filePieces;
        this.type = type;
    }

    public String getProfileName(){return profileName;}
    public String getContent(){return content;}
    public String getType(){return type;}
    public byte[] getPiece(){return piece;}
}