import java.awt.*;
import java.io.*;
import MT4Connector.Connector;
import MT4Connector.consts.*;

public class MyApp2 extends MT4Connector.Connector
{
    String[] SymbolName;    
    double[] CurrentAsk;
    double[] CurrentBid;
    double[] PreviousAsk;
    double[] PreviousBid;
    String[] format = null;
    int[] digits;
    
    @Override
    public int OnInit()
    {
        System.out.println("終了するにはctrl+cを押してください。");
        int t = SymbolsTotal(false);
        SymbolName = new String[t];
        CurrentAsk = new double[t];
        CurrentBid = new double[t];
        PreviousAsk = new double[t];
        PreviousBid = new double[t];
        format = new String[t];
        digits = new int[t];
        
        for (int p = 0; p < SymbolsTotal(false); p++)
        {
            SymbolName[p] = SymbolName(p, false);
            format[p] = String.format("%%.%df", (int)MarketInfo(SymbolName[p], MarketInfo.DoubleProperty.MODE_DIGITS));   
            digits[p] = (int)MarketInfo(SymbolName[p], MarketInfo.DoubleProperty.MODE_DIGITS);
        }
        EventSetTimer(3);
        return MT4Runtime.InitializeRetCode.INIT_SUCCEEDED;
    }
    @Override
    public void OnTick()
    {
        return;
    }
    @Override
    public void OnTimer()
    {
        for (int p = 0; p < SymbolName.length; p++)
        {
            CurrentAsk[p] = MarketInfo(SymbolName[p], MarketInfo.DoubleProperty.MODE_ASK);
            CurrentBid[p] = MarketInfo(SymbolName[p], MarketInfo.DoubleProperty.MODE_BID);

            Reset();
            System.out.printf("%-11s: ", SymbolName[p]);

            if (CurrentBid[p]==PreviousBid[p])
            {
                CursorForward(10);
            }
            else
            {
                SetForegroundColor(Color.RED);
            
                if (CurrentBid[p]>PreviousBid[p]) SetForegroundColor(Color.RED);
                else SetForegroundColor(Color.BLUE);
                System.out.printf("%-9s ", String.format(format[p], CurrentBid[p]));
                PreviousBid[p] = CurrentBid[p];
            }
            
            if (CurrentAsk[p]==PreviousAsk[p])
            {
                CursorForward(10);
            }
            else
            {
                SetForegroundColor(Color.RED);
            
                if (CurrentAsk[p]>PreviousAsk[p]) SetForegroundColor(Color.RED);
                else SetForegroundColor(Color.BLUE);
                System.out.printf("%-9s ", String.format(format[p], CurrentAsk[p]));
                PreviousAsk[p] = CurrentAsk[p];
            }
            if ((p + 1) % 4 == 0) System.out.println();
        }
        System.out.println();
        CursorUp((int)(SymbolName.length / 4) + 1);
        return;
    }
    @Override
    public void OnDeinit(final int reason)
    {
        return;
    }
    public static void main(String[] args)
    {
        System.out.println("MyApp2を起動しました。");
        if (args.length < 2)
        {
            System.out.println("パラメタが指定されていません。");
            System.out.println("MyApp2を終了します。");
            return;
        }
        
        String PipeName = args[1];
        Connector p = new MyApp2();
        p.ConnectToMT4(PipeName);
        System.out.println("MyApp2を終了します。");
        
        return;
    }


    private static void _PrintEscapeCode()
    {
        if (System.getProperty("os.name").startsWith("Windows"))
        {
            //  Windows
            String cmd = "cmd /C type EscapeCode";
            String[] sar = cmd.split(" ");
            ProcessBuilder pb = new ProcessBuilder(sar);
            pb.inheritIO();
            try
            {
                Process p = pb.start();
                p.waitFor();
            }
            catch (IOException | InterruptedException e)
            {
                //  特に何もしない
            }        
        }
        else
        {
            //  それ以外のOS
            System.out.print((char)0x001B);
        }
        return;
    }    
    //  文字の色を変えるため
    public static void SetForegroundColor(Color c)
    {
        _PrintEscapeCode();
        System.out.printf(String.format("[38;2;%d;%d;%dm", c.getRed(), c.getGreen(), c.getBlue()));
        return;
    }
    public static void SetBackgroundColor(Color c)
    {
        _PrintEscapeCode();
        System.out.printf(String.format("[48;2;%d;%d;%dm", c.getRed(), c.getGreen(), c.getBlue()));
        return;
    }    
    public static void CursorUp(int n)
    {
        _PrintEscapeCode();
        System.out.printf("[%dA", n);
        return;
    }
    public static void CursorDown(int n)
    {
        _PrintEscapeCode();
        System.out.printf("[%dB", n);
        return;
    }
    public static void CursorForward(int n)
    {
        _PrintEscapeCode();
        System.out.printf("[%dC", n);
        return;
    }
    public static void CursorBack(int n)
    {
        _PrintEscapeCode();
        System.out.printf("[%dD", n);
        return;
    }
    public static void Reset()
    {
        _PrintEscapeCode();
        System.out.printf("[0m");
        return;
    }
}
