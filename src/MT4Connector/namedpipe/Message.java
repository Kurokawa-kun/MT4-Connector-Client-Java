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
    public DataGram[] Data = new DataGram[NUMBER_OF_DATAGRAMS];
    
    //  コンストラクタ
    public Message()
    {
        for (int i = 0; i < NUMBER_OF_DATAGRAMS; i++)
        {
            Data[i] = new DataGram();
        }
        Clear();
        return;
    }
    public void Clear()
    {
        for (int i = 0; i < NUMBER_OF_DATAGRAMS; i++)
        {
            Data[i].Clear();
        }
        return;
    }
    public void DumpAll()
    {
        for (int i = 0; i < NUMBER_OF_DATAGRAMS; i++)
        {
            Data[i].DumpMessage();
        }
        return;
    }   
    public void SetMessageType(char mt)
    {
        Data[0].Buffer[DataGram.BUFFER_SIZE - 1] = '\0';
        Data[0].Buffer[0] = mt;
        return;
    }
    public void SetEmergencyStop(char flg)
    {
        Data[0].Buffer[DataGram.BUFFER_SIZE - 1] = '\0';
        Data[0].Buffer[1] = flg;
        return;
    }
    public void SetSequenceNumber(char num)
    {
        Data[0].Buffer[DataGram.BUFFER_SIZE - 1] = '\0';
        Data[0].Buffer[2] = num;
        return;
    }
    public int GetMessageType()
    {
      return (int)Data[0].Buffer[0];
    }
    public int GetEmergencyStop()
    {
      return (int)Data[0].Buffer[1];
    }
    public char GetSequenceNumber()
    {
        return Data[0].Buffer[2];
    }
}
