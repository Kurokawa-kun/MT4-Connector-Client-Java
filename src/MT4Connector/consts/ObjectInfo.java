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
        public static final int OBJ_CYCLES              =  0x14;
        public static final int OBJ_CHANNEL             =  0x05;
        public static final int OBJ_STDDEVCHANNEL       =  0x06;
        public static final int OBJ_REGRESSION          =  0x04;
        public static final int OBJ_PITCHFORK           =  0x13;
        public static final int OBJ_GANNLINE            =  0x07;
        public static final int OBJ_GANNFAN             =  0x08;
        public static final int OBJ_GANNGRID            =  0x09;
        public static final int OBJ_FIBO                =  0x0A;
        public static final int OBJ_FIBOTIMES           =  0x0B;
        public static final int OBJ_FIBOFAN             =  0x0C;
        public static final int OBJ_FIBOARC             =  0x0D;
        public static final int OBJ_FIBOCHANNEL         =  0x0F;
        public static final int OBJ_EXPANSION           =  0x0E;
        public static final int OBJ_RECTANGLE           =  0x10;
        public static final int OBJ_TRIANGLE            =  0x11;
        public static final int OBJ_ELLIPSE             =  0x12;
        public static final int OBJ_ARROW_THUMB_UP      =  0x1D;
        public static final int OBJ_ARROW_THUMB_DOWN    =  0x1E;
        public static final int OBJ_ARROW_UP            =  0x1F;
        public static final int OBJ_ARROW_DOWN          =  0x20;
        public static final int OBJ_ARROW_STOP          =  0x21;
        public static final int OBJ_ARROW_CHECK         =  0x22;
        public static final int OBJ_ARROW_LEFT_PRICE    =  0x23;
        public static final int OBJ_ARROW_RIGHT_PRICE   =  0x24;
        public static final int OBJ_ARROW_BUY           =  0x25;
        public static final int OBJ_ARROW_SELL          =  0x26;
        public static final int OBJ_ARROW               =  0x16;
        public static final int OBJ_TEXT                =  0x15;
        public static final int OBJ_LABEL               =  0x17;
        public static final int OBJ_BUTTON              =  0x19;
        public static final int OBJ_BITMAP              =  0x1A;
        public static final int OBJ_BITMAP_LABEL        =  0x18;
        public static final int OBJ_EDIT                =  0x1B;
        public static final int OBJ_EVENT               =  0x2A;
        public static final int OBJ_RECTANGLE_LABEL     =  0x1C;
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
        public static final int OBJPROP_TIME1           = 0x00;
        public static final int OBJPROP_PRICE1          = 0x01;
        public static final int OBJPROP_TIME2           = 0x02;
        public static final int OBJPROP_PRICE2          = 0x03;
        public static final int OBJPROP_TIME3           = 0x04;
        public static final int OBJPROP_PRICE3          = 0x05;
        public static final int OBJPROP_COLOR           = 0x06;
        public static final int OBJPROP_STYLE           = 0x07;
        public static final int OBJPROP_WIDTH           = 0x08;
        public static final int OBJPROP_BACK            = 0x09;
        public static final int OBJPROP_RAY             = 0x0A;
        public static final int OBJPROP_ELLIPSE         = 0x0B;
        public static final int OBJPROP_SCALE           = 0x0C;
        public static final int OBJPROP_ANGLE           = 0x0D;
        public static final int OBJPROP_ARROWCODE       = 0x0E;
        public static final int OBJPROP_TIMEFRAMES      = 0x0F;
        public static final int OBJPROP_DEVIATION       = 0x10;
        public static final int OBJPROP_FONTSIZE        = 0x64;
        public static final int OBJPROP_CORNER          = 0x65;
        public static final int OBJPROP_XDISTANCE       = 0x66;
        public static final int OBJPROP_YDISTANCE       = 0x67;
        public static final int OBJPROP_FIBOLEVELS      = 0xC8;
        public static final int OBJPROP_LEVELCOLOR      = 0xC9;
        public static final int OBJPROP_LEVELSTYLE      = 0xCA;
        public static final int OBJPROP_LEVELWIDTH      = 0xCB;
        public static final int OBJPROP_FIRSTLEVEL      = 0xD2;
        public static final int OBJPROP_FIRSTLEVEL_1    = 0xD3;
        public static final int OBJPROP_FIRSTLEVEL_2    = 0xD4;
        public static final int OBJPROP_FIRSTLEVEL_3    = 0xD5;
        public static final int OBJPROP_FIRSTLEVEL_4    = 0xD6;
        public static final int OBJPROP_FIRSTLEVEL_5    = 0xD7;
        public static final int OBJPROP_FIRSTLEVEL_6    = 0xD8;
        public static final int OBJPROP_FIRSTLEVEL_7    = 0xD9;
        public static final int OBJPROP_FIRSTLEVEL_8    = 0xDA;
        public static final int OBJPROP_FIRSTLEVEL_9    = 0xDB;
        public static final int OBJPROP_FIRSTLEVEL_10   = 0xDC;
        public static final int OBJPROP_FIRSTLEVEL_11   = 0xDD;
        public static final int OBJPROP_FIRSTLEVEL_12   = 0xDE;
        public static final int OBJPROP_FIRSTLEVEL_13   = 0xDF;
        public static final int OBJPROP_FIRSTLEVEL_14   = 0xE0;
        public static final int OBJPROP_FIRSTLEVEL_15   = 0xE1;
        public static final int OBJPROP_FIRSTLEVEL_16   = 0xE2;
        public static final int OBJPROP_FIRSTLEVEL_17   = 0xE3;
        public static final int OBJPROP_FIRSTLEVEL_18   = 0xE4;
        public static final int OBJPROP_FIRSTLEVEL_19   = 0xE5;
        public static final int OBJPROP_FIRSTLEVEL_20   = 0xE6;
        public static final int OBJPROP_FIRSTLEVEL_21   = 0xE7;
        public static final int OBJPROP_FIRSTLEVEL_22   = 0xE8;
        public static final int OBJPROP_FIRSTLEVEL_23   = 0xE9;
        public static final int OBJPROP_FIRSTLEVEL_24   = 0xEA;
        public static final int OBJPROP_FIRSTLEVEL_25   = 0xEB;
        public static final int OBJPROP_FIRSTLEVEL_26   = 0xEC;
        public static final int OBJPROP_FIRSTLEVEL_27   = 0xED;
        public static final int OBJPROP_FIRSTLEVEL_28   = 0xEE;
        public static final int OBJPROP_FIRSTLEVEL_29   = 0xEF;
        public static final int OBJPROP_FIRSTLEVEL_30   = 0xF0;
        public static final int OBJPROP_FIRSTLEVEL_31   = 0xF1;
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
