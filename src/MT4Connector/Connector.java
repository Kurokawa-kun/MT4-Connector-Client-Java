package MT4Connector;
import java.util.*;
import java.time.*;
import java.io.*;
import java.awt.*;
import MT4Connector.consts.*;
import MT4Connector.types.*;
import MT4Connector.namedpipe.*;

//  MT4コネクタ（クライアント側）
public abstract class Connector
{
    protected PipeClient Pipe;  //  名前付きパイプの実体
    protected boolean FlagEmergencyStop = false;  //  システムを停止する必要が生じたとき。このフラグが立ったときはサーバー側にクライアント側のOnDeinit呼び出しを依頼する
        protected HashMap<String/*通貨ペア*/, HashMap<Integer/*時間足*/, ArrayList<Candle>>> TickData = new HashMap<>(50);  //  全シンボル全時間足のローソク足をロードするためのリスト
    //  ！このTickDataのしくみは改善事項。実行速度が遅すぎて実用的ではない。    
    public final static int[] TimeFrames = MT4Runtime.TimeFrame.MT4TimeFrames; //  対応している時間足の一覧
    protected boolean DebugMode = false;    //  デバッグモードの有無、毎回MT4側にデバッグモード指定の有無を確認するのはオーバーヘッドが大きいので自前の変数で持つ

    //  MT4の関数に対応する関数。これは継承先のクラスで実装してもらう
    public abstract int OnInit();
    public abstract void OnTick();
    public void OnTimer(){}
    public abstract void OnDeinit(final int reason);
    //  OnInitが呼ばれる前に内部的に実行される。プラットフォームやサーバに関する情報をやり取りするための関数（将来の機能拡張用）
    public final void OnPreInit(boolean debug)
    {
        DebugMode = debug;  //  デバッグモードの設定
        PrintDebugMessage("OnPreInitが呼ばれました。");
        PrintDebugMessage("OnPreInitを抜けます。");
        return;
    }    
    //  内部的に呼び出されるタイマー。他の通貨ペアの情報などを裏方で送受信するため（将来の機能拡張用）
    public final void OnTimerInternal()
    {
        PrintDebugMessage("OnTimerInternalが呼ばれました。");
        PrintDebugMessage("OnTimerInternalを抜けます。");
        return;
    }
    
    //  ----------  MT4のグローバル変数に相当するもの  ----------
    protected double Ask;
    protected double Bid;
    protected long Volume;
    protected int Bars;
    protected int Digits;
    protected int Point;
    private int LastError = 0;    //  GetLastError関数で取得するため
        
