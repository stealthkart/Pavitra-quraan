package com.scalosphere.labs.kquran.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.scalosphere.labs.kquran.common.Response;
import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.data.QuranDataProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class QuranFileUtils {
  private static final String TAG = "QuranFileUtils";
  /*
  Original url and file paths
  public static final String IMG_HOST = "http://android.quran.com/data/"; -- original
   private static final String QURAN_BASE = "quran_android/";
  */

  public static final String IMG_HOST = "http://www.pavitra-quraan.com/" ;
  private static final String QURAN_BASE = "kquraan/";
  private static final String DATABASE_DIRECTORY = "db";
  private static final int BUFF_SIZE = 1024;

  private static final int DEFAULT_READ_TIMEOUT = 20 * 1000; // 20s
  private static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000; // 15s

  // Database files in the Assests
  public static final String Arabic_Asset_DB = "db/quran.ar.db";
  public static final String Kannada_Asset_DB = "db/quran.kan.hamzah.db";

  public static boolean debugRmDir(String dir, boolean deleteDirectory) {
    File directory = new File(dir);
    if (directory.isDirectory()) {
      String[] children = directory.list();
      for (String s : children) {
        if (!debugRmDir(dir + File.separator + s, true))
          return false;
      }
    }

    return !deleteDirectory || directory.delete();
  }

  public static void debugLsDir(String dir) {
    File directory = new File(dir);
    Log.d(TAG, directory.getAbsolutePath());

    if (directory.isDirectory()) {
      String[] children = directory.list();
      for (String s : children)
        debugLsDir(dir + File.separator + s);
    }
  }

  // check if the images with the given width param have a version
  // that we specify (ex if version is 3, check for a .v3 file).
  public static boolean isVersion(Context context,
                                  String widthParam, int version) {
    String quranDirectory = getQuranDirectory(context, widthParam);
    if (quranDirectory == null) {
      return false;
    }
    try {
      File vFile = new File(quranDirectory +
          File.separator + ".v" + version);
      return vFile.exists();
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean haveAllImages(Context context, String widthParam) {
    String quranDirectory = getQuranDirectory(context, widthParam);
    if (quranDirectory == null) {
      return false;
    }

    String state = Environment.getExternalStorageState();
    if (state.equals(Environment.MEDIA_MOUNTED)) {
      File dir = new File(quranDirectory + File.separator);
      if (dir.isDirectory()) {
        String[] fileList = dir.list();
        if (fileList == null) {
          return false;
        }
        int files = fileList.length;
        //Log.i(TAG, " number of files dowmloaded "+files +" total min expected "+ Constants.PAGES_LAST)  ;
        if (files >= Constants.PAGES_LAST) {
          // ideally, we should loop for each page and ensure
          // all pages are there, but this will do for now.
          return true;
        }
      } else {
        QuranFileUtils.makeQuranDirectory(context);
      }
    }
    return false;
  }

  public static String getPageFileName(int p) {
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    nf.setMinimumIntegerDigits(3);
    return "page" + nf.format(p) + ".png";
  }

  public static boolean isSDCardMounted() {
    String state = Environment.getExternalStorageState();
    return state.equals(Environment.MEDIA_MOUNTED);
  }

  public static Response getImageFromSD(Context context, String filename) {
    return getImageFromSD(context, null, filename);
  }

  public static Response getImageFromSD(Context context, String widthParam,
                                        String filename) {
    String location;
    if (widthParam != null) {
      location = getQuranDirectory(context, widthParam);
    } else {
      location = getQuranDirectory(context);
    }

    if (location == null) {
      return new Response(Response.ERROR_SD_CARD_NOT_FOUND);
    }

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
    final Bitmap bitmap = BitmapFactory.decodeFile(location +
        File.separator + filename, options);
    return bitmap == null ? new Response(Response.ERROR_FILE_NOT_FOUND) :
        new Response(bitmap);
  }

  public static boolean writeNoMediaFile(Context context) {
    File f = new File(getQuranDirectory(context) + "/.nomedia");
    if (f.exists()) {
      return true;
    }

    try {
      return f.createNewFile();
    }
    catch (IOException e) {
      return false;
    }
  }

  public static boolean makeQuranDirectory(Context context) {
    String path = getQuranDirectory(context);
    if (path == null)
      return false;

    File directory = new File(path);
    if (directory.exists() && directory.isDirectory()) {
      return writeNoMediaFile(context);
    }
    else if (directory.mkdirs()) {
      return writeNoMediaFile(context);
    }
    else { return false; }
  }

  public static boolean makeQuranDatabaseDirectory(Context context) {
    String path = getQuranDatabaseDirectory(context);
    if (path == null)
      return false;

    File directory = new File(path);
    if (directory.exists() && directory.isDirectory()) {
      return true;
    }
    else if (directory.mkdirs()) { return true; }
    else { return false; }
  }

  public static Response getImageFromWeb(Context context, String filename) {
    return getImageFromWeb(context, filename, false);
  }

  private static Response getImageFromWeb(Context context,
      String filename, boolean isRetry) {
    QuranScreenInfo instance = QuranScreenInfo.getInstance();
    if (instance == null) {
      instance = QuranScreenInfo.getOrMakeInstance(context);
    }

    String urlString = IMG_HOST + "width"
        + instance.getWidthParam() + "/"
        + filename;

    //Log.i(TAG, "want to download: " + urlString);
    //System.out.println(" Img url is "+urlString);

    InputStream is = null;
    HttpURLConnection connection = null;
    try {
      // thanks to Picasso URLConnectionDownloader
      connection = (HttpURLConnection) (new URL(urlString).openConnection());
      connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
      connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
      if (connection.getResponseCode() >= 300 ||
          connection.getContentLength() == 0) {
        if (isRetry) {
          return new Response(Response.ERROR_DOWNLOADING_ERROR);
        } else {
          return getImageFromWeb(context, filename, true);
        }
      }

      is = connection.getInputStream();

      String path = getQuranDirectory(context);
      if (path != null) {
        path += File.separator + filename;

        // can't write to the sdcard, try to decode in memory
        if (!QuranFileUtils.makeQuranDirectory(context)) {
          final Bitmap bitmap = decodeBitmapStream(is);
          if (bitmap != null) {
            return new Response(bitmap, Response.WARN_SD_CARD_NOT_FOUND);
          }
        }

        try {
          final Bitmap bitmap = decodeBitmapStream(is);
          if (bitmap != null) {
            int warning = tryToSaveBitmap(bitmap, path) ? 0 :
                Response.WARN_COULD_NOT_SAVE_FILE;
            return new Response(bitmap, warning);
          } else if (isRetry) {
            return new Response(Response.ERROR_DOWNLOADING_ERROR);
          } else {
            return getImageFromWeb(context, filename, true);
          }
        } catch (Exception e) {
          return isRetry ? new Response(Response.ERROR_DOWNLOADING_ERROR) :
              getImageFromWeb(context, filename, true);
        }
      } else {
        final Bitmap bitmap = decodeBitmapStream(is);
        if (bitmap == null) {
          return new Response(Response.ERROR_DOWNLOADING_ERROR);
        } else {
          return new Response(bitmap, Response.WARN_SD_CARD_NOT_FOUND);
        }
      }
    } catch (Exception e) {
      if (isRetry) {
        return new Response(Response.ERROR_DOWNLOADING_ERROR);
      } else {
        return getImageFromWeb(context, filename, true);
      }
    } finally {
      if (is != null) {
        try { is.close(); }
        catch (Exception e){
          // no-op
        }
      }

      if (connection != null) {
        try {
          connection.disconnect();
        } catch (Exception e) {
          // no-op
        }
      }
    }
  }

  private static Bitmap decodeBitmapStream(InputStream is) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
    return BitmapFactory.decodeStream(is, null, options);
  }

  private static boolean tryToSaveBitmap(Bitmap bitmap, String savePath)
      throws IOException {
    FileOutputStream output = new FileOutputStream(savePath);
    try {
      return bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
    } finally {
      try {
        output.flush();
        output.close();
      } catch (Exception e) {
        // ignore...
      }
    }
  }

  public static String getQuranBaseDirectory(Context context) {
    String basePath = QuranSettings.getAppCustomLocation(context);

    if (!isSDCardMounted()) {
      if (basePath == null || basePath.equals(
          Environment.getExternalStorageDirectory().getAbsolutePath())){
        basePath = null;
      }
    }

    if (basePath != null) {
      if (!basePath.endsWith("/")){
        basePath += "/";
      }
      return basePath + QURAN_BASE;
    }

    return null;
  }

  /**
   * Returns the app used space in megabytes
   *
   * @return
   */
  public static int getAppUsedSpace(Context context) {
    File base = new File(getQuranBaseDirectory(context));
    ArrayList<File> files = new ArrayList<File>();
    files.add(base);
    long size = 0;
    while (!files.isEmpty()) {
      File f = files.remove(0);
      if (f.isDirectory()) {
        File[] subFiles = f.listFiles();
        if (subFiles != null){
          for (File sf : subFiles) files.add(sf);
        }
      }
      else {
        size += f.length();
      }
    }
    return (int) (size / (long) (1024 * 1024));
  }

  public static String getQuranDatabaseDirectory(Context context) {
        String base = getQuranBaseDirectory(context);
        return (base == null) ? null : base + DATABASE_DIRECTORY;
    }


  public static String getQuranDirectory(Context context) {
    QuranScreenInfo qsi = QuranScreenInfo.getInstance();
    if (qsi == null) {
      return null;
    }
    return getQuranDirectory(context, qsi.getWidthParam());
  }

  public static String getQuranDirectory(Context context, String widthParam) {
    String base = getQuranBaseDirectory(context);
    return (base == null) ? null : base + "width" + widthParam;
  }

  public static String getZipFileUrl() {
    QuranScreenInfo qsi = QuranScreenInfo.getInstance();
    if (qsi == null) {
      return null;
    }
    return getZipFileUrl(qsi.getWidthParam());
  }

  public static String getZipFileUrl(String widthParam) {
    String url = IMG_HOST;
    url += "images" + widthParam + ".zip";
    return url;
  }

  public static String getPatchFileUrl(String widthParam, int toVersion) {
    return IMG_HOST + "patch" +
        widthParam + "_v" + toVersion + ".zip";
  }

  public static String getAyaPositionFileName() {
    QuranScreenInfo qsi = QuranScreenInfo.getInstance();
    if (qsi == null) return null;
    return getAyaPositionFileName(qsi.getWidthParam());
  }

  public static String getAyaPositionFileName(String widthParam) {
    return "ayahinfo" + widthParam + ".db";
  }

  public static String getAyaPositionFileUrl() {
    QuranScreenInfo qsi = QuranScreenInfo.getInstance();
    if (qsi == null) {
      return null;
    }
    return getAyaPositionFileUrl(qsi.getWidthParam());
  }

  public static String getAyaPositionFileUrl(String widthParam) {
    String url = IMG_HOST + "width" + widthParam;
    url += "/ayahinfo" + widthParam + ".zip";
    return url;
  }

   public static String getGaplessDatabaseRootUrl() {
    QuranScreenInfo qsi = QuranScreenInfo.getInstance();
    if (qsi == null)
      return null;
    return IMG_HOST + "databases/audio/";
  }

  public static boolean haveAyaPositionFile(Context context) {
    String base = QuranFileUtils.getQuranDatabaseDirectory(context);
      Log.d(TAG, "inside haveAyaPositionFile");
      if (base == null)
          QuranFileUtils.makeQuranDatabaseDirectory(context);
      String filename = QuranFileUtils.getAyaPositionFileName();
      if (filename != null) {
          String ayaPositionDb = base + File.separator + filename;
          File f = new File(ayaPositionDb);
          if (!f.exists()) {
              return false;
          }
          else {
              return true;
          }
      }

    return false;
  }

  public static boolean hasTranslation(Context context, String fileName) {
    String path = getQuranDatabaseDirectory(context);
    if (path != null) {
      path += File.separator + fileName;
      return new File(path).exists();
    }
    return false;
  }

  public static boolean removeTranslation(Context context, String fileName) {
    String path = getQuranDatabaseDirectory(context);
    if (path != null) {
      path += File.separator + fileName;
      File f = new File(path);
      return f.delete();
    }
    return false;
  }

  public static boolean hasArabicSearchDatabase(Context context) {
    return hasTranslation(context, QuranDataProvider.QURAN_ARABIC_DATABASE);
  }

  public static boolean hasKanSearchDatabase(Context context) {
        return hasTranslation(context, QuranDataProvider.QURAN_KAN_DATABASE);
  }

  public static String getArabicSearchDatabaseUrl() {
    return IMG_HOST + DATABASE_DIRECTORY + "/" +
        QuranDataProvider.QURAN_ARABIC_DATABASE;
  }

  public static String getKanSearchDatabaseUrl() {
        return IMG_HOST + DATABASE_DIRECTORY + "/" +
                QuranDataProvider.QURAN_KAN_DATABASE;
  }

  public static void migrateAudio(Context context) {
    String oldAudioDirectory = AudioUtils.getOldAudioRootDirectory(context);
    String destinationAudioDirectory = AudioUtils.getAudioRootDirectory(context);
    if (oldAudioDirectory != null && destinationAudioDirectory != null) {
      File old = new File(oldAudioDirectory);
      if (old.exists()) {
        //Log.d(TAG, "old audio path exists");
        File dest = new File(destinationAudioDirectory);
        if (!dest.exists()) {
          // just in case the user manually deleted /sdcard/quran_android
          // and left the audio as is (unlikely, but just in case).
          String parentDir = QuranFileUtils.getQuranBaseDirectory(context);
          new File(parentDir).mkdir();

          //Log.d(TAG, "new audio path doesn't exist, renaming...");
          boolean result = old.renameTo(dest);
          //Log.d(TAG, "result of renaming: " + result);
        }
        else {
          //Log.d(TAG, "destination already exists..");
          File[] oldFiles = old.listFiles();
          if (oldFiles != null) {
            for (File f : oldFiles) {
              File newFile = new File(dest, f.getName());
              if (newFile != null) {
                boolean result = f.renameTo(newFile);
                Log.d(TAG, "attempting to copy " + f +
                    " to " + newFile + ", res: " + result);
              }
            }
          }
        }
      }

      try {
        // make the .nomedia file if it doesn't already exist
        File noMediaFile = new File(destinationAudioDirectory, ".nomedia");
        if (!noMediaFile.exists()) {
          noMediaFile.createNewFile();
        }
      }
      catch (IOException ioe) {
      }
    }
  }

  public static boolean moveAppFiles(Context context, String newLocation) {
    if (QuranSettings.getAppCustomLocation(context).equals(newLocation))
      return true;
    File currentDirectory = new File(getQuranBaseDirectory(context));
    File newDirectory = new File(newLocation, QURAN_BASE);
    if (!currentDirectory.exists()) {
      // No files to copy, so change the app directory directly
      return true;
    }
    else if (newDirectory.exists() || newDirectory.mkdirs()) {
      try {
        copyFileOrDirectory(currentDirectory, newDirectory);
        deleteFileOrDirectory(currentDirectory);
        return true;
      }
      catch (IOException e) {
        Log.e(TAG, "error moving app files", e);
      }
    }
    return false;
  }




    public static boolean moveDBFiles(Context context, String currentLocation, String newLocation) {
        if (QuranSettings.getAppCustomLocation(context).equals(newLocation))
            return true;
        File currentDirectory = new File(currentLocation);
        File newDirectory = new File(newLocation, QURAN_BASE);
        if (!currentDirectory.exists()) {
            // No files to copy, so change the app directory directly
            return true;
        }
        else if (newDirectory.exists() || newDirectory.mkdirs()) {
            try {
                copyFileOrDirectory(currentDirectory, newDirectory);
                deleteFileOrDirectory(currentDirectory);
                return true;
            }
            catch (IOException e) {
                Log.e(TAG, "error moving app files", e);
            }
        }
        return false;
    }

  private static void deleteFileOrDirectory(File file) {
    if (file.isDirectory()) {
      File[] subFiles = file.listFiles();
      for (File sf : subFiles) {
        if (sf.isFile())
          sf.delete();
        else
          deleteFileOrDirectory(sf);
      }
    }
    file.delete();
  }

  private static void copyFileOrDirectory(File source, File destination) throws IOException {
    if (source.isDirectory()) {
      if (!destination.exists()) {
        destination.mkdirs();
      }

      File[] files = source.listFiles();
      for (File f : files) {
        copyFileOrDirectory(f, new File(destination, f.getName()));
      }
    }
    else { copyFile(source, destination); }
  }

  public static void copyFile(File source, File destination) throws IOException {
    InputStream in = new FileInputStream(source);
    OutputStream out = new FileOutputStream(destination);

    byte[] buffer = new byte[1024];
    int length;
    while ((length = in.read(buffer)) > 0) {
      out.write(buffer, 0, length);
    }
    out.flush();
    out.close();
    in.close();
  }

  public static void copyAssetDB(Context context, String sourceFileName, String destFileName) throws IOException {
        InputStream is = context.getAssets().open(sourceFileName);

        String dbDir = QuranFileUtils.getQuranDatabaseDirectory(context);
        //Log.i(TAG, "getQuranDatabaseDirectory "+dbDir);
        boolean dbDirExists=false;
        dbDirExists= QuranFileUtils.makeQuranDatabaseDirectory(context);
        //Log.i(TAG, "dbDirExists  "+dbDirExists);
        String dbFile = dbDir + File.separator + destFileName;
        File f = new File(dbFile);
        if (!f.exists()) {
            //Log.i(TAG, "file doesn't exist "+f.getAbsolutePath());
            f.createNewFile();
            OutputStream os = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            while (is.read(buffer) > 0) {
                os.write(buffer);
            }

            os.flush();
            os.close();
            is.close();
        }
        else {
            //Log.i(TAG, "file already exists, so not copying the Asset DB "+f.getAbsolutePath());
        }
    }


    public static void copyAssetDBLatestVersion(Context context, String sourceFileName, String destFileName) throws IOException {
        InputStream is = context.getAssets().open(sourceFileName);

        String dbDir = QuranFileUtils.getQuranBaseDirectory(context);
        //Log.i(TAG, "getQuranBaseDirectory method, for copying from assets to "+dbDir);

        String dbFile = dbDir + File.separator + destFileName;
        File f = new File(dbFile);
        if (!f.exists()) {
            //Log.i(TAG, "file doesn't exist "+f.getAbsolutePath());
            f.createNewFile();
            OutputStream os = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            while (is.read(buffer) > 0) {
                os.write(buffer);
            }

            os.flush();
            os.close();
            is.close();
        }
        else {
            //Log.i(TAG, "file already exists, so not copying the Asset DB latest version"+f.getAbsolutePath());
        }
    }
}
