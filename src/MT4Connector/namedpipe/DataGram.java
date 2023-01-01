package MT4Connector.namedpipe;
import java.util.*;
import java.awt.*;
import java.time.*;

//  送受信するメッセージの最小単位
public class DataGram
{
    //  定数
    static final int BUFFER_SIZE = 256;    
    static final char CHAR_NULL = 0x00;
    static final char CHAR_INITIALIZED = 0x1A;    
    
    protected char[] Buffer;   //  メッセージバッファ本文
    
    //  コンストラクタ
    public DataGram()
    {
        Buffer = new char[BUFFER_SIZE];
        Clear();
        return;
    }
    
    //  データを16進数の文字列型で取得する
    public String GetHexString(char v)
    {
        char[] buf = new char[2];
        Arrays.fill(buf, CHAR_NULL);
        for (int s=0; s<=1; s++)
        {
            char t = (char)((v >> (4 * s)) & 0x0F);
            switch (t)
            {
                case 0x00:
                {
                    buf[1-s] = '0';
                    break;
                }
                case 0x01:
                {
                    buf[1 - s] = '1';
                    break;
                }
                case 0x02:
                {
                    buf[1 - s] = '2';
                    break;
                }
                case 0x03:
                {
                    buf[1 - s] = '3';
                    break;
                }
                case 0x04:
                {
                    buf[1 - s] = '4';
                    break;
                }
                case 0x05:
                {
                    buf[1 - s] = '5';
                   break;
                }
                case 0x06:
                {
                    buf[1 - s] = '6';
                    break;
                }
                case 0x07:
                {
                    buf[1 - s] = '7';
                    break;
                }
                case 0x08:
                {
                    buf[1 - s] = '8';
                    break;
                }
                case 0x09:
                {
                    buf[1 - s] = '9';
                    break;
                }
                case 0x0A:
                {
                    buf[1 - s] = 'A';
                    break;
                }
                case 0x0B:
                {
                    buf[1 - s] = 'B';
                break;
                }
                case 0x0C:
                {
                    buf[1 - s] = 'C';
                    break;
                }
                case 0x0D:
                {
                    buf[1 - s] = 'D';
                    break;
                }
                case 0x0E:
                {
                    buf[1 - s] = 'E';
                    break;
                }
                case 0x0F:
                {
                    buf[1 - s] = 'F';
                    break;
                }
                default:
                {
                    //  エラー
                    buf[1 - s] = '*';
                    break;
                }
            }
        }        
        return String.valueOf(buf);
    }
    
    //  メッセージを画面に出力する（デバッグ目的）
    public void DumpMessage()
    {
        for (int c=0; c<BUFFER_SIZE; c+=32)
        {
            System.out.printf("%s%s%s%s%s%s%s%s %s%s%s%s%s%s%s%s %s%s%s%s%s%s%s%s %s%s%s%s%s%s%s%s \n", 
                GetHexString(Buffer[c + 0]), GetHexString(Buffer[c + 1]), GetHexString(Buffer[c + 2]), GetHexString(Buffer[c + 3]), 
                GetHexString(Buffer[c + 4]), GetHexString(Buffer[c + 5]), GetHexString(Buffer[c + 6]), GetHexString(Buffer[c + 7]), 
                GetHexString(Buffer[c + 8]), GetHexString(Buffer[c + 9]), GetHexString(Buffer[c + 10]), GetHexString(Buffer[c + 11]), 
                GetHexString(Buffer[c + 12]), GetHexString(Buffer[c + 13]), GetHexString(Buffer[c + 14]), GetHexString(Buffer[c + 15]), 
                GetHexString(Buffer[c + 16]), GetHexString(Buffer[c + 17]), GetHexString(Buffer[c + 18]), GetHexString(Buffer[c + 19]), 
                GetHexString(Buffer[c + 20]), GetHexString(Buffer[c + 21]), GetHexString(Buffer[c + 22]), GetHexString(Buffer[c + 23]), 
                GetHexString(Buffer[c + 24]), GetHexString(Buffer[c + 25]), GetHexString(Buffer[c + 26]), GetHexString(Buffer[c + 27]), 
                GetHexString(Buffer[c + 28]), GetHexString(Buffer[c + 29]), GetHexString(Buffer[c + 30]), GetHexString(Buffer[c + 31])
            );
        }
        return;
    }
    
    //  メッセージバッファのクリア
    public void Clear()
    {
        Arrays.fill(Buffer, CHAR_NULL);
        Buffer[BUFFER_SIZE - 1] = CHAR_INITIALIZED;
        return;
    }
    
