package com.stupidbeauty.xapkinstaller.core;

import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;
import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.ZipFile;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import com.google.gson.Gson;
import java.io.OutputStream;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.view.View;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.os.Looper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.ResolveInfo;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.os.Build;
// import androidx.core.content.FileProvider;
import android.database.ContentObserver;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import android.database.ContentObserver;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import java.io.DataInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
// import android.content.ClipData;
import org.apache.commons.io.FileUtils;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
// import java.util.Collection;
import java.util.HashMap;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FilenameUtils;
import java.io.File;

public class XAPKInstaller
{
  private static final String PACKAGE_INSTALLED_ACTION = "com.example.android.apis.content.SESSION_API_PACKAGE_INSTALLED";
  private String fullUrl = null; //!< Downloading full url.
  private Notification continiusNotification=null; //!<记录的通知
  private int NOTIFICATION = 84951; //!< 通知编号。陈欣
  private long MinimalApkSize = 250; //!< minimal accepted apk file size.

  public Future<File> fileDownloadFuture; //!< The file download future.
  private NotificationManager mNM;
  private Context baseApplication = null; //!< 上下文对象。
  private static final String TAG="XAPKInstaller"; //!<输出调试信息了时使用的标记
  
  /**
  * Extract xapk file parts.
  */
  private XAPKManifest extractXapk(String downloadFilePath) 
  {
//     ArrayList<XAPKPart> result=new ArrayList();
    XAPKManifest result=null; // result
    
    try
    {
      //     Chen xin.

      ZipFile zipFile=new ZipFile(downloadFilePath);
      Log.d(TAG,"extractXapk, 123"); //Debug.
      
      Enumeration<ZipArchiveEntry> entries=zipFile.getEntries();
    
      while(entries.hasMoreElements())
      {
        ZipArchiveEntry entry=(ZipArchiveEntry)(entries.nextElement());
        
        String entryFioleName=entry.getName(); // Get file name.
        
        InputStream entryInputStream=zipFile.getInputStream(entry);
        
        //       Chen xin. open the inputstream to file.
  
        File downloadFolder = baseApplication.getExternalCacheDir();

        String wholePath =downloadFolder.getPath()+ File.separator  + entryFioleName; // Construct whole path.

        String entryFileFullName=wholePath;

        File entryFile=new File(entryFileFullName);
        
        FileUtils.copyInputStreamToFile(entryInputStream, entryFile);
      } // while(entries.hasMoreElements())
    
      //     Chen xin, parse manifest.json 
      Gson gson=new Gson(); //创建gson对象。

      File downloadFolder = baseApplication.getExternalCacheDir();

      String wholePath =downloadFolder.getPath()+ File.separator  + "manifest.json";
      File manifestFile=new File(wholePath);
    
      String text=FileUtils.readFileToString(manifestFile);
      
      Log.d(TAG,"extractXapk, 162, text: " + text); //Debug.

      XAPKManifest voiceRecognizeResult=gson.fromJson(text, XAPKManifest.class); // 解析成结果对象。

      Log.d(TAG,"extractXapk, 166, parse result: " + voiceRecognizeResult); //Debug.
      result=voiceRecognizeResult;
    }
    catch (IOException e) 
    {
      Log.d(TAG,"extractXapk, 165, exception here"); //Debug.
      e.printStackTrace();
    } // catch (IOException e) 

//     Log.d(TAG,"extractXapk, 168, result size: "+ result.size()); //Debug.
    
    return result;
  } // private ArrayList<XAPKPart> extractXapk(String downloadFilePath)
  
  /**
    * 要求安装应用
    * @param downloadFilePath 应用安装包路径
    */
  public boolean installXapk(String downloadFilePath, IntentSender statusReceiver)
  {
    boolean result=false;
    
    XAPKManifest xapkManifest=extractXapk(downloadFilePath);
    
    if (xapkManifest!=null) // extract success
    {
      ArrayList<XAPKPart> xapkParts=xapkManifest.getSplitApks(); // Get apk parts.
      
      int partAmount=xapkParts.size();
      
      if (partAmount>0) // Got something
      {
        boolean installExpansinCussess= installExpansions(xapkManifest); // Install expansions.
        
        if (installExpansinCussess)
        {
          try
          {
            requestInstallApiParts(xapkParts, statusReceiver); // Request install by view.
            
            result=true;
          }
          catch(RuntimeException e)
          {
            e.printStackTrace();
          }
        } // if (installExpansinCussess)
      } // if (partAmount>0) // Got something
    } // if (xapkManifest!=null) // extract success
    
    return result;
  } //private void requestInstall(String downloadFilePath)
  
    /**
    *   Goto file manager settings page.
    */
    private void gotoFileManagerSettingsPage()
    {
      Log.d(TAG, "gotoFileManagerSettingsPage"); //Debug.

      Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);  // 跳转语言和输入设备

      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      String packageNmae=baseApplication.getPackageName();
      Log.d(TAG, "gotoFileManagerSettingsPage, package name: " + packageNmae); //Debug.

      String url = "package:"+packageNmae;

      Log.d(TAG, "gotoFileManagerSettingsPage, url: " + url); //Debug.

      intent.setData(Uri.parse(url));

