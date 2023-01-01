package MT4Connector.types;
import java.time.*;

//  ローソク足
public class Candle
{
    public double Open;
    public double High;
    public double Low;
    public double Close;
    public long Volume;
    public LocalDateTime Time;
    public Candle(double open, double high, double low, double close, long volume, LocalDateTime time)
    {        
        Open = open;
        High = high;
        Low = low;
        Close = close;
        Volume = volume;
        Time = time;
        return;
    }
}
