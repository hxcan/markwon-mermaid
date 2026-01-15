package com.stupidbeauty.xapkinstaller.core;

import android.util.Log;
// import android.app.Application;
import android.os.Looper;
// import com.stupidbeauty.voiceui.VoiceUi;
import android.content.pm.PackageInfo;
import android.os.Environment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.io.File;

/**
 * 一些基本信息的定义。
 * @author root
 *
 */
class XAPKManifest
{
  private static final String TAG="XAPKManifest"; //!< 输出调试信息了时使用的标记
	private ArrayList<XAPKPart> splitApks; //!< split apks list.
	private ArrayList<XAPKExpansion> expansions; //!< expansions list.
	private ArrayList<XAPKPart> split_apks; //!< split apks list.

	public ArrayList<XAPKPart> getSplitApks()
	{
    ArrayList<XAPKPart> result;
    if (splitApks!=null)
    {
      Log.d(TAG,"getSplitApks, 26, splitApks"); //Debug.
      result=splitApks;
    }
    else
    {
      Log.d(TAG,"getSplitApks, 26, split_apks"); //Debug.
      result=split_apks;
    }
	
    return result;
	} // public ArrayList<XAPKPart> getSplitApks()

		public ArrayList<XAPKExpansion> getExpansions()
	{
    return expansions;
	} // public ArrayList<XAPKPart> getSplitApks()
}