      baseApplication.startActivity(intent);
    } // private void gotoFileManagerSettingsPage()

    /**
    *  CheCK THE permission of file manager.
    */
    private void checkFileManagerPermission()
    {
      Log.d(TAG, "checkFileManagerPermission " ); //Debug.
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) // Android 11. isExternalStorageManager
      {
        boolean isFileManager=Environment.isExternalStorageManager();

        Log.d(TAG, "checkFileManagerPermission, is file manager: " + isFileManager ); //Debug.
        if (isFileManager) // Is file manager
        {
        } // if (isFileManager) // Is file manager
        else // Not file manager
        {
          // Chen xin
          gotoFileManagerSettingsPage(); // Goto file manager settings page.
        } // else // Not file manager
      }
    } // private void checkFileManagerPermission()

  /**
  * Install expansions.
  */
  private boolean installExpansions(XAPKManifest xapkManifest)
  {
    boolean result=false;
    ArrayList<XAPKExpansion> xapkParts=xapkManifest.getExpansions(); // Get expansions.
    
    if (xapkParts!=null)
    {
      checkFileManagerPermission(); // CheCK THE permission of file manager.
      int xapkPartCounter=0;
        
      int partAmount=xapkParts.size();
      try
      {
        for(xapkPartCounter=0; xapkPartCounter< partAmount; xapkPartCounter++) // request parts one by one.
        {
          XAPKExpansion xapkPart=xapkParts.get(xapkPartCounter);
          
          String partFiePath=xapkPart.getFile();
          String partId=xapkPart.getId();
          
          File downloadFolder = baseApplication.getExternalCacheDir();

          String wholePath =downloadFolder.getPath()+ File.separator  + partFiePath;
          
          String downloadFilePath= wholePath; // Get whole source path.

          Log.d(TAG,"requestInstallApiParts, 250, add part: " + partId); //Debug.
  //         addApkToInstallSession(downloadFilePath, session, partId);

  // 	public static final String LOG_BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"com.stupidbeauty.lanime"+File.separator+"Log"; //!<日志目录的路径。

          File externalDirectory=Environment.getExternalStorageDirectory();
          
          String installFiePath= xapkPart.getInstallPath(); // Get install path.
        
          String targetWhoelPath=externalDirectory.getPath()+ File.separator  + installFiePath; // target install file path. obb file.
          
          File sourceFileObb=new File(downloadFilePath); // Source file.
          File targetFileObb=new File(targetWhoelPath); // Target file.
          
          FileUtils.copyFile(sourceFileObb, targetFileObb); // Copy file.
        } // for(int xapkPartCounter=0; xapkPartCounter< xapkParts.length(); xapkPartCounter++)

        Log.d(TAG,"requestInstallApiParts, 256, commit "); //Debug.
  //       session.commit(statusReceiver);
  
        result=true;
      }
      catch (IOException e) 
      {
        Log.d(TAG,"installExpansions, 244, got exception"); //Debug.
        e.printStackTrace();
//         throw new RuntimeException("Couldn't install package", e);
      } // catch (IOException e) 
    } // if (xapkParts!=null)
    else // no expansion
    {
      result=true;
    } // else // true
    
    return result;
  } // private void installExpansions(XAPKManifest xapkManifest)
  
  /**
  * Request install split apks.
  */
  private void requestInstallApiParts(ArrayList<XAPKPart> xapkParts, IntentSender statusReceiver)
  {
    PackageInstaller.Session session = null;
    try
    {
      PackageInstaller packageInstaller = baseApplication.getPackageManager().getPackageInstaller();
      PackageInstaller.SessionParams params = new PackageInstaller.SessionParams( PackageInstaller.SessionParams.MODE_FULL_INSTALL);
      int sessionId = packageInstaller.createSession(params);
      session = packageInstaller.openSession(sessionId);

      int xapkPartCounter=0;
      
      int partAmount=xapkParts.size();
      for(xapkPartCounter=0; xapkPartCounter< partAmount; xapkPartCounter++) // request parts one by one.
      {
        XAPKPart xapkPart=xapkParts.get(xapkPartCounter);
        
        String partFiePath=xapkPart.getFile();
        String partId=xapkPart.getId();
        
        File downloadFolder = baseApplication.getExternalCacheDir();

        String wholePath =downloadFolder.getPath()+ File.separator  + partFiePath;
        
        String downloadFilePath= wholePath;

        Log.d(TAG,"requestInstallApiParts, 250, add part: " + partId); //Debug.
        addApkToInstallSession(downloadFilePath, session, partId);
      } // for(int xapkPartCounter=0; xapkPartCounter< xapkParts.length(); xapkPartCounter++)

      Log.d(TAG,"requestInstallApiParts, 256, commit "); //Debug.
      session.commit(statusReceiver);
    }
    catch (IOException e) 
    {
      throw new RuntimeException("Couldn't install package", e);
    }
    catch (RuntimeException e) // Other runtime exceptions
    {
      if (session != null) 
      {
        session.abandon();
      }
      throw e;
    } // catch (RuntimeException e) // Other runtime exceptions
  } // private void requestInstallApiParts(ArrayList<XAPKPart> xapkParts, IntentSender statusReceiver)

  private void addApkToInstallSession(String assetName, PackageInstaller.Session session, String partId) throws IOException 
  {
    // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
    // if the disk is almost full.

    OutputStream packageInSession = session.openWrite(partId, 0, -1);
        
    File apkFileObject=new File(assetName);
            
    FileUtils.copyFile(apkFileObject, packageInSession); // Copy file.
//     byte[] buffer=FileUtils.readFileToByteArray(apkFileObject);
//             
//     int n=buffer.length;
//             
//     packageInSession.write(buffer, 0, n);
                
    packageInSession.close();
  } // private void addApkToInstallSession(String assetName, PackageInstaller.Session session, String partId) throws IOException 

  public XAPKInstaller(Context context) 
  {
    baseApplication = context; // 获取 context 对象。
  } // public XAPKInstaller(Context context) 
}
