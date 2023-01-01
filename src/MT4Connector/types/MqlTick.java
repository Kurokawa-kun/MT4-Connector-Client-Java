package MT4Connector.types;
import java.time.*;

//  MT4のMqlTick構造体に相当するクラス
public class MqlTick
{
    public double ask;
    public double bid;
    public int flags;
    public double last;
    public LocalDateTime time;
    public long time_msc;
    public long volume;
    public double volume_real;
    
    public MqlTick()
    {
        ask = 0;
        bid = 0;
        flags = 0;
        double last = 0;
        time = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        time_msc = 0;
        volume = 0;
        volume_real = 0;
        return;
    }
}
