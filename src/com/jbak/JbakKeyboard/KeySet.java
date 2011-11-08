package com.jbak.JbakKeyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.KeyEvent;

public class KeySet
{
	int keycode = 0;
	char keychar = 0;
	int flags = 0;
	int action = 0;
	Intent extra = null;
	String sext = null;
	void setApp(ComponentName cn)
	{
		if(cn==null)
		{
			extra=null;
			return;
		}
		extra =new Intent();
		extra.putExtra(KEY_APP, 
			new Intent(Intent.ACTION_MAIN)
					.setComponent(cn)
					.addFlags(ACT_DEFAULT)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK)
					);
	}
	void setText(String text)
	{
		extra =new Intent().putExtra(KEY_TEXT, text);
	}
	ComponentName getApp()
	{
		if(extra==null)
			return null;
		Intent in = (Intent)extra.getParcelableExtra(KEY_APP);
		if(in==null)
			return null;
		return in.getComponent();
	}
	void run()
	{
		switch(action)
		{
			case ACT_RUN_APP:
				Intent in = (Intent)extra.getParcelableExtra(KEY_APP);
				if(in==null)
					return;
				try{
					st.c().startActivity(in);
				}
				catch (Throwable e) {
				}
				break;
				case ACT_TEXT:
					ServiceJbKbd.inst.onText(getText());
					break;
				case ACT_APP_DETAILS:
					showInstalledAppDetails(st.c(), ServiceJbKbd.inst.getCurrentInputEditorInfo().packageName);
					break;
		}
	}
	boolean checkEdit(boolean bEditor)
	{
		if(bEditor)
			return !st.has(flags, FLAG_NOT_IN_TEXT_FIELDS);
		else
			return !st.has(flags, FLAG_TEXT_FIELDS);
	}
	boolean isLong()
	{
		return st.has(flags, FLAG_LONG_PRESS);
	}
	String getText()
	{
		if(extra==null)
			return null;
		return extra.getStringExtra(KEY_TEXT);
	}
	static String getKeyName(int kcode)
	{
		int rid = 0;
		switch (kcode)
		{
			case KeyEvent.KEYCODE_MENU: rid = R.string.k_menu; break;
			case KeyEvent.KEYCODE_BACK: rid = R.string.k_back; break;
			case KeyEvent.KEYCODE_SEARCH: rid = R.string.k_search; break;
			case KeyEvent.KEYCODE_VOLUME_UP: rid = R.string.k_volumeUp; break;
			case KeyEvent.KEYCODE_VOLUME_DOWN: rid = R.string.k_volumeDown; break;
		}
		if(rid==0)
			return null;
		return st.c().getString(rid);
	}
	static public final int ACT_DEFAULT =0;
	static public final int ACT_RUN_APP =1;
	static public final int ACT_TEXT 	=2;
	static public final int ACT_APP_DETAILS =3;
	
	static public int FLAG_LONG_PRESS = 0x0000001;
	static public int FLAG_TEXT_FIELDS = 0x0000002;
	static public int FLAG_NOT_IN_TEXT_FIELDS = 0x0000004;
	
	static public final String KEY_APP = "ka";
	static public final String KEY_TEXT = "kt";
	/** ��������� ��� {@link #showInstalledAppDetails} */
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
/** ��������� ��� {@link #showInstalledAppDetails} */
    private static final String APP_PKG_NAME_22 = "pkg";
/** ��������� ��� {@link #showInstalledAppDetails} */
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
/** ��������� ��� {@link #showInstalledAppDetails} */
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
    public static void showInstalledAppDetails(Context c,String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        c.startActivity(intent);
    }
	
}

