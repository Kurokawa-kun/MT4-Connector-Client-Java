import java.awt.*;
import java.io.*;
import MT4Connector.Connector;
import MT4Connector.consts.*;

public class MyApp2 extends MT4Connector.Connector
{
    String[] symbolName;
    double[] currentAsk;
    double[] currentBid;
    double[] previousAsk;
    double[] previousBid;
    String[] format = null;
    int[] digits;
    
    @Override
    public int OnInit()
    {
        System.out.println("終了するにはctrl+cを押してください。");
        int t = SymbolsTotal(false);
        symbolName = new String[t];
        currentAsk = new double[t];
        currentBid = new double[t];
        previousAsk = new double[t];
        previousBid = new double[t];
        format = new String[t];
        digits = new int[t];
        
        for (int p = 0; p < SymbolsTotal(false); p++)
        {
            symbolName[p] = SymbolName(p, false);
            format[p] = String.format("%%.%df", (int)MarketInfo(symbolName[p], MarketInfo.DoubleProperty.MODE_DIGITS));   
            digits[p] = (int)MarketInfo(symbolName[p], MarketInfo.DoubleProperty.MODE_DIGITS);
        }
        EventSetTimer(3);
        return MT4Runtime.InitializeRetCode.INIT_SUCCEEDED;
    }
    @Override
    public void OnTick()
    {
    }
    @Override
    public void OnTimer()
    {
        for (int p = 0; p < symbolName.length; p++)
        {
            currentAsk[p] = MarketInfo(symbolName[p], MarketInfo.DoubleProperty.MODE_ASK);
            currentBid[p] = MarketInfo(symbolName[p], MarketInfo.DoubleProperty.MODE_BID);

            Reset();
            System.out.printf("%-11s: ", symbolName[p]);

            if (currentBid[p]==previousBid[p])
            {
                CursorForward(10);
            }
            else
            {
                SetForegroundColor(Color.RED);
            
                if (currentBid[p]>previousBid[p]) SetForegroundColor(Color.RED);
                else SetForegroundColor(Color.BLUE);
                System.out.printf("%-9s ", String.format(format[p], currentBid[p]));
                previousBid[p] = currentBid[p];
            }
            
            if (currentAsk[p]==previousAsk[p])
            {
                CursorForward(10);
            }
            else
            {
                SetForegroundColor(Color.RED);
            
                if (currentAsk[p]>previousAsk[p]) SetForegroundColor(Color.RED);
                else SetForegroundColor(Color.BLUE);
                System.out.printf("%-9s ", String.format(format[p], currentAsk[p]));
                previousAsk[p] = currentAsk[p];
            }
            if ((p + 1) % 4 == 0) System.out.println();
        }
        System.out.println();
        CursorUp((int)(symbolName.length / 4) + 1);
    }
    @Override
    public void OnDeinit(final int reason)
    {
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
    }

    private static void _printEscapeCode()
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
    }    
    //  文字の色を変えるため
    public static void SetForegroundColor(Color c)
    {
        _printEscapeCode();
        System.out.printf(String.format("[38;2;%d;%d;%dm", c.getRed(), c.getGreen(), c.getBlue()));
    }
    public static void SetBackgroundColor(Color c)
    {
        _printEscapeCode();
        System.out.printf(String.format("[48;2;%d;%d;%dm", c.getRed(), c.getGreen(), c.getBlue()));
    }    
    public static void CursorUp(int n)
    {
        _printEscapeCode();
        System.out.printf("[%dA", n);
    }
    public static void CursorDown(int n)
    {
        _printEscapeCode();
        System.out.printf("[%dB", n);
    }
    public static void CursorForward(int n)
    {
        _printEscapeCode();
        System.out.printf("[%dC", n);
    }
    public static void CursorBack(int n)
    {
        _printEscapeCode();
        System.out.printf("[%dD", n);
    }
    public static void Reset()
    {
        _printEscapeCode();
        System.out.printf("[0m");
    }
}
