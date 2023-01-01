package MT4Connector.consts;

public class ObjectInfo
{
    //  GUIオブジェクトの種類
    public static class ObjectType    //  ！この定数はMQL4とMQL5で互換性がない
    {
        public static final int EMPTY                   = -0x01;
        public static final int OBJ_VLINE               =  0x00;
        public static final int OBJ_HLINE               =  0x01;
        public static final int OBJ_TREND               =  0x02;
        public static final int OBJ_TRENDBYANGLE        =  0x03;
        public static final int OBJ_CYCLES              =  0x04;
        public static final int OBJ_CHANNEL             =  0x05;
        public static final int OBJ_STDDEVCHANNEL       =  0x06;
        public static final int OBJ_REGRESSION          =  0x07;
        public static final int OBJ_PITCHFORK           =  0x08;
        public static final int OBJ_GANNLINE            =  0x09;
        public static final int OBJ_GANNFAN             =  0x0A;
        public static final int OBJ_GANNGRID            =  0x0B;
        public static final int OBJ_FIBO                =  0x0C;
        public static final int OBJ_FIBOTIMES           =  0x0D;
        public static final int OBJ_FIBOFAN             =  0x0E;
        public static final int OBJ_FIBOARC             =  0x0F;
        public static final int OBJ_FIBOCHANNEL         =  0x10;
        public static final int OBJ_EXPANSION           =  0x11;
        public static final int OBJ_RECTANGLE           =  0x12;
        public static final int OBJ_TRIANGLE            =  0x13;
        public static final int OBJ_ELLIPSE             =  0x14;
        public static final int OBJ_ARROW_THUMB_UP      =  0x15;
        public static final int OBJ_ARROW_THUMB_DOWN    =  0x16;
        public static final int OBJ_ARROW_UP            =  0x17;
        public static final int OBJ_ARROW_DOWN          =  0x18;
        public static final int OBJ_ARROW_STOP          =  0x19;
        public static final int OBJ_ARROW_CHECK         =  0x1A;
        public static final int OBJ_ARROW_LEFT_PRICE    =  0x1B;
        public static final int OBJ_ARROW_RIGHT_PRICE   =  0x1C;
        public static final int OBJ_ARROW_BUY           =  0x1D;
        public static final int OBJ_ARROW_SELL          =  0x1E;
        public static final int OBJ_ARROW               =  0x1F;
        public static final int OBJ_TEXT                =  0x20;
        public static final int OBJ_LABEL               =  0x21;
        public static final int OBJ_BUTTON              =  0x22;
        public static final int OBJ_BITMAP              =  0x23;
        public static final int OBJ_BITMAP_LABEL        =  0x24;
        public static final int OBJ_EDIT                =  0x25;
        public static final int OBJ_EVENT               =  0x26;
        public static final int OBJ_RECTANGLE_LABEL     =  0x27;
    }
    public static class IntegerPropertyID
    {
        public static final int OBJPROP_COLOR = 0x06;
        public static final int OBJPROP_STYLE = 0x07;
        public static final int OBJPROP_WIDTH = 0x08;
        public static final int OBJPROP_BACK = 0x09;
        public static final int OBJPROP_ZORDER = 0xCF;
        public static final int OBJPROP_FILL = 0x407;
        public static final int OBJPROP_HIDDEN = 0xD0;
        public static final int OBJPROP_SELECTED = 0x11;
        public static final int OBJPROP_READONLY = 0x404;
        public static final int OBJPROP_TYPE = 0x12;
        public static final int OBJPROP_TIME = 0x13;
        public static final int OBJPROP_SELECTABLE = 0x3E8;
        public static final int OBJPROP_CREATETIME = 0x3E6;
        public static final int OBJPROP_LEVELS = 0xC8;
        public static final int OBJPROP_LEVELCOLOR = 0xC9;
        public static final int OBJPROP_LEVELSTYLE = 0xCA;
        public static final int OBJPROP_LEVELWIDTH = 0xCB;
        public static final int OBJPROP_ALIGN = 0x40C;
        public static final int OBJPROP_FONTSIZE = 0x64;
        public static final int OBJPROP_RAY_RIGHT = 0x3EC;
        public static final int OBJPROP_ELLIPSE = 0x0B;
        public static final int OBJPROP_ARROWCODE = 0x0E;
        public static final int OBJPROP_TIMEFRAMES = 0x0F;
        public static final int OBJPROP_ANCHOR = 0x3F3;
        public static final int OBJPROP_XDISTANCE = 0x66;
        public static final int OBJPROP_YDISTANCE = 0x67;
        public static final int OBJPROP_STATE = 0x3FA;
        public static final int OBJPROP_XSIZE = 0x3FB;
        public static final int OBJPROP_YSIZE = 0x3FC;
        public static final int OBJPROP_XOFFSET = 0x409;
        public static final int OBJPROP_YOFFSET = 0x40A;
        public static final int OBJPROP_BGCOLOR = 0x401;
        public static final int OBJPROP_CORNER = 0x65;
        public static final int OBJPROP_BORDER_TYPE = 0x405;
        public static final int OBJPROP_BORDER_COLOR = 0x40B;
    }   
    public static class DoublePropertyID
    {
        public static final int OBJPROP_PRICE = 0x14;
        public static final int OBJPROP_LEVELVALUE = 0xCC;
        public static final int OBJPROP_SCALE = 0x0C;
        public static final int OBJPROP_ANGLE = 0x0D;
        public static final int OBJPROP_DEVIATION = 0x10;
    }
    public static class StringPropertyID
    {
        public static final int OBJPROP_NAME = 0x40D;
        public static final int OBJPROP_TEXT = 0x3E7;
        public static final int OBJPROP_TOOLTIP = 0xCE;
        public static final int OBJPROP_LEVELTEXT = 0xCD;
        public static final int OBJPROP_FONT = 0x3E9;
        public static final int OBJPROP_BMPFILE = 0x3F9;
    }
    public static class ObjectProperty
    {
        public static final int OBJPROP_TIME1           = 0;
        public static final int OBJPROP_PRICE1          = 1;
        public static final int OBJPROP_TIME2           = 2;
        public static final int OBJPROP_PRICE2          = 3;
        public static final int OBJPROP_TIME3           = 4;
        public static final int OBJPROP_PRICE3          = 5;
        public static final int OBJPROP_COLOR           = 6;
        public static final int OBJPROP_STYLE           = 7;
        public static final int OBJPROP_WIDTH           = 8;
        public static final int OBJPROP_BACK            = 9;
        public static final int OBJPROP_RAY             = 10;
        public static final int OBJPROP_ELLIPSE         = 11;
        public static final int OBJPROP_SCALE           = 12;
        public static final int OBJPROP_ANGLE           = 13;
        public static final int OBJPROP_ARROWCODE       = 14;
        public static final int OBJPROP_TIMEFRAMES      = 15;
        public static final int OBJPROP_DEVIATION       = 16;
        public static final int OBJPROP_FONTSIZE        = 100;
        public static final int OBJPROP_CORNER          = 101;
        public static final int OBJPROP_XDISTANCE       = 102;
        public static final int OBJPROP_YDISTANCE       = 103;
        public static final int OBJPROP_FIBOLEVELS      = 200;
        public static final int OBJPROP_LEVELCOLOR      = 201;
        public static final int OBJPROP_LEVELSTYLE      = 202;
        public static final int OBJPROP_LEVELWIDTH      = 203;
        public static final int OBJPROP_FIRSTLEVEL      = 210;
        public static final int OBJPROP_FIRSTLEVEL_1    = 211;
        public static final int OBJPROP_FIRSTLEVEL_2    = 212;
        public static final int OBJPROP_FIRSTLEVEL_3    = 213;
        public static final int OBJPROP_FIRSTLEVEL_4    = 214;
        public static final int OBJPROP_FIRSTLEVEL_5    = 215;
        public static final int OBJPROP_FIRSTLEVEL_6    = 216;
        public static final int OBJPROP_FIRSTLEVEL_7    = 217;
        public static final int OBJPROP_FIRSTLEVEL_8    = 218;
        public static final int OBJPROP_FIRSTLEVEL_9    = 219;
        public static final int OBJPROP_FIRSTLEVEL_10   = 220;
        public static final int OBJPROP_FIRSTLEVEL_11   = 221;
        public static final int OBJPROP_FIRSTLEVEL_12   = 222;
        public static final int OBJPROP_FIRSTLEVEL_13   = 223;
        public static final int OBJPROP_FIRSTLEVEL_14   = 224;
        public static final int OBJPROP_FIRSTLEVEL_15   = 225;
        public static final int OBJPROP_FIRSTLEVEL_16   = 226;
        public static final int OBJPROP_FIRSTLEVEL_17   = 227;
        public static final int OBJPROP_FIRSTLEVEL_18   = 228;
        public static final int OBJPROP_FIRSTLEVEL_19   = 229;
        public static final int OBJPROP_FIRSTLEVEL_20   = 230;
        public static final int OBJPROP_FIRSTLEVEL_21   = 231;
        public static final int OBJPROP_FIRSTLEVEL_22   = 232;
        public static final int OBJPROP_FIRSTLEVEL_23   = 233;
        public static final int OBJPROP_FIRSTLEVEL_24   = 234;
        public static final int OBJPROP_FIRSTLEVEL_25   = 235;
        public static final int OBJPROP_FIRSTLEVEL_26   = 236;
        public static final int OBJPROP_FIRSTLEVEL_27   = 237;
        public static final int OBJPROP_FIRSTLEVEL_28   = 238;
        public static final int OBJPROP_FIRSTLEVEL_29   = 239;
        public static final int OBJPROP_FIRSTLEVEL_30   = 240;
        public static final int OBJPROP_FIRSTLEVEL_31   = 241;
    }
    public static class AlignMode
    {
        public static final int ALIGN_LEFT = 0x01;
        public static final int ALIGN_CENTER = 0x02;
        public static final int ALIGN_RIGHT = 0x00;
    }
    public static class BorderType
    {
        public static final int BORDER_FLAT = 0x00;
        public static final int BORDER_RAISED = 0x01;
        public static final int BORDER_SUNKEN = 0x02;
    }
}
