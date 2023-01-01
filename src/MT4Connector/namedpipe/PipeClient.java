package MT4Connector.namedpipe;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//  クライアント側
public class PipeClient
{
    FileChannel Pipe;
    ByteBuffer DataStream;
    
    public PipeClient()
    {
        DataStream = ByteBuffer.allocate(2 * DataGram.BUFFER_SIZE);            
        return;
    }
    public void ConnectToServer(String PipeName) throws FileNotFoundException
    {
        File f = new File("\\\\.\\pipe\\" + PipeName);
        Pipe = new RandomAccessFile(f, "rw").getChannel();
        return;
    }
    public boolean SendMessage(Message msg)
    {
        int p=0;
        DataStream.clear();
        
        if (msg.GetMessageType() == Message.MSG_NOP) return true;
      
        for (int d=0; d<2; d++)
        {
            for (int b=0; b<DataGram.BUFFER_SIZE; b++)
            {
                DataStream.array()[p] = (byte)msg.Data[d].Buffer[b];
                p++;
            }
        }
        
        try
        {
            if (Pipe.write(DataStream) != Message.NUMBER_OF_DATAGRAMS * DataGram.BUFFER_SIZE) return false;
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
        DataStream.clear();
        int bs = 0;
        try
        {
            bs = Pipe.read(DataStream);
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
                msg.Data[d].Buffer[b] = (char)DataStream.array()[p];
                p++;
            }
        }
        
        return true;
    }
    public void Close()
    {
        try
        {
            Pipe.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return;
    }
}