    //  データの設定
    public void SetData(char v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        Buffer[0] = v;
        return;
    }
    public void SetData(boolean v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        Buffer[0] = v ? (char)1 : (char)0;
        return;
    }
    public void SetData(short v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        Buffer[0] = (char)((v >> 8) & 0xFF);
        Buffer[1] = (char)(v & 0xFF);
        return;
    }
    public void SetData(int v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        Buffer[0] = (char)((v >> 24) & 0xFF);
        Buffer[1] = (char)((v >> 16) & 0xFF);      
        Buffer[2] = (char)((v >> 8) & 0xFF);
        Buffer[3] = (char)(v & 0xFF);
        return;
    }
    public void SetData(float v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        SetData(Float.floatToRawIntBits(v));
        return;
    }
    public void SetData(Color v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        char a = (char)0;
        char r = (char)v.getRed();
        char g = (char)v.getGreen();
        char b = (char)v.getBlue();
        
        SetData(a << 24 | b << 16 | g << 8 | r);
        return;
    }
    public void SetData(long v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        Buffer[0] = (char)((v >> 56) & 0xFF);
        Buffer[1] = (char)((v >> 48) & 0xFF);      
        Buffer[2] = (char)((v >> 40) & 0xFF);
        Buffer[3] = (char)((v >> 32) & 0xFF);
        Buffer[4] = (char)((v >> 24) & 0xFF);
        Buffer[5] = (char)((v >> 16) & 0xFF);      
        Buffer[6] = (char)((v >> 8) & 0xFF);
        Buffer[7] = (char)(v & 0xFF);
        return;
    }
    //public void SetData(ulong v)
    //{
    //    SetData((long)v);
    //    return;
    //}
    public void SetData(double v)
    {
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        SetData(Double.doubleToRawLongBits(v));
        return;
    }
    public void SetData(LocalDateTime v)
    {
        long l = v.toEpochSecond(ZoneOffset.UTC);
        System.out.printf("%d \n", l);
        
        SetData(l);
        return;
    }
    public void SetData(String v)
    {
        Arrays.fill(Buffer, CHAR_NULL);
        for (int c=0; c<Math.min(BUFFER_SIZE - 1, v.length()); c++)
        {
            Buffer[c] = v.charAt(c); 
        }
        Buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        return;
    }
    //  データの取得
    public char GetDataChar()
    {
        return Buffer[0];
    }
    //public uchar GetDataUChar()
    //{
    //    return (uchar)Buffer[0];
    //}
    public boolean GetDataBool()
    {
        return Buffer[0]==1;
    }
    public short GetDataShort()
    {
        short v = 0;
        v = (short)((Buffer[0] << 8) | Buffer[1]);
        return v;
    }
    //public ushort GetDataUShort()
    //{
    //    return (ushort)GetDataShort();
    //    //return (ushort)(
    //    //   (Buffer[0] << 8) | 
    //    //   Buffer[1]
    //    //);
    //}
    public int GetDataInt()
    {
        int i = 0;
        for (int v = 0; v < 4; v++)
        {
            i |= ((int)Buffer[v] & 0xFF) << (8 * (3 - v));
        }
        return i;
    }
    //public uint GetDataUInt()
    //{
    //    return (uint)GetDataInt();
    //    //return (uint)(
    //    //   (Buffer[0] << 24) | 
    //    //   (Buffer[1] << 16) | 
    //    //   (Buffer[2] << 8) | 
    //    //   Buffer[3]
    //    //);
    //}
    public float GetDataFloat()
    {
        return Float.intBitsToFloat(GetDataInt());
    }
    public Color GetDataColor()
    {
        int red = Byte.toUnsignedInt((byte)Buffer[3]);
        int green = Byte.toUnsignedInt((byte)Buffer[2]);
        int blue = Byte.toUnsignedInt((byte)Buffer[1]);
        int alpha = Byte.toUnsignedInt((byte)Buffer[0]);
        Color rv = new Color(red, green, blue, alpha);
        return rv;
    }
    public long GetDataLong()
    {
        long l = 0;
        for (int v = 0; v < 8; v++)
        {
            l |= ((long)Buffer[v] & 0xFF) << (8 * (7 - v));
        }
        return l;
    }
    //public ulong GetDataULong()
    //{
    //    return (ulong)GetDataLong();
    //}
    public double GetDataDouble()
    {
        return Double.longBitsToDouble(GetDataLong());
    }    
    public LocalDateTime GetDataDateTime()
    {
        Instant in = Instant.ofEpochSecond(GetDataLong());
        return LocalDateTime.ofInstant(in, ZoneOffset.UTC);
    }    
    public String GetDataString()
    {
        char buf[] = new char[BUFFER_SIZE];
        int c;
        for (c=0; c<BUFFER_SIZE; c++)
        {
            if (Buffer[c]==CHAR_NULL) break;
            buf[c] = Buffer[c];
        }
        return String.valueOf(buf).substring(0, c);
    }
    //  終了通知を受信したか返却する
    public boolean IsQuitReceived()
    {
        return (Buffer[1] == 1);
    }    
    public boolean IsEmpty()
    {
        for (int c=0; c<BUFFER_SIZE-1; c++)
        {
            if (Buffer[c]!=CHAR_NULL) return false;
        }
        if (Buffer[BUFFER_SIZE-1]!=CHAR_INITIALIZED) return false;
        return true;
    }
    public void CopyDataGramTo(DataGram to)
    {
        System.arraycopy(Buffer, 0, to.Buffer, 0, BUFFER_SIZE);
        return;
    }
};
