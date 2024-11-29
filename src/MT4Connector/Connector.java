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
    protected PipeClient pipe;  //  名前付きパイプの実体
    protected boolean flagEmergencyStop = false;  //  システムを停止する必要が生じたとき。このフラグが立ったときはサーバー側にクライアント側のOnDeinit呼び出しを依頼する
        protected HashMap<String/*通貨ペア*/, HashMap<Integer/*時間足*/, ArrayList<Candle>>> tickData = new HashMap<>(50);  //  全シンボル全時間足のローソク足をロードするためのリスト
    //  ！このTickDataのしくみは改善事項。実行速度が遅すぎて実用的ではない。    
    public final static int[] timeFrames = MT4Runtime.TimeFrame.MT4TimeFrames; //  対応している時間足の一覧
    protected boolean debugMode = false;    //  デバッグモードの有無、毎回MT4側にデバッグモード指定の有無を確認するのはオーバーヘッドが大きいので自前の変数で持つ
    
    //  コンストラクタ
    public Connector()
    {
        pipe = new PipeClient();
    }
    
    //  MT4の関数に対応する関数。これは継承先のクラスで実装してもらう
    public abstract int OnInit();
    public abstract void OnTick();
    public void OnTimer(){}
    public abstract void OnDeinit(final int reason);
    //  OnInitが呼ばれる前に内部的に実行される。プラットフォームやサーバに関する情報をやり取りするための関数（将来の機能拡張用）
    public final void OnPreInit(boolean debug)
    {
        debugMode = debug;  //  デバッグモードの設定
        printDebugMessage("OnPreInitが呼ばれました。");
        printDebugMessage("OnPreInitを抜けます。");
    }    
    //  内部的に呼び出されるタイマー。他の通貨ペアの情報などを裏方で送受信するため（将来の機能拡張用）
    public final void OnTimerInternal()
    {
        printDebugMessage("OnTimerInternalが呼ばれました。");
        printDebugMessage("OnTimerInternalを抜けます。");
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
    protected void printDebugMessage(String message)
    {
        if (debugMode) System.err.println(message);
    }
    //  TickDataリストの初期化
    public void InitChartList()
    {
        //  初期化
        for (int l = 0; l < SymbolsTotal(false); l++)
        {
            String cp = SymbolName(l, false);
            tickData.put(cp, new HashMap<Integer, ArrayList<Candle>>(21));
            for (int t : timeFrames)
            {
                tickData.get(cp).put(t, new ArrayList<Candle>(10000));
            }
        }
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
                tickData.get(currencyPair).get(tf).add(new Candle(aOpen[a], aHigh[a], aLow[a], aClose[a], aVolume[a], aTime[a]));
            }
        }
    }
    //  MT4MT5側の独自追加関数を呼び出す
    public int GetCandlesOpen(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "CopyOpen";
        funcInfo.parameter[0].setData(symbolName);
        funcInfo.parameter[1].setData(timeFrame);
        funcInfo.parameter[2].setData(barFrom);
        funcInfo.parameter[3].setData(barTo - barFrom);
        funcInfo.parameter[4].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        for (int i = 0; i < funcInfo.getNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.auxiliary[i].getDataDouble();
        }
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int GetCandlesHigh(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "CopyHigh";
        funcInfo.parameter[0].setData(symbolName);
        funcInfo.parameter[1].setData(timeFrame);
        funcInfo.parameter[2].setData(barFrom);
        funcInfo.parameter[3].setData(barTo - barFrom);
        funcInfo.parameter[4].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        for (int i = 0; i < funcInfo.getNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.auxiliary[i].getDataDouble();
        }
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int GetCandlesLow(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "CopyLow";
        funcInfo.parameter[0].setData(symbolName);
        funcInfo.parameter[1].setData(timeFrame);
        funcInfo.parameter[2].setData(barFrom);
        funcInfo.parameter[3].setData(barTo - barFrom);
        funcInfo.parameter[4].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        for (int i = 0; i < funcInfo.getNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.auxiliary[i].getDataDouble();
        }
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int GetCandlesClose(String symbolName, int timeFrame, double[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "CopyClose";
        funcInfo.parameter[0].setData(symbolName);
        funcInfo.parameter[1].setData(timeFrame);
        funcInfo.parameter[2].setData(barFrom);
        funcInfo.parameter[3].setData(barTo - barFrom);
        funcInfo.parameter[4].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        for (int i = 0; i < funcInfo.getNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.auxiliary[i].getDataDouble();
        }
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int GetCandlesVolume(String symbolName, int timeFrame, long[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "CopyTickVolume";
        funcInfo.parameter[0].setData(symbolName);
        funcInfo.parameter[1].setData(timeFrame);
        funcInfo.parameter[2].setData(barFrom);
        funcInfo.parameter[3].setData(barTo - barFrom);
        funcInfo.parameter[4].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        for (int i = 0; i < funcInfo.getNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.auxiliary[i].getDataLong();
        }
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int GetCandlesTime(String symbolName, int timeFrame, LocalDateTime[] data, int barFrom, int barTo)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "CopyTime";
        funcInfo.parameter[0].setData(symbolName);
        funcInfo.parameter[1].setData(timeFrame);
        funcInfo.parameter[2].setData(barFrom);
        funcInfo.parameter[3].setData(barTo - barFrom);
        funcInfo.parameter[4].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        for (int i = 0; i < funcInfo.getNumberOfAuxiliaries(); i++)
        {
            data[i] = funcInfo.auxiliary[i].getDataDateTime();
        }
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
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
        funcInfo.funcName = "GetPlatformVersion";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    
    //  リソース
    public boolean PlaySound(String FileName)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "PlaySound";
        funcInfo.parameter[0].setData(FileName);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        //LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();        
    }
    //  共通関数
    //void Alert(argument...)  //  ！未サポート。使わなさそうだから今は実装しない
    
    //ENUM_POINTER_TYPE  CheckPointer(object* anyobject);  //  クライアントから呼び出す必要がないので実装しない
    
    public int iBars(String CurrencyPair, int TimeFrame)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "iBars";
        funcInfo.parameter[0].setData(CurrencyPair);
        funcInfo.parameter[1].setData(TimeFrame);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    
    public LocalDateTime TimeCurrent()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TimeCurrent";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public LocalDateTime TimeLocal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TimeLocal";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public LocalDateTime TimeGMT()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TimeGMT";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public int TimeDaylightSavings()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TimeDaylightSavings";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public double AccountInfoDouble(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountInfoDouble";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public long AccountInfoInteger(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountInfoInteger";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataLong();
    }
    public String AccountInfoString(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountInfoString";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }

    public double AccountBalance()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountBalance";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    
    public double AccountCredit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountCredit";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    
    public String AccountCompany()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountCompany";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    
    public String AccountCurrency()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountCurrency";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public double AccountEquity()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountEquity";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }

    public double AccountFreeMargin()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountFreeMargin";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public double AccountFreeMarginCheck(String symbol, int cmd, double volume)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountFreeMarginCheck";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(cmd);
        funcInfo.parameter[2].setData(volume);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public double AccountFreeMarginMode()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountFreeMarginMode";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public int AccountLeverage()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountLeverage";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public double AccountMargin()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountMargin";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String AccountName()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountName";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }    
    public int AccountNumber()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountNumber";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public double AccountProfit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountProfit";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String AccountServer()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountServer";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public int AccountStopoutLevel()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountStopoutLevel";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int AccountStopoutMode()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "AccountStopoutMode";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public boolean IsStopped()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsStopped";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public int UninitializeReason()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "UninitializeReason";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    
    public int TerminalInfoInteger(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TerminalInfoInteger";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public double TerminalInfoDouble(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TerminalInfoDouble";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String TerminalInfoString(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TerminalInfoString";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public int MQLInfoInteger(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "MQLInfoInteger";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public String MQLInfoString(int property_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "MQLInfoString";
        funcInfo.parameter[0].setData(property_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public void MQLSetInteger(int property_id, int property_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "MQLSetInteger";
        funcInfo.parameter[0].setData(property_id);
        funcInfo.parameter[1].setData(property_value);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
    }
    
    public String Symbol()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "Symbol";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataString();
    }
    public int Period()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "Period";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int Digits()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "Digits";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public double Point()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "Point";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDouble();
    }
    public boolean IsConnected()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsConnected";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean IsDemo()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsDemo";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean IsDllsAllowed()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsDllsAllowed";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean IsExpertEnabled()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsExpertEnabled";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean IsLibrariesAllowed()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsLibrariesAllowed";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }

    public boolean IsOptimization()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsOptimization";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean IsTesting()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsTesting";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean IsTradeAllowed()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsTradeAllowed";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean IsTradeContextBusy()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsTradeContextBusy";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean IsVisualMode()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "IsVisualMode";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }

    public String TerminalCompany()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TerminalCompany";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataString();
    }
    
    public String TerminalName()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TerminalName";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataString();
    }
    
    public String TerminalPath()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TerminalPath";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataString();
    }
    
    public double MarketInfo(String symbol, int type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "MarketInfo";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDouble();
    }
    public int SymbolsTotal(boolean selected)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolsTotal";
        funcInfo.parameter[0].setData(selected);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public String SymbolName(int pos, boolean selected)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolName";
        funcInfo.parameter[0].setData(pos);
        funcInfo.parameter[1].setData(selected);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public boolean SymbolSelect(String name, boolean select)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolSelect";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(select);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public long SymbolInfoInteger(String name, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolInfoInteger";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(prop_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataLong();
    }
    public double SymbolInfoDouble(String name, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolInfoDouble";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(prop_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String SymbolInfoString(String name, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolInfoString";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(prop_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    
    public boolean SymbolInfoTick(String symbol, MqlTick mqlTick)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolInfoTick";
        
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(mqlTick.ask);
        funcInfo.parameter[2].setData(mqlTick.bid);
        funcInfo.parameter[3].setData(mqlTick.flags);
        funcInfo.parameter[4].setData(mqlTick.last);
        funcInfo.parameter[5].setData(mqlTick.time);
        funcInfo.parameter[6].setData(mqlTick.time_msc);
        funcInfo.parameter[7].setData(mqlTick.volume);
        funcInfo.parameter[8].setData(mqlTick.volume_real);
        
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        
        mqlTick.ask = funcInfo.auxiliary[0].getDataDouble();
        mqlTick.bid = funcInfo.auxiliary[1].getDataDouble();
        mqlTick.flags = funcInfo.auxiliary[2].getDataInt();
        mqlTick.last = funcInfo.auxiliary[3].getDataDouble();
        mqlTick.time = funcInfo.auxiliary[4].getDataDateTime();
        mqlTick.time_msc = funcInfo.auxiliary[5].getDataLong();
        mqlTick.volume = funcInfo.auxiliary[6].getDataLong();
        //mqlTick.volume_real = funcInfo.auxiliary[7].getDataDouble();
        
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean SymbolInfoSessionQuote(String name, int day_of_week, int session_index, LocalDateTime from, LocalDateTime to)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolInfoSessionQuote";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(day_of_week);
        funcInfo.parameter[2].setData(session_index);
        funcInfo.parameter[3].setData(from);
        funcInfo.parameter[4].setData(to);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean SymbolInfoSessionTrade(String name, int day_of_week, int session_index, LocalDateTime from, LocalDateTime to)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SymbolInfoSessionTrade";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(day_of_week);
        funcInfo.parameter[2].setData(session_index);
        funcInfo.parameter[3].setData(from);
        funcInfo.parameter[4].setData(to);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    
    //  時系列・インジケータアクセス
    public long SeriesInfoInteger(String symbol_name, int timeframe, int prop_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "SeriesInfoInteger";
        funcInfo.parameter[0].setData(symbol_name);
        funcInfo.parameter[1].setData(timeframe);
        funcInfo.parameter[2].setData(prop_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataLong();
    }
    
    public boolean RefreshRates()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "RefreshRates";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    //  算術関数
    //  Java内部の算術関数を使用してください。
    
    //  チャート操作
    //  チャート操作系の関数はこのプログラムの目的とはあまり関係ないので実装しません。
        
    //  取引関数
    public int OrdersHistoryTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrdersHistoryTotal";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int OrdersTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrdersTotal";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    
    public boolean OrderSelect(int index, int select, int pool)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSelect";
        funcInfo.parameter[0].setData(index);
        funcInfo.parameter[1].setData(select);
        funcInfo.parameter[2].setData(pool);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSend";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(cmd);
        funcInfo.parameter[2].setData(volume);
        funcInfo.parameter[3].setData(price);
        funcInfo.parameter[4].setData(slippage);
        funcInfo.parameter[5].setData(stoploss);
        funcInfo.parameter[6].setData(takeprofit);
        funcInfo.parameter[7].setData("\0");
        funcInfo.parameter[8].setData(0);
        funcInfo.parameter[9].setData((long)0);
        funcInfo.parameter[10].setData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSend";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(cmd);
        funcInfo.parameter[2].setData(volume);
        funcInfo.parameter[3].setData(price);
        funcInfo.parameter[4].setData(slippage);
        funcInfo.parameter[5].setData(stoploss);
        funcInfo.parameter[6].setData(takeprofit);
        funcInfo.parameter[7].setData(comment);
        funcInfo.parameter[8].setData(0);
        funcInfo.parameter[9].setData((long)0);
        funcInfo.parameter[10].setData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment, int magic)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSend";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(cmd);
        funcInfo.parameter[2].setData(volume);
        funcInfo.parameter[3].setData(price);
        funcInfo.parameter[4].setData(slippage);
        funcInfo.parameter[5].setData(stoploss);
        funcInfo.parameter[6].setData(takeprofit);
        funcInfo.parameter[7].setData(comment);
        funcInfo.parameter[8].setData(magic);
        funcInfo.parameter[9].setData((long)0);
        funcInfo.parameter[10].setData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment, int magic, LocalDateTime expiration)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSend";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(cmd);
        funcInfo.parameter[2].setData(volume);
        funcInfo.parameter[3].setData(price);
        funcInfo.parameter[4].setData(slippage);
        funcInfo.parameter[5].setData(stoploss);
        funcInfo.parameter[6].setData(takeprofit);
        funcInfo.parameter[7].setData(comment);
        funcInfo.parameter[8].setData(magic);
        funcInfo.parameter[9].setData(expiration);
        funcInfo.parameter[10].setData(Color.BLACK);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public int OrderSend(String symbol, int cmd, double volume, double price, int slippage, double stoploss, double takeprofit, String comment, int magic, LocalDateTime expiration, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSend";
        funcInfo.parameter[0].setData(symbol);
        funcInfo.parameter[1].setData(cmd);
        funcInfo.parameter[2].setData(volume);
        funcInfo.parameter[3].setData(price);
        funcInfo.parameter[4].setData(slippage);
        funcInfo.parameter[5].setData(stoploss);
        funcInfo.parameter[6].setData(takeprofit);
        funcInfo.parameter[7].setData(comment);
        funcInfo.parameter[8].setData(magic);
        funcInfo.parameter[9].setData(expiration);
        funcInfo.parameter[10].setData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public boolean OrderClose(int ticket, double lots, double price, int slippage, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderClose";
        funcInfo.parameter[0].setData(ticket);
        funcInfo.parameter[1].setData(lots);
        funcInfo.parameter[2].setData(price);
        funcInfo.parameter[3].setData(slippage);
        funcInfo.parameter[4].setData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean OrderCloseBy(int ticket, int opposite, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderCloseBy";
        funcInfo.parameter[0].setData(ticket);
        funcInfo.parameter[1].setData(opposite);
        funcInfo.parameter[2].setData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean OrderModify(int ticket, double price, double stoploss, double takeprofit, LocalDateTime expiration, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderModify";
        funcInfo.parameter[0].setData(ticket);
        funcInfo.parameter[1].setData(price);
        funcInfo.parameter[2].setData(stoploss);
        funcInfo.parameter[3].setData(takeprofit);
        funcInfo.parameter[4].setData(expiration);
        funcInfo.parameter[5].setData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean OrderDelete(int ticket, Color arrow_color)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderDelete";
        funcInfo.parameter[0].setData(ticket);
        funcInfo.parameter[1].setData(arrow_color);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public void OrderPrint()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderPrint";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
    }
    public int OrderTicket()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderTicket";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    
    public LocalDateTime OrderOpenTime()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderOpenTime";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public double OrderOpenPrice()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderOpenPrice";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public int OrderType()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderType";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public double OrderLots()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderLots";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String OrderSymbol()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSymbol";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public double OrderStopLoss()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderStopLoss";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public double OrderTakeProfit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderTakeProfit";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public LocalDateTime OrderCloseTime()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderCloseTime";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public double OrderClosePrice()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderClosePrice";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public double OrderCommission()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderCommission";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public LocalDateTime OrderExpiration()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderExpiration";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public double OrderSwap()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderSwap";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public double OrderProfit()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderProfit";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String OrderComment()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderComment";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public int OrderMagicNumber()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "OrderMagicNumber";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    
    //  トレードシグナル
    //  このプログラムはインジケータの開発を目的としたものではないためトレードシグナル系の関数の実装はしません。
    
    //   クライアントターミナルのグローバル変数
    public boolean GlobalVariableCheck(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableCheck";
        funcInfo.parameter[0].setData(name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    public LocalDateTime GlobalVariableTime(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableTime";
        funcInfo.parameter[0].setData(name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public boolean GlobalVariableDel(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableDel";
        funcInfo.parameter[0].setData(name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public double GlobalVariableGet(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableGet";
        funcInfo.parameter[0].setData(name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String GlobalVariableName(int index)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableName";
        funcInfo.parameter[0].setData(index);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public LocalDateTime GlobalVariableSet(String name, double value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableSet";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(value);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDateTime();
    }
    public void GlobalVariablesFlush()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariablesFlush";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
    }
    public boolean GlobalVariableTemp(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableTemp";
        funcInfo.parameter[0].setData(name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean GlobalVariableSetOnCondition(String name, double value, double check_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariableSetOnCondition";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(value);
        funcInfo.parameter[2].setData(check_value);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    
    public int GlobalVariablesDeleteAll()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariablesDeleteAll1";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int GlobalVariablesDeleteAll(String prefix_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariablesDeleteAll2";
        funcInfo.parameter[0].setData(prefix_name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int GlobalVariablesDeleteAll(String prefix_name, LocalDateTime limit_data)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariablesDeleteAll3";
        funcInfo.parameter[0].setData(prefix_name);
        funcInfo.parameter[1].setData(limit_data);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int GlobalVariablesTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "GlobalVariablesTotal";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    //  オブジェクト関数
    public boolean ObjectCreate(long chart_id, String object_name, int object_type, int sub_window, LocalDateTime time1, double price1)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectCreate1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(object_type);
        funcInfo.parameter[3].setData(sub_window);
        funcInfo.parameter[4].setData(time1);
        funcInfo.parameter[5].setData(price1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectCreate(long chart_id, String object_name, int object_type, int sub_window, LocalDateTime time1, double price1, LocalDateTime time2, double price2)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectCreate2";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(object_type);
        funcInfo.parameter[3].setData(sub_window);
        funcInfo.parameter[4].setData(time1);
        funcInfo.parameter[5].setData(price1);
        funcInfo.parameter[6].setData(time2);
        funcInfo.parameter[7].setData(price2);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectCreate(long chart_id, String object_name, int object_type, int sub_window, LocalDateTime time1, double price1, LocalDateTime time2, double price2, LocalDateTime time3, double price3)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectCreate3";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(object_type);
        funcInfo.parameter[3].setData(sub_window);
        funcInfo.parameter[4].setData(time1);
        funcInfo.parameter[5].setData(price1);
        funcInfo.parameter[6].setData(time2);
        funcInfo.parameter[7].setData(price2);
        funcInfo.parameter[8].setData(time2);
        funcInfo.parameter[9].setData(price2);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public String ObjectName(long chart_id, int object_index, int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectName1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_index);
        funcInfo.parameter[2].setData(sub_window);
        funcInfo.parameter[3].setData(object_type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public String ObjectName(long chart_id, int object_index)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectName1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_index);
        funcInfo.parameter[2].setData(-1);
        funcInfo.parameter[3].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public String ObjectName(long chart_id, int object_index, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectName1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_index);
        funcInfo.parameter[2].setData(sub_window);
        funcInfo.parameter[3].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }    
    public String ObjectName(int object_index)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectName2";
        funcInfo.parameter[0].setData(object_index);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }
    public boolean ObjectDelete(String object_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectDelete1";
        funcInfo.parameter[0].setData(object_name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectDelete(long chart_id, String object_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectDelete2";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    
    public boolean ObjectsDeleteAll(long chart_id, int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(sub_window);
        funcInfo.parameter[2].setData(object_type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }    
    public boolean ObjectsDeleteAll(long chart_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(-1);
        funcInfo.parameter[2].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(sub_window);
        funcInfo.parameter[2].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, String prefix, int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll2";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(prefix);
        funcInfo.parameter[2].setData(sub_window);
        funcInfo.parameter[3].setData(object_type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, String prefix)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll2";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(prefix);
        funcInfo.parameter[2].setData(-1);
        funcInfo.parameter[3].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll(long chart_id, String prefix, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll2";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(prefix);
        funcInfo.parameter[2].setData(sub_window);
        funcInfo.parameter[3].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll(int sub_window, int object_type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll3";
        funcInfo.parameter[0].setData(sub_window);
        funcInfo.parameter[1].setData(object_type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll3";
        funcInfo.parameter[0].setData(-1);
        funcInfo.parameter[1].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectsDeleteAll(int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsDeleteAll3";
        funcInfo.parameter[0].setData(sub_window);
        funcInfo.parameter[1].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    int ObjectFind(long chart_id, String object_name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectFind1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }    
    int ObjectFind(String name)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectFind2";
        funcInfo.parameter[0].setData(name);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataInt();
    }
    public LocalDateTime ObjectGetTimeByValue(long chart_id, String object_name, double value, int line_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetTimeByValue";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(value);
        funcInfo.parameter[3].setData(line_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDateTime();
    }
    public LocalDateTime ObjectGetTimeByValue(long chart_id, String object_name, double value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetTimeByValue";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(value);
        funcInfo.parameter[3].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDateTime();
    }
    public double ObjectGetValueByShift(String name, int shift)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetValueByShift";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(shift);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDouble();
    }
    public double ObjectGetValueByTime(long chart_id, String object_name, LocalDateTime time, int line_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetValueByShift";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(time);
        funcInfo.parameter[3].setData(line_id);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDouble();
    }
    public double ObjectGetValueByTime(long chart_id, String object_name, LocalDateTime time)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetValueByShift";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(time);
        funcInfo.parameter[3].setData(0);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataDouble();
    }
    public boolean ObjectMove(long chart_id, String object_name, int point_index, LocalDateTime time, double price)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectMove1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(point_index);
        funcInfo.parameter[3].setData(time);
        funcInfo.parameter[4].setData(price);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectMove(String name, int point_index, LocalDateTime time1, double price1)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectMove2";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(point_index);
        funcInfo.parameter[2].setData(time1);
        funcInfo.parameter[3].setData(price1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataBool();
    }
    public int ObjectsTotal(long chart_id, int sub_window, int type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsTotal1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(sub_window);
        funcInfo.parameter[2].setData(type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int ObjectsTotal(long chart_id)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsTotal1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(-1);
        funcInfo.parameter[2].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int ObjectsTotal(long chart_id, int sub_window)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsTotal1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(sub_window);
        funcInfo.parameter[2].setData(-1);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int ObjectsTotal(int type)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsTotal2";
        funcInfo.parameter[0].setData(type);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public int ObjectsTotal()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectsTotal2";
        funcInfo.parameter[0].setData(ObjectInfo.ObjectType.EMPTY);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        return funcInfo.returnValue.getDataInt();
    }
    public long ObjectGetInteger(long chart_id, String object_name, int prop_id, int prop_modifier)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetInteger1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(prop_id);
        funcInfo.parameter[3].setData(prop_modifier);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataLong();
    }
    public double ObjectGetDouble(long chart_id, String object_name, int prop_id, int prop_modifier)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetDouble1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(prop_id);
        funcInfo.parameter[3].setData(prop_modifier);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataDouble();
    }
    public String ObjectGetString(long chart_id, String object_name, int prop_id, int prop_modifier)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectGetString1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(prop_id);
        funcInfo.parameter[3].setData(prop_modifier);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataString();
    }        
    public boolean ObjectSetInteger(long chart_id, String object_name, int prop_id, long prop_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectSetInteger1";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(prop_id);
        funcInfo.parameter[3].setData(prop_value);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectSetDouble(long chart_id, String object_name, int prop_id, double prop_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectSetDouble";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(prop_id);
        funcInfo.parameter[3].setData(prop_value);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean ObjectSetString(long chart_id, String object_name, int prop_id, String prop_value)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ObjectSetString";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(object_name);
        funcInfo.parameter[2].setData(prop_id);
        funcInfo.parameter[3].setData(prop_value);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean TextSetFont(String name, int size, int flags, int orientation)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "TextSetFont";
        funcInfo.parameter[0].setData(name);
        funcInfo.parameter[1].setData(size);
        funcInfo.parameter[2].setData(flags);
        funcInfo.parameter[3].setData(orientation);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    //  イベント操作
    public boolean EventSetMillisecondTimer(int milliseconds)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "EventSetMillisecondTimer";
        funcInfo.parameter[0].setData(milliseconds);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public boolean EventSetTimer(int seconds)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "EventSetTimer";
        funcInfo.parameter[0].setData(seconds);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public void EventKillTimer()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "EventKillTimer";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
    }
    public boolean EventChartCustom(long chart_id, short custom_event_id, long lparam, double dparam, String sparam)
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "EventChartCustom";
        funcInfo.parameter[0].setData(chart_id);
        funcInfo.parameter[1].setData(custom_event_id);
        funcInfo.parameter[2].setData(lparam);
        funcInfo.parameter[3].setData(dparam);
        funcInfo.parameter[4].setData(sparam);
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
        LastError = funcInfo.errorCode;
        return funcInfo.returnValue.getDataBool();
    }
    public void ExpertRemove()
    {
        FuncInfo funcInfo = new FuncInfo();
        funcInfo.funcName = "ExpertRemove";
        if (!SendReceiveRequest(funcInfo)) flagEmergencyStop = true;
    }
    //  MT4の関数はここまで
    
    //  指定した関数を呼び出す
    //  サーバー側と違いこの関数が呼び出せるのはグローバル関数のみ
    public void CallFunc(FuncInfo funcInfo)
    {
        if (MT4StringCompare(funcInfo.funcName, "OnPreInit"))
        {
            boolean debug = funcInfo.parameter[0].getDataDouble() != 0 ? true : false;
            OnPreInit(debug);
        }
        else
        {
            Ask = funcInfo.parameter[0].getDataDouble();
            Bid = funcInfo.parameter[1].getDataDouble();
            Volume = funcInfo.parameter[7].getDataLong();
            Bars = funcInfo.parameter[8].getDataInt();
            Digits = funcInfo.parameter[9].getDataInt();
            Point = funcInfo.parameter[10].getDataInt();

            if (MT4StringCompare(funcInfo.funcName, "OnInit"))
            {
                funcInfo.returnValue.setData(
                    OnInit()
                );
            }
            else if (MT4StringCompare(funcInfo.funcName, "OnTick"))
            {
                OnTick();
            }
            else if (MT4StringCompare(funcInfo.funcName, "OnTimer"))
            {
                OnTimer();
            }
            else if (MT4StringCompare(funcInfo.funcName, "OnDeinit"))
            {
                OnDeinit(funcInfo.parameter[0].getDataInt());
            }
            else if (MT4StringCompare(funcInfo.funcName, "OnTimerInternal"))
            {
                OnTimerInternal();
            }
            else
            {
                System.err.printf("不明な関数が呼び出されました'%s'。\n", funcInfo.funcName);
            }            
        }        
    }
    
    public void CallExpertRemove()
    {
        //  クライアント側なので何もしない。ソース共通化のため残している
    }
    
    boolean MT4StringCompare(String a, String b)
    {
        return (a.compareTo(b) == 0);
    }
    
    //  リクエスト情報の送受信
    public boolean SendReceiveRequest(FuncInfo funcInfo)
    {
        printDebugMessage("SendReceiveRequestが呼ばれました。");
        int PosParameter = 0;
        int PosAuxiliary = 0;
        Message s = new Message();
        Message r = new Message();
        FuncInfo funcInfoReceived = new FuncInfo();
        
        if (funcInfo.funcName != null)
        {
            //  呼び出す関数が決まっている
            s.setMessageType(Message.MSG_REQUEST_CALL_FUNCTION);
            s.getDataGram()[1].setData(funcInfo.funcName);
        }
        else
        {
            //  呼び出す関数が決まっていない（最初だけ何も要求を送らない）
            s.setMessageType(Message.MSG_NOP);
        }
        
        do
        {
            printDebugMessage("リクエスト情報を送受信するためのループ");
            s.setEmergencyStop(flagEmergencyStop ? (char)1 : (char)0);
            if (!pipe.SendMessage(s))
            {
                System.err.println("メッセージの送信に失敗しました");
                s = null;
                r = null;
                funcInfoReceived = null;
                return false;
            }
            printDebugMessage(String.format("メッセージを送信しました。メッセージタイプは%X", s.getMessageType()));
            
            if (funcInfoReceived.funcName != null && 
                MT4StringCompare(funcInfoReceived.funcName, "OnDeinit") && 
                s.getMessageType() == Message.MSG_AUXILIARY_END)
            {
                 //  OnDeinitの呼び出し依頼、かつ補助情報の送信が終わった場合
                 break;   //  この関数の無限ループを抜ける
            }

            //  ----  ここまでで関数呼び出し依頼は完了している  ----
            
            r.clear();
            if (!pipe.ReceiveMessage(r))
            {
                System.err.println("メッセージの受信に失敗しました");
                s = null;
                r = null;
                funcInfoReceived = null;
                return false;
            }
            printDebugMessage(String.format("メッセージを受信しました。メッセージタイプは%X", r.getMessageType()));
            
            if (r.getMessageType()==Message.MSG_NULL)
            {
                System.err.println("空のメッセージを受信しました。緊急停止します");
                s = null;
                r = null;
                funcInfoReceived = null;
                return false;
            }
            
            //  緊急終了フラグの確認
            if(r.getEmergencyStop()!=0)
            {
                CallExpertRemove();
            }
            
            //  先方からのメッセージを処理する
            switch (r.getMessageType())
            {
                //   関数呼び出しの結果
                //  関数呼び出し依頼をした場合
                case Message.MSG_REQUEST_PARAMETER:
                {
                    printDebugMessage("MSG_REQUEST_PARAMETERを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_PARAMETER);
                    funcInfo.parameter[PosParameter].copyDataGramTo(s.getDataGram()[1]);
                    if (PosParameter >= funcInfo.getNumberOfParameters())
                    {
                        s.setMessageType(Message.MSG_PARAMETER_END);
                    }
                    PosParameter++;
                    break;
                }
                case Message.MSG_RETURN_VALUE:
                {
                    printDebugMessage("MSG_RETURN_VALUEを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_REQUEST_ERROR_CODE);
                    r.getDataGram()[1].copyDataGramTo(funcInfo.returnValue);
                    break;
                }
                case Message.MSG_ERROR_CODE:
                {
                    printDebugMessage("MSG_ERROR_CODEを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_REQUEST_AUXILIARY);
                    funcInfo.errorCode = r.getDataGram()[1].getDataInt();
                    PosAuxiliary = 0;
                    break;
                }
                case Message.MSG_AUXILIARY:
                case Message.MSG_AUXILIARY_END:
                {
                    printDebugMessage("MSG_AUXILIARYまたはMSG_AUXILIARY_ENDを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_REQUEST_AUXILIARY);
                    r.getDataGram()[1].copyDataGramTo(funcInfo.auxiliary[PosAuxiliary]);
                    PosAuxiliary++;
                    break;
                }
                //  関数呼び出し依頼
                case Message.MSG_REQUEST_CALL_FUNCTION:
                {
                    printDebugMessage("MSG_CALL_FUNCTIONを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_REQUEST_PARAMETER);
                    funcInfoReceived.clear();
                    funcInfoReceived.funcName = r.getDataGram()[1].getDataString();
                    PosParameter = 0;
                    break;
                }
                case Message.MSG_PARAMETER:
                {
                    printDebugMessage("MSG_PARAMETERを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_REQUEST_PARAMETER);
                    r.getDataGram()[1].copyDataGramTo(funcInfoReceived.parameter[PosParameter]);
                    PosParameter++;
                    break;
                }
                case Message.MSG_PARAMETER_END:
                {
                    printDebugMessage("MSG_PARAMETER_ENDを受信しました");
                    r.getDataGram()[1].copyDataGramTo(funcInfoReceived.parameter[PosParameter]);
                    PosParameter++;
                    
                    //  関数実行
                    CallFunc(funcInfoReceived);
                    
                    //  関数呼び出し後
                    s.clear();
                    s.setMessageType(Message.MSG_RETURN_VALUE);
                    funcInfoReceived.returnValue.copyDataGramTo(s.getDataGram()[1]);
                    break;
                }
                case Message.MSG_REQUEST_ERROR_CODE:
                {
                    printDebugMessage("MSG_REQUEST_ERROR_CODEを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_ERROR_CODE);
                    s.getDataGram()[1].setData(funcInfoReceived.errorCode);
                    break;
                }
                case Message.MSG_REQUEST_AUXILIARY:
                {
                    printDebugMessage("MSG_REQUEST_AUXILIARYを受信しました");
                    s.clear();
                    s.setMessageType(Message.MSG_AUXILIARY);
                    funcInfoReceived.auxiliary[PosAuxiliary].copyDataGramTo(s.getDataGram()[1]);
                    if (PosAuxiliary >= funcInfoReceived.getNumberOfAuxiliaries())
                    {
                        s.setMessageType(Message.MSG_AUXILIARY_END);
                    }
                    PosAuxiliary++;
                    break;
                }
            }
        }
        while (r.getMessageType()!=Message.MSG_AUXILIARY_END);
        
        s = null;
        r = null;
        funcInfoReceived = null;
        printDebugMessage("リクエスト情報の送受信を行いました。");
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
                synchronized (pipe)
                {
                    flagEmergencyStop = true;
                    pipe.wait();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    };
    
    //  サーバーへの接続を行う
    public final void ConnectToMT4(String PipeName)
    {
        printDebugMessage("サーバへ接続します。");
        try
        {
            pipe.ConnectToServer(PipeName);

        }
        catch (FileNotFoundException e)
        {
            System.err.printf("指定された名前付きパイプ'%s'が見つかりません。\n", PipeName);
            return;                
        }
        Runtime.getRuntime().addShutdownHook(shutdownSequence1);
        FuncInfo fi = new FuncInfo();
        fi.funcName = null;
        SendReceiveRequest(fi);
        printDebugMessage("リクエストの送受信がすべて終わりました。プログラムを終了します。");
        
        try
        {
            synchronized (pipe)
            {
                pipe.notify();
                Runtime.getRuntime().removeShutdownHook(shutdownSequence1);            
            }
        }
        catch (IllegalStateException e)
        {
            //  シャットダウン中だった場合。特に何もしない
        }
        
        printDebugMessage("サーバーとの接続を切断します。");
        pipe.Close();
    }
}
