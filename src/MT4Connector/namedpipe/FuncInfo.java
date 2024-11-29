package MT4Connector.namedpipe;
//  リクエストの送受信で使われる関数の情報

//  含まれる情報：
//  関数名
//  関数に渡すパラメタ
//  関数の復帰値
//  関数のエラーコード
//  補助情報（配列を取る復帰値、呼び出し先で変更されたパラメタの値などを格納する）
public class FuncInfo
{
    public static final int MAX_NUMBER_OF_PARAMETERS = 30;
    public static final int MAX_NUMBER_OF_AUXILIARIES = 5000;
    public String funcName;
    public DataGram[] parameter = new DataGram[MAX_NUMBER_OF_PARAMETERS];
    public DataGram returnValue;
    public int errorCode;
    public DataGram[] auxiliary = new DataGram[MAX_NUMBER_OF_AUXILIARIES];
    
    //  コンストラクタ
    public FuncInfo()
    {
        clear();
    }
    //  フィールドをクリアする
    public void clear()
    {
        funcName = null;
        for (int c=0; c<MAX_NUMBER_OF_PARAMETERS; c++)
        {
            parameter[c] = new DataGram();
            parameter[c].clear();
        }
        returnValue = new DataGram();
        returnValue.clear();
        errorCode = 0;
        for (int c=0; c<MAX_NUMBER_OF_AUXILIARIES; c++)
        {
            auxiliary[c] = new DataGram();
            auxiliary[c].clear();
        }
    }
    //  パラメタの数を取得する
    public int getNumberOfParameters()
    {
        int c;
        for (c=0; c<MAX_NUMBER_OF_PARAMETERS; c++)
        {
            if (parameter[c].isEmpty()) break;
        }
        return c;
    }
    //  補助情報の数を取得する
    public int getNumberOfAuxiliaries()
    {
        int c;
        for (c=0; c<MAX_NUMBER_OF_AUXILIARIES; c++)
        {
            if (auxiliary[c].isEmpty()) break;
        }
        return c;
    }
}
