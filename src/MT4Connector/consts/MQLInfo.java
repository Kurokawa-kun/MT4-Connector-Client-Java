package MT4Connector.consts;

public class MQLInfo
{
    public static class IntegerPropertyID
    {
        public static final int MQL_CODEPAGE = 0x0F;
        public static final int MQL_PROGRAM_TYPE = 0x02;
        public static final int MQL_DLLS_ALLOWED = 0x03;
        public static final int MQL_TRADE_ALLOWED = 0x04;
        public static final int MQL_SIGNALS_ALLOWED = 0x0E;
        public static final int MQL_DEBUG = 0x05;
        public static final int MQL_PROFILER = 0x0A;
        public static final int MQL_TESTER = 0x06;
        public static final int MQL_OPTIMIZATION = 0x07;
        public static final int MQL_VISUAL_MODE = 0x08;
        public static final int MQL_FRAME_MODE = 0x0C;
        public static final int MQL_LICENSE_TYPE = 0x09;
    }
    public static class StringPropertyID
    {
        public static final int MQL_PROGRAM_NAME = 0x00;
        public static final int MQL_PROGRAM_PATH = 0x01;
    }
    public static class LicenseType
    {
        public static final int LICENSE_FREE = 0x00;
        public static final int LICENSE_DEMO = 0x01;
        public static final int LICENSE_FULL = 0x02;
        public static final int LICENSE_TIME = 0x03;
    }
    public static class ProgramType
    {
        public static final int PROGRAM_SCRIPT = 0x01;
        public static final int PROGRAM_EXPERT = 0x02;
        public static final int PROGRAM_INDICATOR = 0x04;    
    }
}


