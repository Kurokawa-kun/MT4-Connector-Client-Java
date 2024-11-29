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
    
    protected char[] buffer;   //  メッセージバッファ本文
    
    //  コンストラクタ
    public DataGram()
    {
        buffer = new char[BUFFER_SIZE];
        clear();
    }
    
    //  データを16進数の文字列型で取得する
    public String getHexString(char v)
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
    public void dumpMessage()
    {
        for (int c=0; c<BUFFER_SIZE; c+=32)
        {
            System.out.printf("%s%s%s%s%s%s%s%s %s%s%s%s%s%s%s%s %s%s%s%s%s%s%s%s %s%s%s%s%s%s%s%s \n", 
                getHexString(buffer[c + 0]), getHexString(buffer[c + 1]), getHexString(buffer[c + 2]), getHexString(buffer[c + 3]), 
                getHexString(buffer[c + 4]), getHexString(buffer[c + 5]), getHexString(buffer[c + 6]), getHexString(buffer[c + 7]), 
                getHexString(buffer[c + 8]), getHexString(buffer[c + 9]), getHexString(buffer[c + 10]), getHexString(buffer[c + 11]), 
                getHexString(buffer[c + 12]), getHexString(buffer[c + 13]), getHexString(buffer[c + 14]), getHexString(buffer[c + 15]), 
                getHexString(buffer[c + 16]), getHexString(buffer[c + 17]), getHexString(buffer[c + 18]), getHexString(buffer[c + 19]), 
                getHexString(buffer[c + 20]), getHexString(buffer[c + 21]), getHexString(buffer[c + 22]), getHexString(buffer[c + 23]), 
                getHexString(buffer[c + 24]), getHexString(buffer[c + 25]), getHexString(buffer[c + 26]), getHexString(buffer[c + 27]), 
                getHexString(buffer[c + 28]), getHexString(buffer[c + 29]), getHexString(buffer[c + 30]), getHexString(buffer[c + 31])
            );
        }
    }
    
    //  メッセージバッファのクリア
    public void clear()
    {
        Arrays.fill(buffer, CHAR_NULL);
        buffer[BUFFER_SIZE - 1] = CHAR_INITIALIZED;
    }
    
    //  データの設定
    public void setData(char v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        buffer[0] = v;
    }
    public void setData(boolean v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        buffer[0] = v ? (char)1 : (char)0;
    }
    public void setData(short v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        buffer[0] = (char)((v >> 8) & 0xFF);
        buffer[1] = (char)(v & 0xFF);
    }
    public void setData(int v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        buffer[0] = (char)((v >> 24) & 0xFF);
        buffer[1] = (char)((v >> 16) & 0xFF);      
        buffer[2] = (char)((v >> 8) & 0xFF);
        buffer[3] = (char)(v & 0xFF);
    }
    public void setData(float v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        setData(Float.floatToRawIntBits(v));
    }
    public void setData(Color v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        char a = (char)0;
        char r = (char)v.getRed();
        char g = (char)v.getGreen();
        char b = (char)v.getBlue();        
        setData(a << 24 | b << 16 | g << 8 | r);
    }
    public void setData(long v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        buffer[0] = (char)((v >> 56) & 0xFF);
        buffer[1] = (char)((v >> 48) & 0xFF);      
        buffer[2] = (char)((v >> 40) & 0xFF);
        buffer[3] = (char)((v >> 32) & 0xFF);
        buffer[4] = (char)((v >> 24) & 0xFF);
        buffer[5] = (char)((v >> 16) & 0xFF);      
        buffer[6] = (char)((v >> 8) & 0xFF);
        buffer[7] = (char)(v & 0xFF);
    }
    //public void setData(ulong v)
    //{
    //    setData((long)v);
    //}
    public void setData(double v)
    {
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
        setData(Double.doubleToRawLongBits(v));
    }
    public void setData(LocalDateTime v)
    {
        long l = v.toEpochSecond(ZoneOffset.UTC);
        System.out.printf("%d \n", l);
        setData(l);
    }
    public void setData(String v)
    {
        Arrays.fill(buffer, CHAR_NULL);
        for (int c=0; c<Math.min(BUFFER_SIZE - 1, v.length()); c++)
        {
            buffer[c] = v.charAt(c); 
        }
        buffer[BUFFER_SIZE - 1] = CHAR_NULL;
    }
    //  データの取得
    public char getDataChar()
    {
        return buffer[0];
    }
    //public uchar getDataUChar()
    //{
    //    return (uchar)buffer[0];
    //}
    public boolean getDataBool()
    {
        return buffer[0]==1;
    }
    public short getDataShort()
    {
        short v = 0;
        v = (short)((buffer[0] << 8) | buffer[1]);
        return v;
    }
    //public ushort getDataUShort()
    //{
    //    return (ushort)GetDataShort();
    //    //return (ushort)(
    //    //   (buffer[0] << 8) | 
    //    //   buffer[1]
    //    //);
    //}
    public int getDataInt()
    {
        int i = 0;
        for (int v = 0; v < 4; v++)
        {
            i |= ((int)buffer[v] & 0xFF) << (8 * (3 - v));
        }
        return i;
    }
    //public uint getDataUInt()
    //{
    //    return (uint)GetDataInt();
    //    //return (uint)(
    //    //   (buffer[0] << 24) | 
    //    //   (buffer[1] << 16) | 
    //    //   (buffer[2] << 8) | 
    //    //   buffer[3]
    //    //);
    //}
    public float getDataFloat()
    {
        return Float.intBitsToFloat(getDataInt());
    }
    public Color getDataColor()
    {
        int red = Byte.toUnsignedInt((byte)buffer[3]);
        int green = Byte.toUnsignedInt((byte)buffer[2]);
        int blue = Byte.toUnsignedInt((byte)buffer[1]);
        int alpha = Byte.toUnsignedInt((byte)buffer[0]);
        Color rv = new Color(red, green, blue, alpha);
        return rv;
    }
    public long getDataLong()
    {
        long l = 0;
        for (int v = 0; v < 8; v++)
        {
            l |= ((long)buffer[v] & 0xFF) << (8 * (7 - v));
        }
        return l;
    }
    //public ulong getDataULong()
    //{
    //    return (ulong)getDataLong();
    //}
    public double getDataDouble()
    {
        return Double.longBitsToDouble(getDataLong());
    }    
    public LocalDateTime getDataDateTime()
    {
        Instant in = Instant.ofEpochSecond(getDataLong());
        return LocalDateTime.ofInstant(in, ZoneOffset.UTC);
    }    
    public String getDataString()
    {
        char buf[] = new char[BUFFER_SIZE];
        int c;
        for (c=0; c<BUFFER_SIZE; c++)
        {
            if (buffer[c]==CHAR_NULL) break;
            buf[c] = buffer[c];
        }
        return String.valueOf(buf).substring(0, c);
    }
    //  終了通知を受信したか返却する
    public boolean isQuitReceived()
    {
        return (buffer[1] == 1);
    }    
    public boolean isEmpty()
    {
        for (int c=0; c<BUFFER_SIZE-1; c++)
        {
            if (buffer[c]!=CHAR_NULL) return false;
        }
        if (buffer[BUFFER_SIZE-1]!=CHAR_INITIALIZED) return false;
        return true;
    }
    public void copyDataGramTo(DataGram to)
    {
        System.arraycopy(buffer, 0, to.buffer, 0, BUFFER_SIZE);
    }
};