    //  ----------  独自に追加した関数  ----------
    //  デバッグモードの場合のみメッセージを出力する
    protected void PrintDebugMessage(String message)
    {
        if (DebugMode) System.err.println(message);
        return;
    }
    //  TickDataリストの初期化
    public void InitChartList()
    {
        //  初期化
        for (int l = 0; l < SymbolsTotal(false); l++)
        {
            String cp = SymbolName(l, false);
            TickData.put(cp, new HashMap<Integer, ArrayList<Candle>>(21));
            for (int t : TimeFrames)
            {
                TickData.get(cp).put(t, new ArrayList<Candle>(10000));
            }
        }
        return;
    }    
    //  MT4からローソク足を読み込んでTickDataリストに追加する
    //  ！今の実装では全シンボル全時間足のローソク足をロードすると30分近くかかるため実用的ではない（改善事項）
    public void ReadChartList(String currencyPair, int timeFrame)
    {
        int tf = timeFrame == MT4Runtime.TimeFrame.PERIOD_CURRENT ? Period() : timeFrame;
        int b = iBars(currencyPair, tf);
        
        for (int i = 0; i < b; i += FuncInfo.MAX_NUMBER_OF_AUXILIARIES)
        {
            int c = b - i < FuncInfo.MAX_NUMBER_OF_AUXILIARIES ? b - i : FuncInfo.MAX_NUMBER_OF_AUXILIARIES;
            double aHigh[] = new double[c];
            double aOpen[] = new double[c];
            double aLow[] = new double[c];
            double aClose[] = new double[c];
            long aVolume[] = new long[c];
            LocalDateTime aTime[] = new LocalDateTime[c];
            
            GetCandlesHigh(currencyPair, tf, aHigh, i, i + c);
            GetCandlesOpen(currencyPair, tf, aOpen, i, i + c);
            GetCandlesLow(currencyPair, tf, aLow, i, i + c);
            GetCandlesClose(currencyPair, tf, aClose, i, i + c);
            GetCandlesVolume(currencyPair, tf, aVolume, i, i + c);            
            int r = GetCandlesTime(currencyPair, tf, aTime, i, i + c);                        
            for (int a = 0; a < r; a++)
            {
                TickData.get(currencyPair).get(tf).add(new Candle(aOpen[a], aHigh[a], aLow[a], aClose[a], aVolume[a], aTime[a]));
            }
        }
        return;
    }
    //  MT4MT5側の独自追加関数を呼び出す
    public int GetCandlesOpen(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "CopyOpen";
        funcInfo.Parameter[0].SetData(symbolName);
        funcInfo.Parameter[1].SetData(timeFrame);
        funcInfo.Parameter[2].SetData(barFrom);
        funcInfo.Parameter[3].SetData(barTo - barFrom);
        funcInfo.Parameter[4].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        for (int i = 0; i < funcInfo.GetNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.Auxiliary[i].GetDataDouble();
        }
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GetCandlesHigh(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "CopyHigh";
        funcInfo.Parameter[0].SetData(symbolName);
        funcInfo.Parameter[1].SetData(timeFrame);
        funcInfo.Parameter[2].SetData(barFrom);
        funcInfo.Parameter[3].SetData(barTo - barFrom);
        funcInfo.Parameter[4].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        for (int i = 0; i < funcInfo.GetNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.Auxiliary[i].GetDataDouble();
        }
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GetCandlesLow(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "CopyLow";
        funcInfo.Parameter[0].SetData(symbolName);
        funcInfo.Parameter[1].SetData(timeFrame);
        funcInfo.Parameter[2].SetData(barFrom);
        funcInfo.Parameter[3].SetData(barTo - barFrom);
        funcInfo.Parameter[4].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        for (int i = 0; i < funcInfo.GetNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.Auxiliary[i].GetDataDouble();
        }
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GetCandlesClose(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "CopyClose";
        funcInfo.Parameter[0].SetData(symbolName);
        funcInfo.Parameter[1].SetData(timeFrame);
        funcInfo.Parameter[2].SetData(barFrom);
        funcInfo.Parameter[3].SetData(barTo - barFrom);
        funcInfo.Parameter[4].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        for (int i = 0; i < funcInfo.GetNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.Auxiliary[i].GetDataDouble();
        }
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GetCandlesVolume(String symbolName, int timeFrame, long[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "CopyTickVolume";
        funcInfo.Parameter[0].SetData(symbolName);
        funcInfo.Parameter[1].SetData(timeFrame);
        funcInfo.Parameter[2].SetData(barFrom);
        funcInfo.Parameter[3].SetData(barTo - barFrom);
        funcInfo.Parameter[4].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        for (int i = 0; i < funcInfo.GetNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.Auxiliary[i].GetDataLong();
        }
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GetCandlesTime(String symbolName, int timeFrame, LocalDateTime[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "CopyTime";
        funcInfo.Parameter[0].SetData(symbolName);
        funcInfo.Parameter[1].SetData(timeFrame);
        funcInfo.Parameter[2].SetData(barFrom);
        funcInfo.Parameter[3].SetData(barTo - barFrom);
        funcInfo.Parameter[4].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        for (int i = 0; i < funcInfo.GetNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.Auxiliary[i].GetDataDateTime();
        }
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    //  MT4独自形式の色情報をJavaのColorクラスに変換する
    public static Color MT4StringColorToColor(String strColor)
    {
        int[] Intensity = new int[3];
        String ssb = strColor.replace("C", "").replace("'", "");
        for (int p=0; p<3; p++)
        {
            String se = ssb.split(",")[p].replace(" ", "");
            if (se.startsWith("0x"))
            {
                Intensity[p] = Integer.parseInt(se.substring(2, se.length()), 16);
            }
            else
            {
                Intensity[p] = Integer.parseInt(se, 10);
            }
        }
        Color rc = new Color(Intensity[0], Intensity[1], Intensity[2]);
        return rc;
    }
    
    //  MT4独自形式の色情報をJavaのColorクラスに変換する
    public static Color MT4ColorToColor(int intColor)
    {
        int[] Intensity = new int[3];
        Intensity[2] = intColor >> 16 & 0xFF;
        Intensity[1] = intColor >>  8 & 0xFF;
        Intensity[0] = intColor & 0xFF;
        return new Color(Intensity[0], Intensity[1], Intensity[2]);
    }
    
    //  サーバー側が返却したエラーコード（LastError変数に格納されている）を返却する。MT4のGetLastError関数と同じように使える。
    public int GetLastError()
    {
        return LastError;
    }
    
    public int GetPlatformVersion()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GetPlatformVersion";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    //  リソース
    public boolean PlaySound(String FileName)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "PlaySound";
        funcInfo.Parameter[0].SetData(FileName);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        //LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();        
    }
    //  共通関数
    //void Alert(argument...)  //  ！未サポート。使わなさそうだから今は実装しない
    
    //ENUM_POINTER_TYPE  CheckPointer(object* anyobject);  //  クライアントから呼び出す必要がないので実装しない
    
    public int iBars(String CurrencyPair, int TimeFrame)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "iBars";
        funcInfo.Parameter[0].SetData(CurrencyPair);
        funcInfo.Parameter[1].SetData(TimeFrame);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    public LocalDateTime TimeCurrent()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TimeCurrent";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public LocalDateTime TimeLocal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TimeLocal";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public LocalDateTime TimeGMT()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TimeGMT";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public int TimeDaylightSavings()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TimeDaylightSavings";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public double AccountInfoDouble(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountInfoDouble";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public long AccountInfoInteger(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountInfoInteger";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataLong();
    }
    public String AccountInfoString(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountInfoString";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }

    public double AccountBalance()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountBalance";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    
    public double AccountCredit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountCredit";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    
    public String AccountCompany()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountCompany";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    
    public String AccountCurrency()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountCurrency";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public double AccountEquity()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountEquity";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }

    public double AccountFreeMargin()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountFreeMargin";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double AccountFreeMarginCheck(String symbol, int cmd, double volume)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountFreeMarginCheck";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(cmd);
        funcInfo.Parameter[2].SetData(volume);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double AccountFreeMarginMode()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountFreeMarginMode";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public int AccountLeverage()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountLeverage";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public double AccountMargin()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountMargin";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String AccountName()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountName";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }    
    public int AccountNumber()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountNumber";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public double AccountProfit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountProfit";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String AccountServer()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountServer";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public int AccountStopoutLevel()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountStopoutLevel";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int AccountStopoutMode()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "AccountStopoutMode";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public boolean IsStopped()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsStopped";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public int UninitializeReason()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "UninitializeReason";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    public int TerminalInfoInteger(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TerminalInfoInteger";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public double TerminalInfoDouble(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TerminalInfoDouble";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String TerminalInfoString(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TerminalInfoString";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public int MQLInfoInteger(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "MQLInfoInteger";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public String MQLInfoString(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "MQLInfoString";
        funcInfo.Parameter[0].SetData(property_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public void MQLSetInteger(int property_id, int property_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "MQLSetInteger";
        funcInfo.Parameter[0].SetData(property_id);
        funcInfo.Parameter[1].SetData(property_value);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return;
    }
    
    public String Symbol()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "Symbol";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataString();
    }
    public int Period()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "Period";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int Digits()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "Digits";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public double Point()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "Point";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public boolean IsConnected()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsConnected";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean IsDemo()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsDemo";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean IsDllsAllowed()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsDllsAllowed";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean IsExpertEnabled()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsExpertEnabled";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean IsLibrariesAllowed()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsLibrariesAllowed";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }

    public boolean IsOptimization()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsOptimization";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean IsTesting()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsTesting";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean IsTradeAllowed()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsTradeAllowed";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean IsTradeContextBusy()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsTradeContextBusy";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean IsVisualMode()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "IsVisualMode";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }

    public String TerminalCompany()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TerminalCompany";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataString();
    }
    
    public String TerminalName()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TerminalName";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataString();
    }
    
    public String TerminalPath()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TerminalPath";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataString();
    }
    
    public double MarketInfo(String symbol, int type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "MarketInfo";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public int SymbolsTotal(boolean selected)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolsTotal";
        funcInfo.Parameter[0].SetData(selected);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public String SymbolName(int pos, boolean selected)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolName";
        funcInfo.Parameter[0].SetData(pos);
        funcInfo.Parameter[1].SetData(selected);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public boolean SymbolSelect(String name, boolean select)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolSelect";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(select);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public long SymbolInfoInteger(String name, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolInfoInteger";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(prop_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataLong();
    }
    public double SymbolInfoDouble(String name, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolInfoDouble";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(prop_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String SymbolInfoString(String name, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolInfoString";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(prop_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    
    public boolean SymbolInfoTick(String symbol, MqlTick mqlTick)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolInfoTick";
        
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(mqlTick.ask);
        funcInfo.Parameter[2].SetData(mqlTick.bid);
        funcInfo.Parameter[3].SetData(mqlTick.flags);
        funcInfo.Parameter[4].SetData(mqlTick.last);
        funcInfo.Parameter[5].SetData(mqlTick.time);
        funcInfo.Parameter[6].SetData(mqlTick.time_msc);
        funcInfo.Parameter[7].SetData(mqlTick.volume);
        funcInfo.Parameter[8].SetData(mqlTick.volume_real);
        
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        
        mqlTick.ask = funcInfo.Auxiliary[0].GetDataDouble();
        mqlTick.bid = funcInfo.Auxiliary[1].GetDataDouble();
        mqlTick.flags = funcInfo.Auxiliary[2].GetDataInt();
        mqlTick.last = funcInfo.Auxiliary[3].GetDataDouble();
        mqlTick.time = funcInfo.Auxiliary[4].GetDataDateTime();
        mqlTick.time_msc = funcInfo.Auxiliary[5].GetDataLong();
        mqlTick.volume = funcInfo.Auxiliary[6].GetDataLong();
        //mqlTick.volume_real = funcInfo.Auxiliary[7].GetDataDouble();
        
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean SymbolInfoSessionQuote(String name, int day_of_week, int session_index, LocalDateTime from, LocalDateTime to)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolInfoSessionQuote";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(day_of_week);
        funcInfo.Parameter[2].SetData(session_index);
        funcInfo.Parameter[3].SetData(from);
        funcInfo.Parameter[4].SetData(to);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean SymbolInfoSessionTrade(String name, int day_of_week, int session_index, LocalDateTime from, LocalDateTime to)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SymbolInfoSessionTrade";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(day_of_week);
        funcInfo.Parameter[2].SetData(session_index);
        funcInfo.Parameter[3].SetData(from);
        funcInfo.Parameter[4].SetData(to);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    //  時系列・インジケータアクセス
    public long SeriesInfoInteger(String symbol_name, int timeframe, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "SeriesInfoInteger";
        funcInfo.Parameter[0].SetData(symbol_name);
        funcInfo.Parameter[1].SetData(timeframe);
        funcInfo.Parameter[2].SetData(prop_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataLong();
    }
    
    public boolean RefreshRates()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "RefreshRates";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    //  算術関数
    //  Java内部の算術関数を使用してください。
    
    //  チャート操作
    //  チャート操作系の関数はこのプログラムの目的とはあまり関係ないので実装しません。
        
    //  取引関数
    public int OrdersHistoryTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrdersHistoryTotal";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int OrdersTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrdersTotal";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    public boolean OrderSelect(int index, int select, int pool)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSelect";
        funcInfo.Parameter[0].SetData(index);
        funcInfo.Parameter[1].SetData(select);
        funcInfo.Parameter[2].SetData(pool);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSend";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(cmd);
        funcInfo.Parameter[2].SetData(volume);
        funcInfo.Parameter[3].SetData(price);
        funcInfo.Parameter[4].SetData(slippage);
        funcInfo.Parameter[5].SetData(stoploss);
        funcInfo.Parameter[6].SetData(takeprofit);
        funcInfo.Parameter[7].SetData("\0");
        funcInfo.Parameter[8].SetData(0);
        funcInfo.Parameter[9].SetData((long)0);
        funcInfo.Parameter[10].SetData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSend";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(cmd);
        funcInfo.Parameter[2].SetData(volume);
        funcInfo.Parameter[3].SetData(price);
        funcInfo.Parameter[4].SetData(slippage);
        funcInfo.Parameter[5].SetData(stoploss);
        funcInfo.Parameter[6].SetData(takeprofit);
        funcInfo.Parameter[7].SetData(comment);
        funcInfo.Parameter[8].SetData(0);
        funcInfo.Parameter[9].SetData((long)0);
        funcInfo.Parameter[10].SetData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment, int magic)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSend";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(cmd);
        funcInfo.Parameter[2].SetData(volume);
        funcInfo.Parameter[3].SetData(price);
        funcInfo.Parameter[4].SetData(slippage);
        funcInfo.Parameter[5].SetData(stoploss);
        funcInfo.Parameter[6].SetData(takeprofit);
        funcInfo.Parameter[7].SetData(comment);
        funcInfo.Parameter[8].SetData(magic);
        funcInfo.Parameter[9].SetData((long)0);
        funcInfo.Parameter[10].SetData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment, int magic, LocalDateTime expiration)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSend";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(cmd);
        funcInfo.Parameter[2].SetData(volume);
        funcInfo.Parameter[3].SetData(price);
        funcInfo.Parameter[4].SetData(slippage);
        funcInfo.Parameter[5].SetData(stoploss);
        funcInfo.Parameter[6].SetData(takeprofit);
        funcInfo.Parameter[7].SetData(comment);
        funcInfo.Parameter[8].SetData(magic);
        funcInfo.Parameter[9].SetData(expiration);
        funcInfo.Parameter[10].SetData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment, int magic, LocalDateTime expiration, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSend";
        funcInfo.Parameter[0].SetData(symbol);
        funcInfo.Parameter[1].SetData(cmd);
        funcInfo.Parameter[2].SetData(volume);
        funcInfo.Parameter[3].SetData(price);
        funcInfo.Parameter[4].SetData(slippage);
        funcInfo.Parameter[5].SetData(stoploss);
        funcInfo.Parameter[6].SetData(takeprofit);
        funcInfo.Parameter[7].SetData(comment);
        funcInfo.Parameter[8].SetData(magic);
        funcInfo.Parameter[9].SetData(expiration);
        funcInfo.Parameter[10].SetData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public boolean OrderClose(int ticket, double lots, double price, int slippage, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderClose";
        funcInfo.Parameter[0].SetData(ticket);
        funcInfo.Parameter[1].SetData(lots);
        funcInfo.Parameter[2].SetData(price);
        funcInfo.Parameter[3].SetData(slippage);
        funcInfo.Parameter[4].SetData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean OrderCloseBy(int ticket, int opposite, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderCloseBy";
        funcInfo.Parameter[0].SetData(ticket);
        funcInfo.Parameter[1].SetData(opposite);
        funcInfo.Parameter[2].SetData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean OrderModify(int ticket, double price, double stoploss, double takeprofit, LocalDateTime expiration, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderModify";
        funcInfo.Parameter[0].SetData(ticket);
        funcInfo.Parameter[1].SetData(price);
        funcInfo.Parameter[2].SetData(stoploss);
        funcInfo.Parameter[3].SetData(takeprofit);
        funcInfo.Parameter[4].SetData(expiration);
        funcInfo.Parameter[5].SetData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean OrderDelete(int ticket, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderDelete";
        funcInfo.Parameter[0].SetData(ticket);
        funcInfo.Parameter[1].SetData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public void OrderPrint()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderPrint";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return ;
    }
    public int OrderTicket()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderTicket";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    public LocalDateTime OrderOpenTime()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderOpenTime";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public double OrderOpenPrice()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderOpenPrice";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public int OrderType()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderType";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public double OrderLots()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderLots";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String OrderSymbol()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSymbol";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public double OrderStopLoss()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderStopLoss";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double OrderTakeProfit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderTakeProfit";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public LocalDateTime OrderCloseTime()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderCloseTime";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public double OrderClosePrice()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderClosePrice";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double OrderCommission()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderCommission";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public LocalDateTime OrderExpiration()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderExpiration";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public double OrderSwap()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderSwap";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double OrderProfit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderProfit";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String OrderComment()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderComment";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public int OrderMagicNumber()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "OrderMagicNumber";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    
    //  トレードシグナル
    //  このプログラムはインジケータの開発を目的としたものではないためトレードシグナル系の関数の実装はしません。
    
    //   クライアントターミナルのグローバル変数
    public boolean GlobalVariableCheck(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableCheck";
        funcInfo.Parameter[0].SetData(name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public LocalDateTime GlobalVariableTime(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableTime";
        funcInfo.Parameter[0].SetData(name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public boolean GlobalVariableDel(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableDel";
        funcInfo.Parameter[0].SetData(name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public double GlobalVariableGet(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableGet";
        funcInfo.Parameter[0].SetData(name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String GlobalVariableName(int index)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableName";
        funcInfo.Parameter[0].SetData(index);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public LocalDateTime GlobalVariableSet(String name, double value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableSet";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(value);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public void GlobalVariablesFlush()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariablesFlush";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return;
    }
    public boolean GlobalVariableTemp(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableTemp";
        funcInfo.Parameter[0].SetData(name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean GlobalVariableSetOnCondition(String name, double value, double check_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariableSetOnCondition";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(value);
        funcInfo.Parameter[2].SetData(check_value);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public int GlobalVariablesDeleteAll()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariablesDeleteAll1";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GlobalVariablesDeleteAll(String prefix_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariablesDeleteAll2";
        funcInfo.Parameter[0].SetData(prefix_name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GlobalVariablesDeleteAll(String prefix_name, LocalDateTime limit_data)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariablesDeleteAll3";
        funcInfo.Parameter[0].SetData(prefix_name);
        funcInfo.Parameter[1].SetData(limit_data);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int GlobalVariablesTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "GlobalVariablesTotal";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    //  オブジェクト関数
    public boolean ObjectCreate(long chart_id, String object_name, int object_type, int sub_window, LocalDateTime time1, double price1)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectCreate1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(object_type);
        funcInfo.Parameter[3].SetData(sub_window);
        funcInfo.Parameter[4].SetData(time1);
        funcInfo.Parameter[5].SetData(price1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectCreate(long chart_id, String object_name, int object_type, int sub_window, LocalDateTime time1, double price1, LocalDateTime time2, double price2)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectCreate2";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(object_type);
        funcInfo.Parameter[3].SetData(sub_window);
        funcInfo.Parameter[4].SetData(time1);
        funcInfo.Parameter[5].SetData(price1);
        funcInfo.Parameter[6].SetData(time2);
        funcInfo.Parameter[7].SetData(price2);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectCreate(long chart_id, String object_name, int object_type, int sub_window, LocalDateTime time1, double price1, LocalDateTime time2, double price2, LocalDateTime time3, double price3)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectCreate3";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(object_type);
        funcInfo.Parameter[3].SetData(sub_window);
        funcInfo.Parameter[4].SetData(time1);
        funcInfo.Parameter[5].SetData(price1);
        funcInfo.Parameter[6].SetData(time2);
        funcInfo.Parameter[7].SetData(price2);
        funcInfo.Parameter[8].SetData(time2);
        funcInfo.Parameter[9].SetData(price2);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public String ObjectName(long chart_id, int object_index, int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectName1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_index);
        funcInfo.Parameter[2].SetData(sub_window);
        funcInfo.Parameter[3].SetData(object_type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public String ObjectName(long chart_id, int object_index)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectName1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_index);
        funcInfo.Parameter[2].SetData(-1);
        funcInfo.Parameter[3].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public String ObjectName(long chart_id, int object_index, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectName1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_index);
        funcInfo.Parameter[2].SetData(sub_window);
        funcInfo.Parameter[3].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }    
    public String ObjectName(int object_index)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectName2";
        funcInfo.Parameter[0].SetData(object_index);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }
    public boolean ObjectDelete(String object_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectDelete1";
        funcInfo.Parameter[0].SetData(object_name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectDelete(long chart_id, String object_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectDelete2";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    
    public boolean ObjectsDeleteAll(long chart_id, int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(sub_window);
        funcInfo.Parameter[2].SetData(object_type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }    
    public boolean ObjectsDeleteAll(long chart_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(-1);
        funcInfo.Parameter[2].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(sub_window);
        funcInfo.Parameter[2].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, String prefix, int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll2";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(prefix);
        funcInfo.Parameter[2].SetData(sub_window);
        funcInfo.Parameter[3].SetData(object_type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, String prefix)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll2";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(prefix);
        funcInfo.Parameter[2].SetData(-1);
        funcInfo.Parameter[3].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, String prefix, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll2";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(prefix);
        funcInfo.Parameter[2].SetData(sub_window);
        funcInfo.Parameter[3].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll(int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll3";
        funcInfo.Parameter[0].SetData(sub_window);
        funcInfo.Parameter[1].SetData(object_type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll3";
        funcInfo.Parameter[0].SetData(-1);
        funcInfo.Parameter[1].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectsDeleteAll(int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsDeleteAll3";
        funcInfo.Parameter[0].SetData(sub_window);
        funcInfo.Parameter[1].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    int ObjectFind(long chart_id, String object_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectFind1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }    
    int ObjectFind(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectFind2";
        funcInfo.Parameter[0].SetData(name);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public LocalDateTime ObjectGetTimeByValue(long chart_id, String object_name, double value, int line_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetTimeByValue";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(value);
        funcInfo.Parameter[3].SetData(line_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public LocalDateTime ObjectGetTimeByValue(long chart_id, String object_name, double value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetTimeByValue";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(value);
        funcInfo.Parameter[3].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDateTime();
    }
    public double ObjectGetValueByShift(String name, int shift)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetValueByShift";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(shift);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double ObjectGetValueByTime(long chart_id, String object_name, LocalDateTime time, int line_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetValueByShift";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(time);
        funcInfo.Parameter[3].SetData(line_id);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public double ObjectGetValueByTime(long chart_id, String object_name, LocalDateTime time)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetValueByShift";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(time);
        funcInfo.Parameter[3].SetData(0);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public boolean ObjectMove(long chart_id, String object_name, int point_index, LocalDateTime time, double price)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectMove1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(point_index);
        funcInfo.Parameter[3].SetData(time);
        funcInfo.Parameter[4].SetData(price);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectMove(String name, int point_index, LocalDateTime time1, double price1)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectMove2";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(point_index);
        funcInfo.Parameter[2].SetData(time1);
        funcInfo.Parameter[3].SetData(price1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public int ObjectsTotal(long chart_id, int sub_window, int type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsTotal1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(sub_window);
        funcInfo.Parameter[2].SetData(type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int ObjectsTotal(long chart_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsTotal1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(-1);
        funcInfo.Parameter[2].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int ObjectsTotal(long chart_id, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsTotal1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(sub_window);
        funcInfo.Parameter[2].SetData(-1);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int ObjectsTotal(int type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsTotal2";
        funcInfo.Parameter[0].SetData(type);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public int ObjectsTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectsTotal2";
        funcInfo.Parameter[0].SetData(ObjectInfo.ObjectType.EMPTY);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return funcInfo.ReturnValue.GetDataInt();
    }
    public long ObjectGetInteger(long chart_id, String object_name, int prop_id, int prop_modifier)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetInteger1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(prop_id);
        funcInfo.Parameter[3].SetData(prop_modifier);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataLong();
    }
    public double ObjectGetDouble(long chart_id, String object_name, int prop_id, int prop_modifier)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetDouble1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(prop_id);
        funcInfo.Parameter[3].SetData(prop_modifier);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataDouble();
    }
    public String ObjectGetString(long chart_id, String object_name, int prop_id, int prop_modifier)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectGetString1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(prop_id);
        funcInfo.Parameter[3].SetData(prop_modifier);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataString();
    }        
    public boolean ObjectSetInteger(long chart_id, String object_name, int prop_id, long prop_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectSetInteger1";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(prop_id);
        funcInfo.Parameter[3].SetData(prop_value);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectSetDouble(long chart_id, String object_name, int prop_id, double prop_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectSetDouble";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(prop_id);
        funcInfo.Parameter[3].SetData(prop_value);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean ObjectSetString(long chart_id, String object_name, int prop_id, String prop_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ObjectSetString";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(object_name);
        funcInfo.Parameter[2].SetData(prop_id);
        funcInfo.Parameter[3].SetData(prop_value);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean TextSetFont(String name, int size, int flags, int orientation)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "TextSetFont";
        funcInfo.Parameter[0].SetData(name);
        funcInfo.Parameter[1].SetData(size);
        funcInfo.Parameter[2].SetData(flags);
        funcInfo.Parameter[3].SetData(orientation);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    //  イベント操作
    public boolean EventSetMillisecondTimer(int milliseconds)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "EventSetMillisecondTimer";
        funcInfo.Parameter[0].SetData(milliseconds);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public boolean EventSetTimer(int seconds)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "EventSetTimer";
        funcInfo.Parameter[0].SetData(seconds);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public void EventKillTimer()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "EventKillTimer";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return;
    }
    public boolean EventChartCustom(long chart_id, short custom_event_id, long lparam, double dparam, String sparam)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "EventChartCustom";
        funcInfo.Parameter[0].SetData(chart_id);
        funcInfo.Parameter[1].SetData(custom_event_id);
        funcInfo.Parameter[2].SetData(lparam);
        funcInfo.Parameter[3].SetData(dparam);
        funcInfo.Parameter[4].SetData(sparam);
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        LastError = funcInfo.ErrorCode;
        return funcInfo.ReturnValue.GetDataBool();
    }
    public void ExpertRemove()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.FuncName = "ExpertRemove";
        if (!SendReceiveRequest(funcInfo)) FlagEmergencyStop = true;
        return;
    }
    //  MT4の関数はここまで
    
    //  指定した関数を呼び出す
    //  サーバー側と違いこの関数が呼び出せるのはグローバル関数のみ
    public void CallFunc(FuncInfo funcInfo)
    {
        if (MT4StringCompare(funcInfo.FuncName, "OnPreInit"))
        {
            boolean debug = funcInfo.Parameter[0].GetDataDouble() != 0 ? true : false;
            OnPreInit(debug);
        }
        else
        {
            Ask = funcInfo.Parameter[0].GetDataDouble();
            Bid = funcInfo.Parameter[1].GetDataDouble();
            Volume = funcInfo.Parameter[7].GetDataLong();
            Bars = funcInfo.Parameter[8].GetDataInt();
            Digits = funcInfo.Parameter[9].GetDataInt();
            Point = funcInfo.Parameter[10].GetDataInt();

            if (MT4StringCompare(funcInfo.FuncName, "OnInit"))
            {
                funcInfo.ReturnValue.SetData(
                    OnInit()
                );
            }
            else if (MT4StringCompare(funcInfo.FuncName, "OnTick"))
            {
                OnTick();
            }
            else if (MT4StringCompare(funcInfo.FuncName, "OnTimer"))
            {
                OnTimer();
            }
            else if (MT4StringCompare(funcInfo.FuncName, "OnDeinit"))
            {
                OnDeinit(funcInfo.Parameter[0].GetDataInt());
            }
            else if (MT4StringCompare(funcInfo.FuncName, "OnTimerInternal"))
            {
                OnTimerInternal();
            }
            else
            {
                System.err.printf("不明な関数が呼び出されました'%s'。\n", funcInfo.FuncName);
            }            
        }        
        return;
    }
    
    public void CallExpertRemove()
    {
        //  クライアント側なので何もしない。ソース共通化のため残している
        return;
    }
    
    boolean MT4StringCompare(String a, String b)
    {
        return (a.compareTo(b) == 0);
    }
    
    //  リクエスト情報の送受信
    public boolean SendReceiveRequest(FuncInfo funcInfo)
    {
        PrintDebugMessage("SendReceiveRequestが呼ばれました。");
        int PosParameter = 0;
        int PosAuxiliary = 0;
        Message s = new Message();
        Message r = new Message();
        FuncInfo funcInfoReceived = new FuncInfo();
        
        if (funcInfo.FuncName != null)
        {
            //  呼び出す関数が決まっている
            s.SetMessageType(Message.MSG_REQUEST_CALL_FUNCTION);
            s.Data[1].SetData(funcInfo.FuncName);
        }
        else
        {
            //  呼び出す関数が決まっていない（最初だけ何も要求を送らない）
            s.SetMessageType(Message.MSG_NOP);
        }
        
        do
        {
            PrintDebugMessage("リクエスト情報を送受信するためのループ");
            s.SetEmergencyStop(FlagEmergencyStop ? (char)1 : (char)0);
            if (!Pipe.SendMessage(s))
            {
                System.err.println("メッセージの送信に失敗しました");
                s = null;
                r = null;
                funcInfoReceived = null;
                return false;
            }
            PrintDebugMessage(String.format("メッセージを送信しました。メッセージタイプは%X", s.GetMessageType()));
            
            if (funcInfoReceived.FuncName != null && 
                MT4StringCompare(funcInfoReceived.FuncName, "OnDeinit") && 
                s.GetMessageType() == Message.MSG_AUXILIARY_END)
            {
                 //  OnDeinitの呼び出し依頼、かつ補助情報の送信が終わった場合
                 break;   //  この関数の無限ループを抜ける
            }

            //  ----  ここまでで関数呼び出し依頼は完了している  ----
            
            r.Clear();
            if (!Pipe.ReceiveMessage(r))
            {
                System.err.println("メッセージの受信に失敗しました");
                s = null;
                r = null;
                funcInfoReceived = null;
                return false;
            }
            PrintDebugMessage(String.format("メッセージを受信しました。メッセージタイプは%X", r.GetMessageType()));            
            
            if (r.GetMessageType()==Message.MSG_NULL)
            {
                System.err.println("空のメッセージを受信しました。緊急停止します");
                s = null;
                r = null;
                funcInfoReceived = null;
                return false;
            }
            
            //  緊急終了フラグの確認
            if(r.GetEmergencyStop()!=0)
            {
                CallExpertRemove();
            }
            
            //  先方からのメッセージを処理する
            switch (r.GetMessageType())
            {
                //   関数呼び出しの結果
                //  関数呼び出し依頼をした場合
                case Message.MSG_REQUEST_PARAMETER:
                {
                    PrintDebugMessage("MSG_REQUEST_PARAMETERを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_PARAMETER);
                    funcInfo.Parameter[PosParameter].CopyDataGramTo(s.Data[1]);
                    if (PosParameter >= funcInfo.GetNumberOfParameters())
                    {
                        s.SetMessageType(Message.MSG_PARAMETER_END);
                    }
                    PosParameter++;
                    break;
                }
                case Message.MSG_RETURN_VALUE:
                {
                    PrintDebugMessage("MSG_RETURN_VALUEを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_REQUEST_ERROR_CODE);
                    r.Data[1].CopyDataGramTo(funcInfo.ReturnValue);
                    break;            
                }
                case Message.MSG_ERROR_CODE:
                {
                    PrintDebugMessage("MSG_ERROR_CODEを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_REQUEST_AUXILIARY);
                    funcInfo.ErrorCode = r.Data[1].GetDataInt();
                    PosAuxiliary = 0;
                    break;            
                }
                case Message.MSG_AUXILIARY:
                case Message.MSG_AUXILIARY_END:
                {
                    PrintDebugMessage("MSG_AUXILIARYまたはMSG_AUXILIARY_ENDを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_REQUEST_AUXILIARY);
                    r.Data[1].CopyDataGramTo(funcInfo.Auxiliary[PosAuxiliary]);
                    PosAuxiliary++;
                    break;            
                }                
                //  関数呼び出し依頼
                case Message.MSG_REQUEST_CALL_FUNCTION:
                {
                    PrintDebugMessage("MSG_CALL_FUNCTIONを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_REQUEST_PARAMETER);
                    funcInfoReceived.Clear();
                    funcInfoReceived.FuncName = r.Data[1].GetDataString();
                    PosParameter = 0;
                    break;
                }
                case Message.MSG_PARAMETER:
                {
                    PrintDebugMessage("MSG_PARAMETERを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_REQUEST_PARAMETER);
                    r.Data[1].CopyDataGramTo(funcInfoReceived.Parameter[PosParameter]);
                    PosParameter++;
                    break;
                }
                case Message.MSG_PARAMETER_END:
                {
                    PrintDebugMessage("MSG_PARAMETER_ENDを受信しました");
                    r.Data[1].CopyDataGramTo(funcInfoReceived.Parameter[PosParameter]);
                    PosParameter++;

                    //  関数実行
                    CallFunc(funcInfoReceived);

                    //  関数呼び出し後
                    s.Clear();
                    s.SetMessageType(Message.MSG_RETURN_VALUE);
                    funcInfoReceived.ReturnValue.CopyDataGramTo(s.Data[1]);
                    break;
                }
                case Message.MSG_REQUEST_ERROR_CODE:
                {
                    PrintDebugMessage("MSG_REQUEST_ERROR_CODEを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_ERROR_CODE);
                    s.Data[1].SetData(funcInfoReceived.ErrorCode);
                    break;
                }
                case Message.MSG_REQUEST_AUXILIARY:
                {
                    PrintDebugMessage("MSG_REQUEST_AUXILIARYを受信しました");
                    s.Clear();
                    s.SetMessageType(Message.MSG_AUXILIARY);
                    funcInfoReceived.Auxiliary[PosAuxiliary].CopyDataGramTo(s.Data[1]);
                    if (PosAuxiliary >= funcInfoReceived.GetNumberOfAuxiliaries())
                    {
                        s.SetMessageType(Message.MSG_AUXILIARY_END);
                    }
                    PosAuxiliary++;
                    break;
                }
            }
        }
        while (r.GetMessageType()!=Message.MSG_AUXILIARY_END);
        
        s = null;
        r = null;
        funcInfoReceived = null;
        PrintDebugMessage("リクエスト情報の送受信を行いました。");
        return true;
    }
    
    //  シャットダウンシーケンス
    Thread shutdownSequence1 = new Thread()
    {
        @Override
        public void run()
        {
            System.out.println("Ctrl+Cが押されました。サーバにOnDeinitの呼出依頼を発行します。");
            try
            {
                synchronized (Pipe)
                {
                    FlagEmergencyStop = true;
                    Pipe.wait();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return;
        }
    };
    
    //  コンストラクタ
    public Connector()
    {
        Pipe = new PipeClient();
        return;
    }
    
    //  サーバーへの接続を行う
    public final void ConnectToMT4(String PipeName)
    {
        PrintDebugMessage("サーバへ接続します。");
        try
        {
            Pipe.ConnectToServer(PipeName);

        }
        catch (FileNotFoundException e)
        {
            System.err.printf("指定された名前付きパイプ'%s'が見つかりません。\n", PipeName);
            return;                
        }
        Runtime.getRuntime().addShutdownHook(shutdownSequence1);
        FuncInfo fi = new FuncInfo();
        fi.FuncName = null;
        SendReceiveRequest(fi);
        PrintDebugMessage("リクエストの送受信がすべて終わりました。プログラムを終了します。");
        
        try
        {
            synchronized (Pipe)
            {
                Pipe.notify();
                Runtime.getRuntime().removeShutdownHook(shutdownSequence1);            
            }
        }
        catch (IllegalStateException e)
        {
            //  シャットダウン中だった場合。特に何もしない
        }
        
        Pipe.Close();
        PrintDebugMessage("サーバーとの接続を切断します。");
        return;
    }
}
