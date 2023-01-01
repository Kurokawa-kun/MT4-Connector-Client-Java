package MT4Connector.consts;

public class TerminalInfo
{
    public static class IntegerPropertyID
    {
        public static final int TERMINAL_BUILD = 0x05;
        public static final int TERMINAL_COMMUNITY_ACCOUNT = 0x17;
        public static final int TERMINAL_COMMUNITY_CONNECTION = 0x18;
        public static final int TERMINAL_CONNECTED = 0x06;
        public static final int TERMINAL_DLLS_ALLOWED = 0x07;
        public static final int TERMINAL_TRADE_ALLOWED = 0x08;
        public static final int TERMINAL_EMAIL_ENABLED = 0x09;
        public static final int TERMINAL_FTP_ENABLED = 0x0A;
        public static final int TERMINAL_NOTIFICATIONS_ENABLED = 0x1A;
        public static final int TERMINAL_MAXBARS = 0x0B;
        public static final int TERMINAL_MQID = 0x16;
        public static final int TERMINAL_CODEPAGE = 0x0C;
        public static final int TERMINAL_CPU_CORES = 0x15;
        public static final int TERMINAL_DISK_SPACE = 0x14;
        public static final int TERMINAL_MEMORY_PHYSICAL = 0x0E;
        public static final int TERMINAL_MEMORY_TOTAL = 0x0F;
        public static final int TERMINAL_MEMORY_AVAILABLE = 0x10;
        public static final int TERMINAL_MEMORY_USED = 0x11;
        public static final int TERMINAL_SCREEN_DPI = 0x1B;
        public static final int TERMINAL_PING_LAST = 0x1C;
    }
    public static class DoublePropertyID
    {
        public static final int TERMINAL_LANGUAGE = 0x0D;
        public static final int TERMINAL_COMPANY = 0x00;
        public static final int TERMINAL_NAME = 0x01;
        public static final int TERMINAL_PATH = 0x02;
        public static final int TERMINAL_DATA_PATH = 0x03;
        public static final int TERMINAL_COMMONDATA_PATH = 0x04;
    }
    public static class StringPropertyID
    {
        public static final int TERMINAL_COMMUNITY_BALANCE = 0x19;
    }
    public static class KeyState
    {
        public static final int TERMINAL_KEYSTATE_LEFT = 0x40D;
        public static final int TERMINAL_KEYSTATE_UP = 0x40E;
        public static final int TERMINAL_KEYSTATE_RIGHT = 0x40F;
        public static final int TERMINAL_KEYSTATE_DOWN = 0x410;
        public static final int TERMINAL_KEYSTATE_SHIFT = 0x3F8;
        public static final int TERMINAL_KEYSTATE_CONTROL = 0x3F9;
        public static final int TERMINAL_KEYSTATE_MENU = 0x3FA;
        public static final int TERMINAL_KEYSTATE_CAPSLOCK = 0x3FC;
        public static final int TERMINAL_KEYSTATE_NUMLOCK = 0x478;
        public static final int TERMINAL_KEYSTATE_SCRLOCK = 0x479;
        public static final int TERMINAL_KEYSTATE_ENTER = 0x3F5;
        public static final int TERMINAL_KEYSTATE_INSERT = 0x415;
        public static final int TERMINAL_KEYSTATE_DELETE = 0x416;
        public static final int TERMINAL_KEYSTATE_HOME = 0x40C;
        public static final int TERMINAL_KEYSTATE_END = 0x40B;
        public static final int TERMINAL_KEYSTATE_TAB = 0x3F1;
        public static final int TERMINAL_KEYSTATE_PAGEUP = 0x409;
        public static final int TERMINAL_KEYSTATE_PAGEDOWN = 0x40A;
        public static final int TERMINAL_KEYSTATE_ESCAPE = 0x403;
    }
}
