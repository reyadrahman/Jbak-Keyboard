package com.jbak.JbakKeyboard;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;

import com.jbak.JbakKeyboard.IKeyboard.Keybrd;
import com.jbak.JbakKeyboard.IKeyboard.Lang;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;
import android.util.Xml;

public class CustomKeyboard extends JbKbd
{
    public static final String KEYBOARD_FOLDER = "keyboards";
    public static final String A_ANDROID = "android:";
    public static final String TAG_KEYBOARD = "Keyboard";
    public static final String VAL_PERCENT = "%p";
    public static final String VAL_PIXELS = "px";
    public static final String TAG_ROW = "Row";
    public static final String TAG_KEY = "Key";
    public static final String TAG_REPLACE = "Replace";
    public static final String DRW_PREFIX = "@drawable/sym_keyboard_";
    public static final String A_keyWidth = "keyWidth";
    public static final String A_keyHeight = "keyHeight";
    public static final String A_verticalGap = "verticalGap";
    public static final String A_horizontalGap = "horizontalGap";
    
    public static final String A_codes="codes";     
    public static final String A_iconPreview="iconPreview"; 
    public static final String A_isModifier="isModifier";   
    public static final String A_isRepeatable="isRepeatable";   
    public static final String A_isSticky="isSticky";       
    public static final String A_keyEdgeFlags="keyEdgeFlags";   
    public static final String A_keyIcon="keyIcon";     
    public static final String A_keyLabel="keyLabel";       
    public static final String A_keyOutputText="keyOutputText"; 
    public static final String A_longKeyOutputText="longKeyOutputText"; 
    public static final String A_popupCharacters="popupCharacters"; 
    public static final String A_popupKeyboard="popupKeyboard";
/** Текстовая метка, рисующаяся мелким шрифтом по центру клавиши с разбиением на строки */    
    public static final String A_smallLabel="smallLabel";
/** Код, срабатывающий по удержанию клавиши */    
    public static final String A_upCode="longCode";   
/** Аттрибут, bool. Если true - на заднем плане клавиши рисуется фон 2, иначе - 1 */    
    public static final String A_specKey="specKey";   
/** Аттрибут, bool. Если true - иконка не окрашивается в цвет текста скина */    
    public static final String A_noColor="noColor";   
/** Аттрибут, bool. Если true - по нажатию клавиши происходит переход к qwerty-клавиатуре, false - переход не происходит*/
    public static final String A_goQwerty="goQwerty";   
    public static final String A_From="from";   
    public static final String A_To="to";   
    public static final String A_Template="template";   
    public static final String A_TemplateLong="longTemplate";   
    public static final String A_Keyboard="keyboard";   
    public static final String A_KeyboardLong="longKeyboard";   

    public static final  byte B_keyWidth   = 1;
    public static final  byte B_keyHeight  = 2;
    public static final  byte B_verticalGap  = 3;
    public static final  byte B_horizontalGap = 4;
    public static final  byte B_codes=5;     
    public static final  byte B_iconPreview=6; 
    public static final  byte B_isModifier=7;   
    public static final  byte B_isRepeatable=8;   
    public static final  byte B_isSticky=9;       
    public static final  byte B_keyEdgeFlags=10;   
    public static final  byte B_keyIcon=11;     
    public static final  byte B_keyLabel=12;       
    public static final  byte B_keyOutputText=13; 
    public static final  byte B_popupCharacters=14; 
    public static final  byte B_popupKeyboard=15;
    public static final  byte B_upCode=16;   
    public static final  byte B_specKey=17;
    public static final  byte B_smallLabel=18;
    public static final  byte B_noColor=19;
    public static final  byte B_longKeyOutputText=20;
    public static final  byte B_goQwerty=21;
    public static final  byte B_from=22;
    public static final  byte B_to=23;
    public static final  byte B_keyboard=24;
    public static final  byte B_keyboardLong=25;
    public static final  byte B_template=26;
    public static final  byte B_templateLong=27;
    
