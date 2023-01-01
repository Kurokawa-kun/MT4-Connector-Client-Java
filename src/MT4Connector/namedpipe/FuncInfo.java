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
    public String FuncName;
    public DataGram[] Parameter = new DataGram[MAX_NUMBER_OF_PARAMETERS];
    public DataGram ReturnValue;
    public int ErrorCode;
    public DataGram[] Auxiliary = new DataGram[MAX_NUMBER_OF_AUXILIARIES];
    
    //  コンストラクタ
    public FuncInfo()
    {
        Clear();
        return;
    }
    //  フィールドをクリアする
    public void Clear()
    {
        FuncName = null;
        for (int c=0; c<MAX_NUMBER_OF_PARAMETERS; c++)
        {
            Parameter[c] = new DataGram();
            Parameter[c].Clear();
        }
        ReturnValue = new DataGram();
        ReturnValue.Clear();
        ErrorCode = 0;
        for (int c=0; c<MAX_NUMBER_OF_AUXILIARIES; c++)
        {
            Auxiliary[c] = new DataGram();
            Auxiliary[c].Clear();
        }
        return;
    }
    //  パラメタの数を取得する
    public int GetNumberOfParameters()
    {
        int c;
        for (c=0; c<MAX_NUMBER_OF_PARAMETERS; c++)
        {
            if (Parameter[c].IsEmpty()) break;
        }
        return c;
    }
    //  補助情報の数を取得する
    public int GetNumberOfAuxiliaries()
    {
        int c;
        for (c=0; c<MAX_NUMBER_OF_AUXILIARIES; c++)
        {
            if (Auxiliary[c].IsEmpty()) break;
        }
        return c;
    }
}
