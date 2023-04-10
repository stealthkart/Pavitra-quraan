package com.scalosphere.labs.kquran;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
//import android.os.AsyncTask;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.data.QuranDataProvider;
import com.scalosphere.labs.kquran.database.DatabaseHandler;
import com.scalosphere.labs.kquran.service.QuranDownloadService;
import com.scalosphere.labs.kquran.service.util.DefaultDownloadReceiver;
import com.scalosphere.labs.kquran.service.util.ServiceIntentHelper;
import com.scalosphere.labs.kquran.task.DBUpdateCheckTask;
import com.scalosphere.labs.kquran.ui.QuranActivity;
import com.scalosphere.labs.kquran.util.QuranFileUtils;
import com.scalosphere.labs.kquran.util.QuranScreenInfo;
import com.scalosphere.labs.kquran.widgets.QuranMaxImageView;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class QuranDataActivity extends Activity implements
        DefaultDownloadReceiver.SimpleDownloadListener {

   public static final String TAG =
           "com.scalosphere.lab";
   public static final String PAGES_DOWNLOAD_KEY = "PAGES_DOWNLOAD_KEY";
   private static final int MSG_REFRESH_MAX_HEIGHT = 1;
//   public static final String KQ_DB_VERSION_CHECK_URL =  "http://scalosphere.in/SalahTime/kqdversion?kq-version=";//2

   private boolean mIsPaused = false;
    CheckPagesAsyncTask mCheckPagesTask;
//   private AsyncTask<Void, Void, Boolean> mCheckPagesTask;
   private AlertDialog mErrorDialog = null;
   private AlertDialog mPromptForDownloadDialog = null;
   private SharedPreferences mSharedPreferences = null;
   private DefaultDownloadReceiver mDownloadReceiver = null;
   private boolean mNeedPortraitImages = false;
   private boolean mNeedLandscapeImages = false;
   private String mPatchUrl;
   private int mRefreshHeightTries;
   private QuranMaxImageView mSplashView;
   private DBUpdateCheckTask updateKDBTask = null;

    @RequiresApi(api = Build.VERSION_CODES.R)
    private boolean checkPermission() {
        int writePerm =   ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPerm = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE);
        int internetPerm = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.INTERNET);
        int accessNetworkPerm = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_NETWORK_STATE);
        int readPhonePerm = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_PHONE_STATE);
        int wakeLockPerm = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WAKE_LOCK);

        return writePerm== PackageManager.PERMISSION_GRANTED && readPerm==PackageManager.PERMISSION_GRANTED && internetPerm==PackageManager.PERMISSION_GRANTED && accessNetworkPerm==PackageManager.PERMISSION_GRANTED && readPhonePerm==PackageManager.PERMISSION_GRANTED && wakeLockPerm==PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==200){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1]==PackageManager.PERMISSION_GRANTED
                    && grantResults[2]==PackageManager.PERMISSION_GRANTED
                    && grantResults[3]==PackageManager.PERMISSION_GRANTED
                    && grantResults[4]==PackageManager.PERMISSION_GRANTED
                    && grantResults[5]==PackageManager.PERMISSION_GRANTED

                     ){
                // do Operation
                }
            else{
                Toast.makeText(getApplicationContext(),"Permission Denied grantResults[0]"+grantResults[0]+" grantResults[1]"+grantResults[1],Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
   public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"baseDir is ---------------------------------------------start>");
      setTheme(R.style.QuranAndroid);

      super.onCreate(savedInstanceState);
      setContentView(R.layout.splash_screen);

          mCheckPagesTask= new CheckPagesAsyncTask(this);

      mSplashView = (QuranMaxImageView)findViewById(R.id.splashview);
       setSplashViewHardwareAcceleratedICS();

       if (mSplashView != null){
         try {
            mSplashView.setImageResource(R.drawable.splash);
         }
         catch (OutOfMemoryError error){
            mSplashView.setBackgroundColor(Color.BLACK);
         }
      }


       if(checkPermission()){
           // do operation
       }
       else {
//           // request Permission in Activity
//           requestPermissions(getApplicationContext(),
//                   new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                           Manifest.permission.READ_EXTERNAL_STORAGE},200);
//           // request Permission in Fragmemt
//           requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                   Manifest.permission.READ_EXTERNAL_STORAGE}, 200);

           requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                   Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WAKE_LOCK}, 200);


       }

