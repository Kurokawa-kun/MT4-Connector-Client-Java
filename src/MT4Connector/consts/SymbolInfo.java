package MT4Connector.consts;

public class SymbolInfo
{
    public static class IntegerPropertyID
    {
        public static final int SYMBOL_SELECT = 0x00;
        public static final int SYMBOL_VISIBLE = 0x4C;
        public static final int SYMBOL_SESSION_DEALS = 0x38;
        public static final int SYMBOL_SESSION_BUY_ORDERS = 0x3C;
        public static final int SYMBOL_SESSION_SELL_ORDERS = 0x3E;
        public static final int SYMBOL_VOLUME = 0x0A;
        public static final int SYMBOL_VOLUMEHIGH = 0x0B;
        public static final int SYMBOL_VOLUMELOW = 0x0C;
        public static final int SYMBOL_TIME = 0x0F;
        public static final int SYMBOL_DIGITS = 0x11;
        public static final int SYMBOL_SPREAD_FLOAT = 0x29;
        public static final int SYMBOL_SPREAD = 0x12;
        public static final int SYMBOL_TRADE_CALC_MODE = 0x1D;
        public static final int SYMBOL_TRADE_MODE = 0x1E;
        public static final int SYMBOL_START_TIME = 0x33;
        public static final int SYMBOL_EXPIRATION_TIME = 0x34;
        public static final int SYMBOL_TRADE_STOPS_LEVEL = 0x1F;
        public static final int SYMBOL_TRADE_FREEZE_LEVEL = 0x20;
        public static final int SYMBOL_TRADE_EXEMODE = 0x21;
        public static final int SYMBOL_SWAP_MODE = 0x25;
        public static final int SYMBOL_SWAP_ROLLOVER3DAYS = 0x28;
        public static final int SYMBOL_EXPIRATION_MODE = 0x31;
        public static final int SYMBOL_FILLING_MODE = 0x32;
        public static final int SYMBOL_ORDER_MODE = 0x47;
    }
    public static class DoublePropertyID
    {
        public static final int SYMBOL_BID = 0x01;
        public static final int SYMBOL_BIDHIGH = 0x02;
        public static final int SYMBOL_BIDLOW = 0x03;
        public static final int SYMBOL_ASK = 0x04;
        public static final int SYMBOL_ASKHIGH = 0x05;
        public static final int SYMBOL_ASKLOW = 0x06;
        public static final int SYMBOL_LAST = 0x07;
        public static final int SYMBOL_LASTHIGH = 0x08;
        public static final int SYMBOL_LASTLOW = 0x09;
        public static final int SYMBOL_POINT = 0x10;
        public static final int SYMBOL_TRADE_TICK_VALUE = 0x1A;
        public static final int SYMBOL_TRADE_TICK_VALUE_PROFIT = 0x35;
        public static final int SYMBOL_TRADE_TICK_VALUE_LOSS = 0x36;
        public static final int SYMBOL_TRADE_TICK_SIZE = 0x1B;
        public static final int SYMBOL_TRADE_CONTRACT_SIZE = 0x1C;
        public static final int SYMBOL_VOLUME_MIN = 0x22;
        public static final int SYMBOL_VOLUME_MAX = 0x23;
        public static final int SYMBOL_VOLUME_STEP = 0x24;
        public static final int SYMBOL_VOLUME_LIMIT = 0x37;
        public static final int SYMBOL_SWAP_LONG = 0x26;
        public static final int SYMBOL_SWAP_SHORT = 0x27;
        public static final int SYMBOL_MARGIN_INITIAL = 0x2A;
        public static final int SYMBOL_MARGIN_MAINTENANCE = 0x2B;
        public static final int SYMBOL_MARGIN_LONG = 0x2C;
        public static final int SYMBOL_MARGIN_SHORT = 0x2D;
        public static final int SYMBOL_MARGIN_LIMIT = 0x2E;
        public static final int SYMBOL_MARGIN_STOP = 0x2F;
        public static final int SYMBOL_MARGIN_STOPLIMIT = 0x30;
        public static final int SYMBOL_SESSION_VOLUME = 0x39;
        public static final int SYMBOL_SESSION_TURNOVER = 0x3A;
        public static final int SYMBOL_SESSION_INTEREST = 0x3B;
        public static final int SYMBOL_SESSION_BUY_ORDERS_VOLUME = 0x3D;
        public static final int SYMBOL_SESSION_SELL_ORDERS_VOLUME = 0x3F;
        public static final int SYMBOL_SESSION_OPEN = 0x40;
        public static final int SYMBOL_SESSION_CLOSE = 0x41;
        public static final int SYMBOL_SESSION_AW = 0x42;
        public static final int SYMBOL_SESSION_PRICE_SETTLEMENT = 0x43;
        public static final int SYMBOL_SESSION_PRICE_LIMIT_MIN = 0x44;
        public static final int SYMBOL_SESSION_PRICE_LIMIT_MAX = 0x45;    
    }
    public static class StringPropertyID
    {
        public static final int SYMBOL_CURRENCY_BASE = 0x16;
        public static final int SYMBOL_CURRENCY_PROFIT = 0x17;
        public static final int SYMBOL_CURRENCY_MARGIN = 0x18;
        public static final int SYMBOL_DESCRIPTION = 0x14;
        public static final int SYMBOL_PATH = 0x15;
    }
    //  曜日（ロールオーバーする曜日を取得するために使う）
    public static class DayOfWeek
    {
        public static final int SUNDAY = 0x00;
        public static final int MONDAY = 0x01;
        public static final int TUESDAY = 0x02;
        public static final int WEDNESDAY = 0x03;
        public static final int THURSDAY = 0x04;
        public static final int FRIDAY = 0x05;
        public static final int SATURDAY = 0x06;
    }
    //  注文執行モード
    public static class TradeExecution
    {
        public static final int SYMBOL_TRADE_EXECUTION_REQUEST = 0x00;
        public static final int SYMBOL_TRADE_EXECUTION_INSTANT = 0x01;
        public static final int SYMBOL_TRADE_EXECUTION_MARKET = 0x02;
        public static final int SYMBOL_TRADE_EXECUTION_EXCHANGE = 0x03; //  MQL4では使用されていない
    }
    //  取引モード
    public static class TradeMode
    {
        public static final int SYMBOL_TRADE_MODE_DISABLED = 0x00;
        public static final int SYMBOL_TRADE_MODE_LONGONLY = 0x03;  //  MQL4では使用されていない
        public static final int SYMBOL_TRADE_MODE_SHORTONLY = 0x04; //  MQL4では使用されていない
        public static final int SYMBOL_TRADE_MODE_CLOSEONLY = 0x01;
        public static final int SYMBOL_TRADE_MODE_FULL = 0x02;
    }
}
