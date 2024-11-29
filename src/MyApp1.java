import MT4Connector.consts.*;

public class MyApp1 extends MT4Connector.Connector
{
    int digits;             //  このシンボルの小数点以下の桁数
    String format = null;   //  このシンボルを表示するためのフォーマット
    
    @Override
    public int OnInit()
    {
        System.out.println("5秒間スリープして成行注文を1回発行するだけのプログラムです。");
        digits = (int)MarketInfo(Symbol(), MarketInfo.DoubleProperty.MODE_DIGITS);
        format = String.format("%%.%df", digits);   
        EventSetTimer(5);
        return MT4Runtime.InitializeRetCode.INIT_SUCCEEDED;
    }
    @Override
    public void OnTick()
    {
        //  現在価格を表示する
        System.out.printf("%-10s: Bid:%-9s Ask:%-9s \n", Symbol(), String.format(format, Bid), String.format(format, Ask));
    }
    @Override
    public void OnTimer()
    {
        EventKillTimer();
        int t = OrderSend(Symbol(), MT4Runtime.OrderType.OP_BUY, 1.00, Ask, 0, Ask - 100 * Point(), Ask + 250 * Point());   //  成行注文
        if (t == -1)
        {
            System.out.printf("OrderSendが失敗しました。エラーコードは'%d'。", GetLastError());
            return;
        }
        System.out.printf("チケット番号は'%d'。\n", t);
        ExpertRemove();   //  プログラムの終了
    }
    @Override
    public void OnDeinit(final int reason)
    {
        //  特に何もしない
    }
    //  メイン
    public static void main(String[] args)
    {
        System.out.println("MyApp1(Java版)を起動します。");
        if (args.length < 2)
        {
            System.out.println("必要なパラメタが指定されていません。");
            System.out.println("MyApp1(Java版)を終了します。");
            return;
        }
        
        String PipeName = args[1];
        new MyApp1().ConnectToMT4(PipeName);
        System.out.println("MyApp1(Java版)を終了します。");
    }
}