    public static final  byte BA_KBD=(byte)'|';
    public static final  byte BA_ROW=58;//(byte)':'
    public static final  byte BA_KEY=(byte)'k';
    int m_displayWidth;
    int m_displayHeight;
    int m_x = 0;
    int m_y = 0;
    int m_rowHeight;
    float m_fraction = 0f;
    float m_globalFraction = 0f;
    List<Key> m_keys;
    Row m_row = null;
    Context m_context;
    boolean m_bBrokenLoad = false;
    DisplayMetrics m_dm;
    Field m_totalHeight;
    static DataOutputStream m_os = null;
    public CustomKeyboard(Context context,Keybrd kbd)
    {
        super(context, kbd);
        try{
            String totalHeight = "mTotalHeight";
            m_totalHeight = Keyboard.class.getDeclaredField(totalHeight);
            m_totalHeight.setAccessible(true);
        }
        catch (Throwable e) {
            m_totalHeight = null;
        }
        m_context = context;
        m_dm = context.getResources().getDisplayMetrics();
        m_displayWidth = m_dm.widthPixels;
        m_displayHeight = m_dm.heightPixels;
        m_keys = getKeys();

        try{
            
            if(kbd.kbdCode==st.KBD_COMPILED)
            {
                AssetManager am = context.getResources().getAssets();
                makeCompiledKeyboard(new DataInputStream(new BufferedInputStream(am.open(kbd.path))));
                
            }
            else
            {
                XmlPullParser xp = Xml.newPullParser();
                FileInputStream fs = new FileInputStream(kbd.path);
                xp.setInput(new BufferedInputStream(fs), null);
                makeKeyboard(xp);
            }
        }
        catch (Throwable e) 
        {
            m_bBrokenLoad = true;
            st.logEx(e);
        }
    }
    void makeCompiledKeyboard(DataInputStream is)
    {
        try{
            byte b = is.readByte();
            boolean bConvert = m_os!=null;
            if(bConvert)
            {
                m_os.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n".getBytes());
            }
            b = bConvert?parseKeyboardWithConvert(is):parseKeyboard(is);
            do
            {
                switch (b)
                {
                    case BA_ROW:
                        if(m_row!=null)
                            m_y+=m_row.defaultHeight+m_row.verticalGap;
                        if(bConvert)
                        {
                            if(m_row!=null)
                                m_os.write(" </Row>\n".getBytes());
                            m_os.write(" <Row>\n".getBytes());
                            m_row = new Row(this); 
                        }
                        else
                            parseRow(null);
                            
                        b = is.readByte();
                    break;
                    case BA_KEY:
                        b = bConvert?parseKeyWithConvert(is):parseKey(is);
                    break;
                    default:
                        b = is.readByte();
                    break;
                }
            }
            while(b!=BA_KBD);
            if(bConvert)
            {
                m_os.write(" </Row>\n".getBytes());
                m_os.write("</Keyboard>\n".getBytes());
            }
            m_y+=m_row.defaultHeight+m_row.verticalGap;
        }
        catch (Throwable e) {
            m_bBrokenLoad = true;
            st.logEx(e);
        }
        postProcessKeyboard();
    }
    void makeKeyboard(XmlPullParser parser)
    {
        try
        {
            int eventType = parser.getEventType();
            boolean done = false;
            List <Key> keys = getKeys();
            if(m_os!=null)
                m_os.writeByte(BA_KBD);
            while (eventType != XmlPullParser.END_DOCUMENT && !done)
            {
                String name = null;
                switch (eventType)
                {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if(name.equals(TAG_KEYBOARD))
                            parseKeyboard(parser);
                        else if(name.equals(TAG_ROW))
                            parseRow(parser);
                        else if(name.equals(TAG_KEY))
                            parseKey(parser, keys);
                        else if(name.equals(TAG_REPLACE))
                            parseReplace(parser);
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if(name.equals(TAG_KEYBOARD))
                        {
                            if(m_os!=null)
                                m_os.writeByte(BA_KBD);
                        }
                        if(name.equals(TAG_ROW))
                        {
                            m_y+=m_row.defaultHeight+m_row.verticalGap;
                        }
                        break;
                }
                eventType = parser.next();
            }
        }
        catch (Throwable e) 
        {
            st.logEx(e);
        }
        try
        {
            postProcessKeyboard();
            if(m_os!=null)
            {
                m_os.flush();
                m_os.close();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        
    }
    void postProcessKeyboard()
    {
        try{
            String totalWidth = "mTotalWidth";
            String modKeys = "mModifierKeys";
            Field f;
            f = Keyboard.class.getDeclaredField(totalWidth);
            f.setAccessible(true);
            f.set(this, m_displayWidth);
            m_totalHeight.set(this, m_y-getVerticalGap());
            f = Keyboard.class.getDeclaredField(modKeys);
            f.setAccessible(true);
        }
        catch(Throwable e)
        {
            st.logEx(e);
        }
    }
    final byte parseKeyboardWithConvert(DataInputStream is)throws IOException
    {
        String out = "<"+TAG_KEYBOARD+" ";
        byte b = 0;
        do
        {
            b = is.readByte();
            if(b==BA_ROW)
                break;
            switch (b)
            {
                case B_keyWidth:
                    out+=A_ANDROID+A_keyWidth+"=\""+is.readFloat()+"%p\" ";
                    break;
                default:
                    is.readFloat();
                break;
            }
        }
        while (b<BA_ROW);
        out+=">\n";
        m_os.write(out.getBytes());
        return b;
    }
    final byte parseKeyboard(DataInputStream is)throws IOException
    {
        byte b = 0;
        do
        {
            b = is.readByte();
            if(b==BA_ROW)
                break;
            switch (b)
            {
                case B_keyWidth:
                	float w = getPercentSizeFloat(is);
                    setKeyWidth(floatToInt(w));
                    m_globalFraction = floatToIntFraction(w);
                    break;
                default:
                    is.readFloat();
                break;
            }
        }
        while (b<BA_ROW);
        return b;
    }
    final boolean parseKeyboard(XmlPullParser p) throws IOException
    {
        int cnt = p.getAttributeCount();
        for(int i=0;i<cnt;i++)
        {
            String name = attName(p, i);
            if(name.equals(A_keyWidth))
            {
                float sz = getSizeFloat(p.getAttributeValue(i),m_displayWidth,m_displayWidth/10,B_keyWidth);
                setKeyWidth(floatToInt(sz));
                m_globalFraction = floatToIntFraction(sz);
            }
            else if(name.equals(A_keyHeight))
                setKeyWidth(getSize(p.getAttributeValue(i),m_displayHeight,50,B_keyHeight));
            else if(name.equals(A_verticalGap))
                setVerticalGap(getSize(p.getAttributeValue(i),m_displayWidth,0,B_verticalGap));
            else if(name.equals(A_horizontalGap))
                setHorizontalGap(getSize(p.getAttributeValue(i),m_displayHeight,0,B_horizontalGap));
        }
        return true;
    }
    final boolean parseRow(XmlPullParser p) throws IOException
    {
        m_x = 0;
        m_fraction = 0f;
        if(m_os!=null)
        {
            m_os.writeByte(BA_ROW);
        }
        m_row = new Row(this);
        m_row.defaultWidth = getKeyWidth();
        m_row.defaultHorizontalGap = 0;
        m_row.verticalGap = 0;
        m_row.defaultHeight = getKeyHeight();
        if(p!=null)
        {
            for(int i=p.getAttributeCount()-1;i>=0;i--)
            {
                String name = p.getAttributeName(i);
                if(A_keyHeight.equals(name))
                {
                    m_row.defaultHeight = getSize(p.getAttributeValue(i), m_row.defaultHeight, m_row.defaultHeight, B_keyHeight);
                }
                if(A_verticalGap.equals(name))
                {
                    m_row.verticalGap = getSize(p.getAttributeValue(i), m_displayHeight, m_row.verticalGap, B_verticalGap);
                }
            }
        }
        return true;
    }
    LatinKey newKey()
    {
        LatinKey k = new LatinKey(m_row);
        k.width = getKeyWidth();
        k.height = m_row.defaultHeight;
        k.gap = getHorizontalGap();
        k.x = m_x;
        k.y = m_y+m_row.verticalGap;
        k.pressed = false;
        k.on = false;
        return k;
    }
    final void processKey(LatinKey k)
    {
        k.x+=k.gap;
        try{
        k.init(m_row);
        }
        catch(Throwable e)
        {
            
        }
        if(k.codes!=null&&k.codes.length>0&&k.codes[0]==KEYCODE_SHIFT)
            setShiftKey(m_keys.size(), k);
        m_keys.add(k);
        m_x+=k.gap+k.width;
    }
    final byte parseKeyWithConvert(DataInputStream is) throws IOException
    {
        String out = "  <Key ";
        LatinKey k = newKey();
        byte b = 0;
        do{
            b = is.readByte();
            switch(b)
            {
                case B_keyWidth:
                    out+=A_ANDROID+A_keyWidth+"=\""+is.readFloat()+"%p\" ";
                    k.width = 10;//getPercentSize(is);
                    break;
                case B_codes:
                    out+=A_ANDROID+A_codes+"=\""+is.readUTF()+"\" ";
                    break;
                case B_upCode:
                    out+=A_ANDROID+A_upCode+"=\""+is.readInt()+"\" ";

//                    k.longCode = is.readInt();
                    break;
                case B_keyLabel:
                    String label = is.readUTF();
                    String mk = "";
                    for(int i=0;i<label.length();i++)
                    {
                        char ch = label.charAt(i);
                        switch(ch)
                        {
                            case '\n': mk+="\\n"; break;
                            case '&': mk+="&amp;";break;
                            case '<': mk+="&lt;";break;
                            case '>': mk+="&gt;";break;
                            case '\"': mk+="&guot;";break;
                            case '\'': mk+="\\'";break;
                            default: mk+=ch;
                        }
                    }
                    out+=A_ANDROID+A_keyLabel+"=\""+mk+"\" ";
                    break;
                case B_smallLabel:
                    out+=A_ANDROID+A_smallLabel+"=\""+(is.readBoolean()?"true":"false")+"\" ";
                    break;
                case B_keyIcon:
                    out+=A_ANDROID+A_keyIcon+"=\""+is.readUTF()+"\" ";
                    break;
                case B_isSticky:
                    out+=A_ANDROID+A_isSticky+"=\""+(is.readBoolean()?"true":"false")+"\" ";
                    break;
                case B_isRepeatable:
                    out+=A_ANDROID+A_isRepeatable+"=\""+(is.readBoolean()?"true":"false")+"\" ";
                    break;
                case B_specKey:    
                    out+=A_ANDROID+A_specKey+"=\""+(is.readBoolean()?"true":"false")+"\" ";
                    break;
                case B_horizontalGap:    
                    out+=A_ANDROID+A_horizontalGap+"=\""+is.readFloat()+"%p\" ";
                    break;
            }
        }
        while(b<BA_ROW);
        out+="/>\n";
        processKey(k);
        m_os.write(out.getBytes());
        return b;
    }
    final byte parseKey(DataInputStream is) throws IOException
    {
        LatinKey k = newKey();
        boolean setWidth = false;
        byte b = 0;
        do{
            b = is.readByte();
            switch(b)
            {
                case B_keyWidth:
                	float w = getPercentSizeFloat(is);
                    k.width = floatToInt(w);
                    m_fraction+=floatToIntFraction(w);
                    setWidth = true;
                    break;
                case B_codes:
                    k.codes = parseCodes(is.readUTF());
                    break;
                case B_template:
                    k.mainText = is.readUTF();
                    k.flags|=LatinKey.FLAG_USER_TEMPLATE;
                    break;
                case B_keyboard:
                    k.mainText = is.readUTF();
                    k.flags|=LatinKey.FLAG_USER_KEYBOARD;
                    break;
                case B_templateLong:   
	                k.longText = is.readUTF();
	                k.flags|=LatinKey.FLAG_USER_TEMPLATE_LONG;
	                break;
                case B_keyboardLong:   
	                k.longText = is.readUTF();
	                k.flags|=LatinKey.FLAG_USER_KEYBOARD_LONG;
	                break;
                case B_upCode:
                    k.longCode = is.readInt();
                    break;
                case B_keyLabel:
                    k.label = is.readUTF();
                    break;
                case B_smallLabel:
                    k.smallLabel = is.readBoolean();
                    break;
                case B_goQwerty:
                    if(is.readBoolean())
                        k.flags|=LatinKey.FLAG_GO_QWERTY;
                    else    
                        k.flags|=LatinKey.FLAG_NOT_GO_QWERTY;
                    break;
                case B_keyIcon:
                    k.icon = getDrawable(is.readUTF(),true);
                    break;
                case B_isSticky:
                    k.sticky = is.readBoolean();
                    break;
                case B_isRepeatable:
                    k.repeatable = is.readBoolean();
                    break;
                case B_specKey:    
                    k.specKey = is.readBoolean()?1:0;
                    break;
                case B_horizontalGap:    
                	float g = getPercentSizeFloat(is);
                	k.gap  = floatToInt(g);
                    m_fraction+=floatToIntFraction(g);
                    if(m_fraction>1f)
                    {
                    	k.gap++;
                    	m_fraction-=1f;
                    }
                    	
                    break;
                case B_keyOutputText:    
                    k.text = is.readUTF();
                    break;
                case B_longKeyOutputText:    
                    k.longText = is.readUTF();
                    break;
            }
        }
        while(b<BA_ROW);
        if(!setWidth)
        	m_fraction+=m_globalFraction;
        if(m_fraction>1f)
        {
        	k.width++;
        	m_fraction-=1;
        }
        processKey(k);
        return b;
    }
    final boolean parseReplace(XmlPullParser p)
    {
        int cnt = p.getAttributeCount();
        String from=null,to=null;
        for(int i=0;i<cnt;i++)
        {
            String name = attName(p, i);
            if(name.equals(A_From))
            {
                from = p.getAttributeValue(i);
            }
            else if(name.equals(A_To))
            {
                to = p.getAttributeValue(i);
            }
            
        }
        if(from==null||to==null)
            return false;
        arReplacement.add(new Replacement(from, to));
        return true;
        
    }
    final void processKeyPercentWidth(float width,LatinKey k)
    {
    	k.width = floatToInt(width);
    	m_fraction+=floatToIntFraction(width);
    	if(m_fraction>=1f)
    	{
    		k.width++;
    		m_fraction-=1f;
    	}
    }
    final boolean parseKey(XmlPullParser p, List<Key> keys) throws IOException
    {
        LatinKey k = newKey();
        if(m_os!=null)
            m_os.writeByte(BA_KEY);
        boolean setWidth = false;
        int cnt = p.getAttributeCount();
        for(int i=0;i<cnt;i++)
        {
            String name = attName(p, i);
            if(name.equals(A_keyWidth))
            {
            	setWidth = true;
            	float sz = getSizeFloat(p.getAttributeValue(i), m_displayWidth, getKeyWidth(),B_keyWidth); 
                k.width = floatToInt(sz);
                m_fraction+=floatToIntFraction(sz);
                
            }
            else if(name.equals(A_keyHeight))
                k.height = getSize(p.getAttributeValue(i), m_displayHeight, getKeyWidth(),B_keyHeight);
            else if(name.equals(A_codes))
                k.codes = parseCodes(p.getAttributeValue(i));
            else if(name.equals(A_upCode))
            {
                k.longCode = Integer.decode(p.getAttributeValue(i));
                if(m_os!=null)
                {
                    m_os.writeByte(B_upCode);
                    m_os.writeInt(k.longCode);
                }
            }
            else if(name.equals(A_keyLabel))
                k.label = processLabel(p.getAttributeValue(i));
            else if(name.equals(A_noColor))
                k.noColorIcon = getBoolean(p.getAttributeValue(i), B_noColor);
            else if(name.equals(A_horizontalGap))
            {
            	float g = getSizeFloat(p.getAttributeValue(i), m_displayWidth, getHorizontalGap(),B_horizontalGap); 
                k.gap = floatToInt(g);
                m_fraction+=floatToIntFraction(g);
                if(m_fraction>=1f)
                {
                	k.gap++;
                	m_fraction-=1f;
                }
            }
            else if(name.equals(A_keyIcon))
                k.icon = getDrawable(p.getAttributeValue(i),false);
            else if(name.equals(A_isSticky))
                k.sticky = getBoolean(p.getAttributeValue(i),B_isSticky);
            else if(name.equals(A_smallLabel))
                k.smallLabel = getBoolean(p.getAttributeValue(i),B_smallLabel);
            else if(name.equals(A_isRepeatable))
                k.repeatable = getBoolean(p.getAttributeValue(i),B_isRepeatable);
            else if(name.equals(A_specKey))
                k.specKey = getBoolean(p.getAttributeValue(i),B_specKey)?1:0;
            else if(name.equals(A_popupCharacters))
            {
                k.popupResId = R.xml.kbd_empty;
                k.popupCharacters =p.getAttributeValue(i);
            } 
            else if(name.equals(A_Keyboard))
            {
            	k.mainText = getString(p.getAttributeValue(i),B_keyboard);
            	k.flags|=LatinKey.FLAG_USER_KEYBOARD;
            }
            else if(name.equals(A_KeyboardLong))
            {
            	k.longText = getString(p.getAttributeValue(i),B_keyboardLong);
            	k.flags|=LatinKey.FLAG_USER_KEYBOARD_LONG;
            }
            else if(name.equals(A_Template))
            {
            	k.mainText = getString(p.getAttributeValue(i),B_template);
            	k.flags|=LatinKey.FLAG_USER_TEMPLATE;
            }
            else if(name.equals(A_TemplateLong))
            {
            	k.longText = getString(p.getAttributeValue(i),B_templateLong);
            	k.flags|=LatinKey.FLAG_USER_TEMPLATE_LONG;
            }
            
            else if(name.equals(A_keyOutputText))
                k.mainText = getString(p.getAttributeValue(i),B_keyOutputText);
            else if(name.equals(A_longKeyOutputText))
                k.longText = getString(p.getAttributeValue(i),B_longKeyOutputText);
            else if(name.equals(A_goQwerty))
                k.setGoQwerty(getBoolean(p.getAttributeValue(i),B_goQwerty));
        }
        if(!setWidth)
        	m_fraction+=m_globalFraction;
        if(m_fraction>1f)
        {
        	k.width++;
        	m_fraction-=1f;
        }
        processKey(k);
        return true;
    }
    public String processLabel(String label) throws IOException
    {
        for(int i=0;i<label.length();i++)
        {
            if(label.charAt(i)=='\\'&&i<label.length()-1)
            {
                char c = label.charAt(i+1);
                switch (c)
                {
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    default:
                    break;
                }
                label = label.substring(0,i)+c+label.substring(i+2);
            }
        }
        if(m_os!=null)
        {
            m_os.writeByte(B_keyLabel);
            m_os.writeUTF(label);
        }
        return label;
    }
    private int[] parseCodes(String attributeValue) throws IOException
    {
        attributeValue.trim();
        if(m_os!=null)
        {
            m_os.writeByte(B_codes);
            m_os.writeUTF(attributeValue);
        }
        String a[] = attributeValue.split(",");
        int cd[] = new int[a.length];
        for(int i=0;i<a.length;i++)
            cd[i]=Integer.decode(a[i]);
        return cd;
    }
    final String attName(XmlPullParser p,int index)
    {
        String name = p.getAttributeName(index);
        if(name.startsWith(A_ANDROID))
            return name.substring(A_ANDROID.length());
        return name;
    }
    final int floatToInt(float val)
    {
        return (int)val;
    }
    final float floatToIntFraction(float val)
    {
    	return val - floatToInt(val);
    }
    float getSizeFloat(String att,float percentBase,int defValue,byte type)throws IOException
    {
        float ret = defValue;
        if(att.endsWith(VAL_PERCENT))
        {
            String v = att.substring(0,att.length()-VAL_PERCENT.length());
            if(m_os!=null)
            {
                m_os.writeByte(type);
                m_os.writeFloat(Float.valueOf(v));
            }
            ret = (float) (Float.valueOf(v)*percentBase/100f);
        }
        else if(att.endsWith(VAL_PIXELS))
        {
            String v = att.substring(0,att.length()-VAL_PIXELS.length());
            if(m_os!=null)
            {
                m_os.writeByte(type);
                m_os.writeFloat(Float.valueOf(v));
            }
            ret = Float.valueOf(v);
        }
        return ret;
    }
    int getSize(String att,float percentBase,int defValue,byte type) throws IOException
    {
        return floatToInt(getSizeFloat(att, percentBase, defValue, type));
    }
    final boolean getBoolean(String att,byte type) throws IOException
    {
        boolean b = (att.compareToIgnoreCase("true")==0);
        if(m_os!=null)
        {
            m_os.writeByte(type);
            m_os.writeBoolean(b);
        }
        return b;
    }
    final String getString(String att,byte type) throws IOException
    {
        if(m_os!=null)
        {
            m_os.writeByte(type);
            m_os.writeUTF(att);
        }
        return att;
    }
    final Drawable getDrawable(String att,boolean bCompiled) throws IOException
    {
        if(att.startsWith(DRW_PREFIX))
            att = att.substring(DRW_PREFIX.length());
        else if(!bCompiled)
            return loadFileDrawable(att);
        if(m_os!=null)
        {
            m_os.writeByte(B_keyIcon);
            m_os.writeUTF(att);
        }
        if(att.equals("delete")) return st.paint().getBitmap(R.drawable.sym_keyboard_delete);
        if(att.equals("done")) return st.paint().getBitmap(R.drawable.sym_keyboard_done);
        if(att.equals("return")) return st.paint().getBitmap(R.drawable.sym_keyboard_return);
        if(att.equals("shift")) return st.paint().getBitmap(R.drawable.sym_keyboard_shift);
        if(att.equals("space")) return st.paint().getBitmap(R.drawable.sym_keyboard_space);    
        if(att.equals("search")) return st.paint().getBitmap(R.drawable.sym_keyboard_search);    
        return null;
    }
    private Drawable loadFileDrawable(String att)
    {
        String path = att;
        if(!att.startsWith("/"))
        {
            path = st.getSettingsPath()+KEYBOARD_FOLDER+"/"+att;
        }
        return st.paint().getBitmap(path);
    }
    void setShiftKey(int index,Key key)
    {
        try{
            Field f;
            f = Keyboard.class.getDeclaredField("mShiftKey");
            f.setAccessible(true);
            f.set(this, key);
            f = Keyboard.class.getDeclaredField("mShiftKeyIndex");
            f.setAccessible(true);
            f.set(this, index);
        }
        catch (Throwable e) {
        }
    }
    public static String loadCustomKeyboards(boolean bCompile)
    {
        try{
            String path = st.getSettingsPath()+KEYBOARD_FOLDER;
            Vector<Keybrd> arKb = new Vector<Keybrd>();
            File f = new File(path);
            if(!f.exists())
            {
                f.mkdirs();
                return null;
            }
            File keyboards[] = st.getFilesByExt(f, st.EXT_XML);
            if(keyboards==null||keyboards.length==0)
                return null;
            for(File kf:keyboards)
            {
                Keybrd k = processCustomKeyboardFile(kf,bCompile);
                if(k!=null)
                    arKb.add(k);
            }
            if(arKb.size()==0)
                return null;
            int pos = 0;
            for(Keybrd k:st.arKbd)
            {
                if(k.kbdCode==st.KBD_CUSTOM)
                    break;
                ++pos;
            }
            Keybrd ak []=new Keybrd[pos+arKb.size()];
            System.arraycopy(st.arKbd, 0, ak, 0, pos);
            for(Keybrd k:arKb)
            {
                ak[pos]=k;
                ++pos;
            }
            st.arKbd = ak;
        }
        catch(Throwable e)
        {
            
        }
        return null;
    }
    public final float getPercentSizeFloat(DataInputStream is) throws IOException
    {
        float f = is.readFloat();
        return f*(float)m_displayWidth/100f;
    }
    public int getPercentSize(DataInputStream is) throws IOException
    {
        return floatToInt(getPercentSizeFloat(is));
    }
    public static Keybrd processCustomKeyboardFile(File kf,boolean bCompile)
    {
        if(!kf.exists()||!kf.canRead())
            return null;
        String name = kf.getName();
        int f = name.indexOf('_');
        if(f<0)
            return null;
        name = name.substring(0,f);
        Lang lng = null;
        for(Lang l:st.arLangs)
        {
            if(l.name.equals(name))
            {
                lng = l;
                break;
            }
        }
        if(lng==null)
            lng = st.addCustomLang(name);
        Keybrd kb = new Keybrd(st.KBD_CUSTOM, lng, R.xml.kbd_empty, 0);
        kb.path = kf.getAbsolutePath();
        if(bCompile)
            compileKeyboard(kf, kb);
        return kb;
    }
    static boolean compileKeyboard(File kf,Keybrd kb)
    {
        String path = kf.getParentFile().getAbsolutePath()+"/compiled";
        new File(path).mkdirs();
        path+="/"+kf.getName().substring(0,kf.getName().length()-4);
        return convertToCompiledFormat(st.c(), kb, path);

    }
    static boolean convertToCompiledFormat(Context c,Keybrd kbd,String xmlPath)
    {
        try{
            File f = new File(xmlPath);
            f.delete();
            f.createNewFile();
            m_os = new DataOutputStream(new FileOutputStream(f));
            new CustomKeyboard(c, kbd);
            m_os = null;
            return true;
        }
        catch (Throwable e) {
        }
        return false;
    }
    boolean setTopSpace(int space)
    {
        int diff = space-m_gap;
        if(diff==0)
            return false;
        m_gap = space;
        try{
            m_totalHeight.setInt(this, getHeight()+diff);
            for(Key k:getKeys())
                k.y+=diff;
            return true;
        }
        catch (Throwable e) {
        }
        return false;
    }
    int m_gap=0;
}
