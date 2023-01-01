package MT4Connector.consts;

public class MT4Runtime
{
    public static class InitializeRetCode
    {
        public static final int INIT_SUCCEEDED              = 0x00;
        public static final int INIT_FAILED                 = 0x01;
        public static final int INIT_PARAMETERS_INCORRECT   = 0x7FFF;
        public static final int INIT_AGENT_NOT_SUITABLE     = 0xFFFF;
    }
    public static class UninitializeReason
    {
        public static final int REASON_PROGRAM       = 0x00;
        public static final int REASON_REMOVE        = 0x01;
        public static final int REASON_RECOMPILE     = 0x02;
        public static final int REASON_CHARTCHANGE   = 0x03;
        public static final int REASON_CHARTCLOSE    = 0x04;
        public static final int REASON_PARAMETERS    = 0x05;
        public static final int REASON_ACCOUNT       = 0x06;
        public static final int REASON_TEMPLATE      = 0x07;
        public static final int REASON_INITFAILED    = 0x08;
        public static final int REASON_CLOSE         = 0x09;
    }
    //  注文種別
    public static class OrderType
    {
        public static final int OP_BUY = 0x00;
        public static final int OP_SELL = 0x01;
        public static final int OP_BUYLIMIT = 0x02;
        public static final int OP_SELLLIMIT = 0x03;
        public static final int OP_BUYSTOP = 0x04;
        public static final int OP_SELLSTOP = 0x05;    
    }
    //  MT4の時間足
    public static class TimeFrame
    {
        public static final int PERIOD_CURRENT = 0;
        public static final int PERIOD_M1 = 1;
        public static final int PERIOD_M5 = 5;
        public static final int PERIOD_M15 = 15;
        public static final int PERIOD_M30 = 30;
        public static final int PERIOD_H1 = 60;
        public static final int PERIOD_H4 = 240;
        public static final int PERIOD_D1 = 1440;
        public static final int PERIOD_W1 = 10080;
        public static final int PERIOD_MN1 = 43200;

        public static final int[] MT4TimeFrames = 
        {
            PERIOD_M1,
            PERIOD_M5,
            PERIOD_M15,
            PERIOD_M30,
            PERIOD_H1,
            PERIOD_H4,
            PERIOD_D1,
            PERIOD_W1,
            PERIOD_MN1
        };
    }    
}
