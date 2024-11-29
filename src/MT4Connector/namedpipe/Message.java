package MT4Connector.namedpipe;

//  名前付きパイプで送受信するメッセージの本体
public class Message
{
    public static final char MSG_NULL                  = (char)0x00;
    public static final char MSG_NOP                   = (char)0x0A;
    public static final char MSG_REQUEST_CALL_FUNCTION = (char)0x23;
    public static final char MSG_REQUEST_PARAMETER     = (char)0x24;
    public static final char MSG_PARAMETER             = (char)0x25;
    public static final char MSG_PARAMETER_END         = (char)0x26;
    public static final char MSG_RETURN_VALUE          = (char)0x27;
    public static final char MSG_REQUEST_ERROR_CODE    = (char)0x28;
    public static final char MSG_ERROR_CODE            = (char)0x29;
    public static final char MSG_REQUEST_AUXILIARY     = (char)0x2A;
    public static final char MSG_AUXILIARY             = (char)0x2B;
    public static final char MSG_AUXILIARY_END         = (char)0x2C;
    
    static final int NUMBER_OF_DATAGRAMS = 2; //  1メッセージあたりのデータグラムの数
    private DataGram[] data = new DataGram[NUMBER_OF_DATAGRAMS];
    public DataGram[] getDataGram()
    {
        return data;
    }
    
    //  コンストラクタ
    public Message()
    {
        for (int i = 0; i < NUMBER_OF_DATAGRAMS; i++)
        {
            data[i] = new DataGram();
        }
        clear();
    }
    public void clear()
    {
        for (int i = 0; i < NUMBER_OF_DATAGRAMS; i++)
        {
            data[i].clear();
        }
    }
    public void dumpAll()
    {
        for (int i = 0; i < NUMBER_OF_DATAGRAMS; i++)
        {
            data[i].dumpMessage();
        }
    }   
    public void setMessageType(char mt)
    {
        data[0].buffer[DataGram.BUFFER_SIZE - 1] = '\0';
        data[0].buffer[0] = mt;
    }
    public void setEmergencyStop(char flg)
    {
        data[0].buffer[DataGram.BUFFER_SIZE - 1] = '\0';
        data[0].buffer[1] = flg;
    }
    public void setSequenceNumber(char num)
    {
        data[0].buffer[DataGram.BUFFER_SIZE - 1] = '\0';
        data[0].buffer[2] = num;
    }
    public int getMessageType()
    {
        return (int)data[0].buffer[0];
    }
    public int getEmergencyStop()
    {
        return (int)data[0].buffer[1];
    }
    public char getSequenceNumber()
    {
        return data[0].buffer[2];
    }
}
