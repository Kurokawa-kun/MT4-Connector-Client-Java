package MT4Connector.namedpipe;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//  クライアント側
public class PipeClient
{
    FileChannel pipe;
    ByteBuffer dataStream;
    
    public PipeClient()
    {
        dataStream = ByteBuffer.allocate(2 * DataGram.BUFFER_SIZE);            
    }
    public void ConnectToServer(String PipeName) throws FileNotFoundException
    {
        File f = new File("\\\\.\\pipe\\" + PipeName);
        pipe = new RandomAccessFile(f, "rw").getChannel();
    }
    public boolean SendMessage(Message msg)
    {
        int p=0;
        dataStream.clear();
        
        if (msg.getMessageType() == Message.MSG_NOP) return true;
        for (int d=0; d<2; d++)
        {
            for (int b=0; b<DataGram.BUFFER_SIZE; b++)
            {
                dataStream.array()[p] = (byte)msg.getDataGram()[d].buffer[b];
                p++;
            }
        }
        
        try
        {
            if (pipe.write(dataStream) != Message.NUMBER_OF_DATAGRAMS * DataGram.BUFFER_SIZE) return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }                
        
        return true;
    }
    public boolean ReceiveMessage(Message msg)
    {
        int p=0;
        dataStream.clear();
        int bs = 0;
        
        try
        {
            bs = pipe.read(dataStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (bs == 0) return false;        
        for (int d=0; d<2; d++)
        {
            for (int b=0; b<DataGram.BUFFER_SIZE; b++)
            {
                msg.getDataGram()[d].buffer[b] = (char)dataStream.array()[p];
                p++;
            }
        }
        
        return true;
    }
    public void Close()
    {
        try
        {
            pipe.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
