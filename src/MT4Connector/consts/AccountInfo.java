package MT4Connector.consts;

public class AccountInfo
{
    public static class IntegerPropertyID
    {
        public static final int ACCOUNT_LOGIN                   = 0x00;
        public static final int ACCOUNT_TRADE_MODE              = 0x20;
        public static final int ACCOUNT_LEVERAGE                = 0x23;
        public static final int ACCOUNT_LIMIT_ORDERS            = 0x2F;
        public static final int ACCOUNT_MARGIN_SO_MODE          = 0x2C;
        public static final int ACCOUNT_TRADE_ALLOWED           = 0x21;
        public static final int ACCOUNT_TRADE_EXPERT            = 0x22;
    }
    
    public static class DoublePropertyID
    {
        public static final int ACCOUNT_BALANCE                 = 0x25;
        public static final int ACCOUNT_CREDIT                  = 0x26;
        public static final int ACCOUNT_PROFIT                  = 0x27;
        public static final int ACCOUNT_EQUITY                  = 0x28;
        public static final int ACCOUNT_MARGIN                  = 0x29;
        public static final int ACCOUNT_FREEMARGIN              = 0x2A;
        public static final int ACCOUNT_MARGIN_FREE             = 0x2A;
        public static final int ACCOUNT_MARGIN_LEVEL            = 0x2B;
        public static final int ACCOUNT_MARGIN_SO_CALL          = 0x2D;
        public static final int ACCOUNT_MARGIN_SO_SO            = 0x2E;
        public static final int ACCOUNT_MARGIN_INITIAL          = 0x30;
        public static final int ACCOUNT_MARGIN_MAINTENANCE      = 0x31;
        public static final int ACCOUNT_ASSETS                  = 0x32;
        public static final int ACCOUNT_LIABILITIES             = 0x33;
        public static final int ACCOUNT_COMMISSION_BLOCKED      = 0x34;
    }
    
    public static class StringPropertyID
    {
        public static final int ACCOUNT_NAME                    = 0x01;
        public static final int ACCOUNT_SERVER                  = 0x03;
        public static final int ACCOUNT_CURRENCY                = 0x24;
        public static final int ACCOUNT_COMPANY                 = 0x02;
        public static final int ACCOUNT_TRADE_MODE_DEMO         = 0x00;
        public static final int ACCOUNT_TRADE_MODE_CONTEST      = 0x01;
        public static final int ACCOUNT_TRADE_MODE_REAL         = 0x02;
    }
    
    public static class StopOutMode
    {
        public static final int ACCOUNT_STOPOUT_MODE_PERCENT    = 0x00;
        public static final int ACCOUNT_STOPOUT_MODE_MONEY      = 0x01;    
    }
}