//        if (Environment.isExternalStorageManager()) {
//           System.out.println("Suspect one");
//        } else {
//            //request for the permission
//            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            Uri uri = Uri.fromParts("package", getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
//        }

      /*
        // remove files for debugging purposes
        QuranUtils.debugRmDir(QuranUtils.getQuranBaseDirectory(), false);
        QuranUtils.debugLsDir(QuranUtils.getQuranBaseDirectory());
       */

      initializeQuranScreen();
      mSharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());
        Log.i(TAG,"baseDir is --------------------------------------------->");
      // one time upgrade to v2.4.3
      if (!mSharedPreferences.contains(Constants.PREF_UPGRADE_TO_243)){
         String baseDir = QuranFileUtils.getQuranBaseDirectory(this);

         Log.i(TAG,"baseDir is --------------------------------------------->"+baseDir);

         if (baseDir != null){

            baseDir = baseDir + File.separator;
            try {
               File f = new File(baseDir);
               if (f.exists() && f.isDirectory()){
                  String[] files = f.list();
                  if (files != null){
                     for (String file : files){
                        if (file.endsWith(".part")){
                           try {
                              new File(baseDir + file).delete();
                           }
                           catch (Exception e){}
                        }
                     }
                  }
               }
            }
            catch (Exception e){
            }
         }

         // update night mode preference and mark that we upgraded to 2.4.2ts
         mSharedPreferences.edit()
                 .putInt(Constants.PREF_NIGHT_MODE_TEXT_BRIGHTNESS,
                         Constants.DEFAULT_NIGHT_MODE_TEXT_BRIGHTNESS)
                 .remove(Constants.PREF_UPGRADE_TO_242)
                 .putBoolean(Constants.PREF_UPGRADE_TO_243, true).apply();
      }
   }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void setSplashViewHardwareAcceleratedICS() {
    // actually requires 11+, but the other call we need
    // for getting max bitmap height requires 14+
    mSplashView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
  }
   
   @Override
   protected void onResume(){
      super.onResume();

      mIsPaused = false;
      mDownloadReceiver = new DefaultDownloadReceiver(this,
              QuranDownloadService.DOWNLOAD_TYPE_PAGES);
      mDownloadReceiver.setCanCancelDownload(true);
      String action = QuranDownloadService.ProgressIntent.INTENT_NAME;
      LocalBroadcastManager.getInstance(this).registerReceiver(
            mDownloadReceiver,
            new IntentFilter(action));
      mDownloadReceiver.setListener(this);

      if (mSharedPreferences.getInt(
          Constants.PREF_MAX_BITMAP_HEIGHT, -1) == -1){
          int height = mSplashView.getMaxBitmapHeight();
          if (height == -1){
            Log.d(TAG, "retrying to get max height in 500...");
            mHandler.sendEmptyMessageDelayed(MSG_REFRESH_MAX_HEIGHT, 500);
            return;
          }

          Log.d(TAG, "got max height height of " + height);
          mSharedPreferences.edit().putInt(
              Constants.PREF_MAX_BITMAP_HEIGHT, height).apply();
          QuranScreenInfo.getInstance().setBitmapMaxHeight(height);
      }

       mCheckPagesTask.startAsyncTask("checkPages");

//      // check whether or not we need to download
//      mCheckPagesTask = new CheckPagesAsyncTask(this);
//      mCheckPagesTask.execute();
   }
   
   @Override
   protected void onPause() {
      // one more attempt to get the max height if we
      // haven't gotten it already...
      if (mSharedPreferences.getInt(
          Constants.PREF_MAX_BITMAP_HEIGHT, -1) == -1){
          int height = mSplashView.getMaxBitmapHeight();
          if (height > 0){
            Log.d(TAG, "got max height height of " + height);
            mSharedPreferences.edit().putInt(
              Constants.PREF_MAX_BITMAP_HEIGHT, height).apply();
            QuranScreenInfo.getInstance().setBitmapMaxHeight(height);
          }
      }

      mIsPaused = true;
      if (mDownloadReceiver != null) {
        mDownloadReceiver.setListener(null);
        LocalBroadcastManager.getInstance(this).
            unregisterReceiver(mDownloadReceiver);
        mDownloadReceiver = null;
      }

      if (mPromptForDownloadDialog != null){
         mPromptForDownloadDialog.dismiss();
         mPromptForDownloadDialog = null;
      }
      
      if (mErrorDialog != null){
         mErrorDialog.dismiss();
         mErrorDialog = null;
      }
      
      super.onPause();
   }

   private Handler mHandler = new Handler(Looper.getMainLooper()){
     @Override
     public void handleMessage(Message msg) {
       if (msg.what == MSG_REFRESH_MAX_HEIGHT){
         if (mSplashView == null || isFinishing() || mIsPaused){
           return;
         }

         int height = mSplashView.getMaxBitmapHeight();
         if (height > -1){
           android.util.Log.d(TAG, "in handler, got max height: " + height);
           mSharedPreferences.edit().putInt(
               Constants.PREF_MAX_BITMAP_HEIGHT, height).apply();
           QuranScreenInfo.getInstance().setBitmapMaxHeight(height);
           // check whether or not we need to download
           if (!mIsPaused) {
               mCheckPagesTask.startAsyncTask("checkPages");
//             mCheckPagesTask = new CheckPagesAsyncTask(QuranDataActivity.this);
//             mCheckPagesTask.execute();
           }
           return;
         }

         mRefreshHeightTries++;
         if (mRefreshHeightTries == 5){
           android.util.Log.d(TAG, "giving up on getting the max height...");
           if (!mIsPaused) {
               mCheckPagesTask.startAsyncTask("checkPages");
//             mCheckPagesTask = new CheckPagesAsyncTask(QuranDataActivity.this);
//             mCheckPagesTask.execute();
           }
         }
         else {
           android.util.Log.d(TAG, "trying to get the max height in a sec...");
           mHandler.sendEmptyMessageDelayed(MSG_REFRESH_MAX_HEIGHT, 1000);
         }
       }
     }
   };

   @Override
   public void handleDownloadSuccess(){
      mSharedPreferences.edit()
         .remove(Constants.PREF_SHOULD_FETCH_PAGES).apply();
       String active = mSharedPreferences.getString(Constants.PREF_ACTIVE_TRANSLATION, null);
       //Log.i(TAG, "active translation is set? "+active);
       if (TextUtils.isEmpty(active)) {
           boolean hasKanDB= QuranFileUtils.hasKanSearchDatabase(getApplicationContext());
           mSharedPreferences.edit().putString(Constants.PREF_ACTIVE_TRANSLATION, QuranDataProvider.QURAN_KAN_DATABASE).apply();
       }

      //TODO commented original code of translation list view
     runListView();
   }

   @Override
   public void handleDownloadFailure(int errId){
      if (mErrorDialog != null && mErrorDialog.isShowing()){
         return;
      }
     showFatalErrorDialog(errId);
   }
   
   private void showFatalErrorDialog(int errorId){
      AlertDialog.Builder builder =  new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.CustomTheme_Dialog));

      builder.setMessage(errorId);
      builder.setCancelable(false);
      builder.setPositiveButton(R.string.download_retry,
              (dialog, id) -> {
                 dialog.dismiss();
                 mErrorDialog = null;
                 removeErrorPreferences();
                 downloadQuranImages(true);
              });
      
      builder.setNegativeButton(R.string.download_cancel,
              (dialog, which) -> {
                 dialog.dismiss();
                 mErrorDialog = null;
                 removeErrorPreferences();
                 mSharedPreferences.edit().putBoolean(
                         Constants.PREF_SHOULD_FETCH_PAGES, false)
                         .apply();
                  //TODO commented original code of translation list view
                  //runListView();
              });
      
      mErrorDialog = builder.create();
      mErrorDialog.show();
   }

   private void removeErrorPreferences(){
      mSharedPreferences.edit()
      .remove(QuranDownloadService.PREF_LAST_DOWNLOAD_ERROR)
      .remove(QuranDownloadService.PREF_LAST_DOWNLOAD_ITEM)
      .apply();
   }
   
   class CheckPagesAsyncTask  {
      private final Context mAppContext;
      public CheckPagesAsyncTask(Context context) {
        mAppContext = context.getApplicationContext();
      }

       //-------------------Main logic here-------------------//
       private void startAsyncTask(String input) {
           Observable.just(input)
                   .map(this::doInBackground)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
//                   .doOnSubscribe(this::onPreExecute)
                   .subscribe(this::onPostExecute);
       }



       //do background things here
       private boolean doInBackground(String data) {
           // intentionally not sleeping because waiting
           // for the splash screen is not cool.
           QuranFileUtils.migrateAudio(mAppContext);

           Log.i("Suspect","A1");

           try {

               if (!QuranFileUtils.hasArabicSearchDatabase(mAppContext)){
                   Log.i("Suspect","B1");
                   //Log.i(TAG, " Copying Arabic DB from assets in "+ System.currentTimeMillis() + " seconds");
                   QuranFileUtils.copyAssetDB(mAppContext,QuranFileUtils.Arabic_Asset_DB,QuranDataProvider.QURAN_ARABIC_DATABASE);
               }else{
                   Log.i("Suspect","C1");
                   // Update Arabic db from the assets db within the APK when the kquraan/db text version is older than the APK Db text version
                   assetsUpdateArabicDB();
               }

               Log.i("Suspect","D1");

               if (!QuranFileUtils.hasKanSearchDatabase(mAppContext)) {
                   Log.i(TAG, " Copying Kannada DB from assets in "+ System.currentTimeMillis() + " seconds");
                   QuranFileUtils.copyAssetDB(mAppContext,QuranFileUtils.Kannada_Asset_DB,QuranDataProvider.QURAN_KAN_DATABASE);
               }else{
                   Log.i(TAG, " Update kannada db from the assets db within the APK when the kquraan/db text version is older than the APK Db text version");
                   // Update kannada db from the assets db within the APK when the kquraan/db text version is older than the APK Db text version
                   assetsUpdateKannadaDB();
               }

           }catch (Exception e){
               Log.e(TAG, " Error in copying DB from Assets to kQuraan/db when the db files do not exist "+e);

               e.printStackTrace();
           }

           //TODO  for the first release set haveLandscape as true
           // TODO and comment this code -- boolean haveLandscape = QuranFileUtils.haveAllImages(mAppContext,QuranScreenInfo.getInstance().getTabletWidthParam());
           if (QuranScreenInfo.getInstance().isTablet(mAppContext)){
               //new code for KQur'aan
               boolean haveLandscape =true;
               // original code commented below
               //boolean haveLandscape = QuranFileUtils.haveAllImages(mAppContext,
               //        QuranScreenInfo.getInstance().getTabletWidthParam());
               boolean havePortrait = QuranFileUtils.haveAllImages(mAppContext,
                       QuranScreenInfo.getInstance().getWidthParam());
               mNeedPortraitImages = !havePortrait;
               mNeedLandscapeImages = !haveLandscape;
               return haveLandscape && havePortrait;
           }
           else {
               boolean haveAll = QuranFileUtils.haveAllImages(mAppContext,
                       QuranScreenInfo.getInstance().getWidthParam());
               mNeedPortraitImages = !haveAll;
               mNeedLandscapeImages = false;
               return haveAll;
           }
       }

       //post execution work here
       private void onPostExecute(boolean result) {

           mPatchUrl = null;

           if (mIsPaused){ return; }
           boolean test= mSharedPreferences.getBoolean(Constants.PREF_SHOULD_FETCH_PAGES, false);

           if (!result){
               String lastErrorItem = mSharedPreferences.getString(
                       QuranDownloadService.PREF_LAST_DOWNLOAD_ITEM, "");
               if (PAGES_DOWNLOAD_KEY.equals(lastErrorItem)){
                   int lastError = mSharedPreferences.getInt(
                           QuranDownloadService.PREF_LAST_DOWNLOAD_ERROR, 0);
                   int errorId = ServiceIntentHelper
                           .getErrorResourceFromErrorCode(lastError, false);
                   showFatalErrorDialog(errorId);
               }
               else if (mSharedPreferences.getBoolean(
                       Constants.PREF_SHOULD_FETCH_PAGES, false)){
                   downloadQuranImages(false);
               }
               else {
                   promptForDownload();
               }
           }
           else {
               // force a check for the images version 3, if it's not
               // there, download the patch.
               QuranScreenInfo qsi = QuranScreenInfo.getInstance();
               String widthParam = qsi.getWidthParam();
               if (qsi.isTablet(QuranDataActivity.this)){
                   String tabletWidth = qsi.getTabletWidthParam();
                   if ((!QuranFileUtils.isVersion(QuranDataActivity.this,
                           widthParam, 3)) ||
                           (!QuranFileUtils.isVersion(QuranDataActivity.this,
                                   tabletWidth, 3))){
                       widthParam += tabletWidth;
                       // get patch for both landscape/portrait tablet images
                       mPatchUrl = QuranFileUtils.getPatchFileUrl(widthParam, 3);
                       Log.d(TAG, "Downloading patch file from "+mPatchUrl);
                       promptForDownload();
                       return;
                   }
               }
               else if (!QuranFileUtils.isVersion(QuranDataActivity.this,
                       widthParam, 3)){
                   // explicitly check whether we need to fix the images
                   mPatchUrl = QuranFileUtils.getPatchFileUrl(widthParam, 3);
                   Log.d(TAG, "Downloading patch file from "+mPatchUrl);
                   promptForDownload();
                   return;
               }

               long time = mSharedPreferences.getLong(
                       Constants.PREF_LAST_UPDATED_TRANSLATIONS, 0);
               Date now = new Date();
               Log.d(TAG, "checking whether we should update translations..");
               if (now.getTime() - time > Constants.TRANSLATION_REFRESH_TIME){
                   //Log.i(TAG, "updating translations list...");
                   //TODO handle Kannada DB upgrade
                   Intent intent = new Intent(QuranDataActivity.this,
                           QuranDownloadService.class);
                   intent.setAction(
                           QuranDownloadService.ACTION_CHECK_TRANSLATIONS);
                   startService(intent);
               }
               //TODO commented original code of translation list view -- temporarily uncommented
               runListView();
           }

       }

//      @Override
//          protected void onPostExecute(Boolean result) {
//
//
//      }
   }

   /**
    * this method asks the service to download quran images.
    * 
    * there are two possible cases - the first is one in which we are not
    * sure if a download is going on or not (ie we just came in the app,
    * the files aren't all there, so we want to start downloading).  in
    * this case, we start the download only if we didn't receive any
    * broadcasts before starting it.
    * 
    * in the second case, we know what we are doing (either because the user
    * just clicked "download" for the first time or the user asked to retry
    * after an error), then we pass the force parameter, which asks the
    * service to just restart the download irrespective of anything else.
    * 
    * @param force whether to force the download to restart or not
    */
   private void downloadQuranImages(boolean force){
      // if any broadcasts were received, then we are already downloading
      // so unless we know what we are doing (via force), don't ask the
      // service to restart the download
      if (mDownloadReceiver != null &&
              mDownloadReceiver.didReceieveBroadcast() && !force){ return; }
      if (mIsPaused){ return; }

      QuranScreenInfo qsi = QuranScreenInfo.getInstance();
      
      String url;
      if (mNeedPortraitImages && !mNeedLandscapeImages){
         // phone (and tablet when upgrading on some devices, ex n10)
         //Log.i( TAG, "Before downloading the  Full zip file for Phone - Portrait images");
         url = QuranFileUtils.getZipFileUrl();
      }
      else if (mNeedLandscapeImages && !mNeedPortraitImages){
         // tablet (when upgrading from pre-tablet on some devices, ex n7).
         //Log.i( TAG, "Before downloading the  Full zip file for Tablet - Landscape Images");
         url = QuranFileUtils.getZipFileUrl(qsi.getTabletWidthParam());
      }
      else {
         // new tablet installation - if both image sets are the same
         // size, then just get the correct one only
         if (qsi.getTabletWidthParam().equals(qsi.getWidthParam())){
            //Log.i( TAG, "Before downloading the  Full zip file for Tablet when phone width & tablet width are same");
            url = QuranFileUtils.getZipFileUrl();
         }
         else {
            // otherwise download one zip with both image sets
            String widthParam = qsi.getWidthParam() +
                    qsi.getTabletWidthParam();
            url = QuranFileUtils.getZipFileUrl(widthParam);
         }
      }

      // if we have a patch url, just use that
      if (!TextUtils.isEmpty(mPatchUrl)){
         url = mPatchUrl;
      }

       String destination = QuranFileUtils.getQuranBaseDirectory(QuranDataActivity.this);
      
      // start service
      Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
              destination, getString(R.string.app_name), PAGES_DOWNLOAD_KEY,
              QuranDownloadService.DOWNLOAD_TYPE_PAGES);
      
      if (!force){
         // handle race condition in which we missed the error preference and
         // the broadcast - if so, just rebroadcast errors so we handle them
         intent.putExtra(QuranDownloadService.EXTRA_REPEAT_LAST_ERROR, true);
      }

      startService(intent);
   }

   private void promptForDownload(){
      int message = R.string.downloadPrompt;
      if (QuranScreenInfo.getInstance().isTablet(this) &&
              (mNeedPortraitImages != mNeedLandscapeImages)){
         message = R.string.downloadTabletPrompt;
      }

      if (!TextUtils.isEmpty(mPatchUrl)){
         // patch message if applicable
         message = R.string.downloadImportantPrompt;
      }

       AlertDialog.Builder dialog =  new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.CustomTheme_Dialog));
      dialog.setMessage(message);
      dialog.setCancelable(false);

      dialog.setPositiveButton(R.string.downloadPrompt_ok,
              (dialog1, id) -> {
                 dialog1.dismiss();
                 mPromptForDownloadDialog = null;
                 mSharedPreferences.edit().putBoolean(
                         Constants.PREF_SHOULD_FETCH_PAGES, true)
                         .apply();
                 downloadQuranImages(true);
              });

      dialog.setNegativeButton(R.string.downloadPrompt_no,
              (dialog12, id) -> {
                 dialog12.dismiss();
                 mPromptForDownloadDialog = null;
                  //TODO commented original code of translation list view
                  runListView();
               });

      mPromptForDownloadDialog = dialog.create();
      mPromptForDownloadDialog.setTitle(R.string.downloadPrompt_title);
      mPromptForDownloadDialog.show();
   }

    public void getKanArabicDB(){
        if (!QuranFileUtils.hasArabicSearchDatabase(this)) {
            String url = QuranFileUtils.getArabicSearchDatabaseUrl();

            String notificationTitle = getString(R.string.download_arabic_db);
            Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                    QuranFileUtils.getQuranDatabaseDirectory(this), notificationTitle,
                    QuranDownloadService.DB_DOWNLOAD_KEY, QuranDownloadService.DOWNLOAD_TYPE_ARABIC_SEARCH_DB);
            intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME,
                    QuranDataProvider.QURAN_ARABIC_DATABASE);
            startService(intent);
        }
        if (!QuranFileUtils.hasKanSearchDatabase(this)) {
            String url = QuranFileUtils.getKanSearchDatabaseUrl();

            String notificationTitle = getString(R.string.download_kan_db);
            Intent intent = ServiceIntentHelper.getDownloadIntent(this, url,
                    QuranFileUtils.getQuranDatabaseDirectory(this), notificationTitle,
                    QuranDownloadService.DB_DOWNLOAD_KEY, QuranDownloadService.DOWNLOAD_TYPE_KAN_SEARCH_DB);
            intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME,
                    QuranDataProvider.QURAN_KAN_DATABASE);
            startService(intent);
        }

    }

    public void assetsUpdateKannadaDB (){
       Log.i(TAG, " Assets Update Kannada Db ");
        //if(!mSharedPreferences.getBoolean(Constants.PREF_HAS_KAN_DB_UPDATED_FROM_ASSETS, false)){
            // Initialize Dababase Handlers
            DatabaseHandler assetsdbHandler;
            DatabaseHandler localdbHandler;

            // 1. Read the  TextVersion of the Kan Db from Kquraan/db
            localdbHandler = new DatabaseHandler(this, QuranDataProvider.QURAN_KAN_DATABASE);
            int localDbTextVersion = localdbHandler.getTextVersion();
           Log.i(TAG, "1. local(kquraan/db) TextVersion  is "+ localDbTextVersion);
            localdbHandler.closeDatabase();


            // 2. Copy Assets Kan Db to KQuraan dir
            int assetsDbTextVersion = 0;
            try {
                long startTime =System.currentTimeMillis();
                QuranFileUtils.copyAssetDBLatestVersion(this,QuranFileUtils.Kannada_Asset_DB,QuranDataProvider.QURAN_KAN_DATABASE);
               Log.i(TAG, "2. Copied Kannada DB from " + QuranFileUtils.Kannada_Asset_DB +" to KQuraan in "+ (System.currentTimeMillis()-startTime) +"Milli seconds");

                try {
                   // Log.i(TAG, " Sleep for 1 Secs ");
                    Thread.sleep(5000);

                    // 3. Read the TextVersion of the Kan Db from Kquraan dir
                    assetsdbHandler = new DatabaseHandler(this, QuranDataProvider.QURAN_KAN_DATABASE, true);
                    assetsDbTextVersion = assetsdbHandler.getTextVersion();
                    Log.i(TAG, "3. Compare assetsDbTextVersion "+ assetsDbTextVersion);
                    assetsdbHandler.closeDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }catch (Exception e){
               // Log.e(TAG, " Error in reading the text version  "+e);
                e.printStackTrace();
            }

            // 4. Compare localDbTextVersion and assetsDbTextVersion
           Log.i(TAG, "4. Compare localDbTextVersion " + localDbTextVersion + " < assetsDbTextVersion " + assetsDbTextVersion);
        String kQuraanDir1 = QuranFileUtils.getQuranBaseDirectory(this);
        String kQuraanDBDir2 = QuranFileUtils.getQuranDatabaseDirectory(this);
            Log.i(TAG,"kQuraanDir+QuranDataProvider.QURAN_KAN_DATABASE-->"+kQuraanDir1+QuranDataProvider.QURAN_KAN_DATABASE+" kQuraanDBDir+File.separator+QuranDataProvider.QURAN_KAN_DATABASE --> "+kQuraanDBDir2+File.separator+QuranDataProvider.QURAN_KAN_DATABASE);

            if(localDbTextVersion < assetsDbTextVersion){
                Log.i(TAG, "5. True -> Replacing db with Assets Txt Version "+ assetsDbTextVersion);
                String kQuraanDir = QuranFileUtils.getQuranBaseDirectory(this);
                String kQuraanDBDir = QuranFileUtils.getQuranDatabaseDirectory(this);
                File sourceDB = new File(kQuraanDir+QuranDataProvider.QURAN_KAN_DATABASE);
                File destDB = new File(kQuraanDBDir+File.separator+QuranDataProvider.QURAN_KAN_DATABASE);
                // Deleting Kannada DB from kquran/db folder
                deleteDb(destDB);

                //7. Copy the latest db file into kquraan/db
                if (!QuranFileUtils.hasKanSearchDatabase(this)){
                    try {

//                        long startTime =System.currentTimeMillis();
                        QuranFileUtils.copyFile(sourceDB,destDB);
                        //Log.i(TAG, " 6. Copied latest Kannada DB from Kquraan dir into kquraan/db " +(System.currentTimeMillis()-startTime) + "Milli seconds");
                        mSharedPreferences.edit().putBoolean(Constants.PREF_HAS_KAN_DB_UPDATED_FROM_ASSETS, true).apply();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Log.i(TAG, " 7. Setting the Preferences to " + mSharedPreferences.getBoolean(Constants.PREF_HAS_KAN_DB_UPDATED_FROM_ASSETS,false));
            }
        //}
    }

    public void assetsUpdateArabicDB (){
       // Log.i(TAG, " Assets Update Arabic Db ");
        if(!mSharedPreferences.getBoolean(Constants.PREF_HAS_Arabic_DB_UPDATED_FROM_ASSETS, false)){
            // Initialize Dababase Handlers
            DatabaseHandler assetsdbHandler;
            DatabaseHandler localdbHandler;

            // 1. Read the  TextVersion of the Arabic Db from Kquraan/db
            localdbHandler = new DatabaseHandler(this, QuranDataProvider.QURAN_ARABIC_DATABASE);
            int localDbTextVersion = localdbHandler.getTextVersion();
           // Log.i(TAG, "1. local(kquraan/db) TextVersion  is "+ localDbTextVersion);
            localdbHandler.closeDatabase();


            // 2. Copy Assets Arabic Db to KQuraan dir
            int assetsDbTextVersion = 0;
            try {
//                long startTime =System.currentTimeMillis();
                QuranFileUtils.copyAssetDBLatestVersion(this,QuranFileUtils.Arabic_Asset_DB,QuranDataProvider.QURAN_ARABIC_DATABASE);
               // Log.i(TAG, "2. Copied Arabic DB from " + QuranFileUtils.Arabic_Asset_DB +" to KQuraan in "+ (System.currentTimeMillis()-startTime) +"Milli seconds");

                try {
                   // Log.i(TAG, " Sleep for 1 Secs ");
                    Thread.sleep(1000);

                    // 3. Read the TextVersion of the Arabic Db from Kquraan dir
                    assetsdbHandler = new DatabaseHandler(this, QuranDataProvider.QURAN_ARABIC_DATABASE, true);
                    assetsDbTextVersion = assetsdbHandler.getTextVersion();
                  //  Log.i(TAG, "3. Assets(kquraan) TextVersion  is "+ assetsDbTextVersion);
                    assetsdbHandler.closeDatabase();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }catch (Exception e){
                Log.e(TAG, " Error in reading the text version  "+e);
                e.printStackTrace();
            }

            // 4. Compare localDbTextVersion and assetsDbTextVersion
           // Log.i(TAG, "4. Compare localDbTextVersion " + localDbTextVersion + " < assetsDbTextVersion " + assetsDbTextVersion);
            if(localDbTextVersion < assetsDbTextVersion){
                //Log.i(TAG, "5. True -> Replacing db with Assets Txt Version "+ assetsDbTextVersion);
                String kQuraanDir = QuranFileUtils.getQuranBaseDirectory(this);
                String kQuraanDBDir = QuranFileUtils.getQuranDatabaseDirectory(this);
                File sourceDB = new File(kQuraanDir+QuranDataProvider.QURAN_ARABIC_DATABASE);
                File destDB = new File(kQuraanDBDir+File.separator+QuranDataProvider.QURAN_ARABIC_DATABASE);
                // Deleting Kannada DB from kquran/db folder
                deleteDb(destDB);

                //7. Copy the latest db file into kquraan/db
                if (!QuranFileUtils.hasArabicSearchDatabase(this)){
                    try {
//                        long startTime =System.currentTimeMillis();
                        QuranFileUtils.copyFile(sourceDB,destDB);
                       // Log.i(TAG, " 6. Copied latest Arabic DB from Kquraan dir into kquraan/db " +(System.currentTimeMillis()-startTime) + "Milli seconds");
                        mSharedPreferences.edit().putBoolean(Constants.PREF_HAS_Arabic_DB_UPDATED_FROM_ASSETS, true).apply();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Log.i(TAG, " 7. Setting the Preferences to " + mSharedPreferences.getBoolean(Constants.PREF_HAS_Arabic_DB_UPDATED_FROM_ASSETS,false));
            }
        }
    }

    private void deleteDb(File db){
        try {
            if (db.exists()) {
                Log.i(TAG, " 5.c Deleting db from " + db.getAbsolutePath());
                db.delete();

            }else{
                Log.e(TAG," File " +db.getAbsolutePath() + "doesn't exist");
            }
        } catch (Exception e){
            Log.e(TAG," error in deleting the file "+e);
        }
    }

    public void updateDBFromServer(){
        //Log.i(TAG, " Inside updateDBFromServer method");
        if (QuranFileUtils.hasKanSearchDatabase(this)) {
           // Log.i(TAG, " Checking kannada text version with the server version");
            try{
                updateKDBTask = new DBUpdateCheckTask(this, true);
                updateKDBTask.execute();
            }
            catch (Exception e){
                Log.e(TAG, "error in update DB From server", e);
            }
        }
        if (QuranFileUtils.hasArabicSearchDatabase(this)) {
            try{
                //Log.i(TAG, " Checking arabic text version with the server version");
                updateKDBTask = new DBUpdateCheckTask(this, false);
                updateKDBTask.execute();
            }
            catch (Exception e){
                Log.e(TAG, "error in update DB From server", e);
            }
        }
    }

    protected void initializeQuranScreen() {
      QuranScreenInfo.getOrMakeInstance(this);
   }

   protected void runListView(boolean showTranslations){
      Intent i = new Intent(this, QuranActivity.class);
      if (showTranslations){
         i.putExtra(QuranActivity.EXTRA_SHOW_TRANSLATION_UPGRADE, true);
      }
      startActivity(i);

      finish();
   }

   protected void runListView(){
      //try to download Arabic & Kannada Db's from the server in the background only if the DB are not existing under kquraan/db folder
      getKanArabicDB();
      // Update kannada db using the server call after verifying the text version of the local DB.
       updateDBFromServer();

      boolean value = (mSharedPreferences.getBoolean(
              Constants.PREF_HAVE_UPDATED_TRANSLATIONS, false));

      runListView(value);
   }
}
