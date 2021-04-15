package com.scalosphere.labs.kquran.task;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.data.QuranDataProvider;
import com.scalosphere.labs.kquran.database.DatabaseHandler;
import com.scalosphere.labs.kquran.service.QuranDownloadService;
import com.scalosphere.labs.kquran.service.util.ServiceIntentHelper;
import com.scalosphere.labs.kquran.util.QuranFileUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by SHUJATH on 04-12-2014.
 */
public class DBUpdateCheckTask extends AsyncTask <String, String , String>  {

    public static final String KQ_KanDB_VERSION_CHECK_URL =  "http://scalosphere.in/SalahTime/kqdversion?kq-version=";//2
    public static final String KQ_ArabicDB_VERSION_CHECK_URL =  "http://scalosphere.in/SalahTime/kqdversion?aq-version=";//2
    private Context mContext;
    public static final String TAG = "DBUpdateCkeckTask";
    public boolean kannadaFlag = false;

    public DBUpdateCheckTask(Context context, boolean kanFlag){
        mContext = context;
        kannadaFlag = kanFlag;
    }


    @Override
    protected String doInBackground(String... params) {
        //Log.i(TAG, "Started doInBackground of DBUpdateCheckTask");
        String dbKanUpdateCheckURL = null;
        String dbArabicUpdateCheckURL = null;
        String version=null;
        try{
            String app_version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            //Log.i(TAG, "App Version  is "+ app_version);
            if(kannadaFlag){
                dbKanUpdateCheckURL = KQ_KanDB_VERSION_CHECK_URL + app_version;
                //Log.i(TAG, "URL  is "+ dbKanUpdateCheckURL);
                version = getLatestDBVersion(dbKanUpdateCheckURL);
            }else{
                dbArabicUpdateCheckURL = KQ_ArabicDB_VERSION_CHECK_URL + app_version;
                //Log.i(TAG, "URL  is "+ dbArabicUpdateCheckURL);
                version = getLatestDBVersion(dbArabicUpdateCheckURL);
            }
        }
        catch (Exception e){
            Log.e(TAG, "error getting app Version number "+ e);
        }
        return version;
    }

    @Override
    protected void onPostExecute(String result){
        Log.i(TAG, "Started onPostExecute of DBUpdateCheckTask, Result is  "+ result);
        int intLatestVersion=0;
        if(!TextUtils.isEmpty(result)){
            result.trim();
            try{
                intLatestVersion=Integer.parseInt(result);
            }
            catch (Exception e){
                Log.e(TAG, " Exception in getting the text version from the server "+ e);
            }

        }
        if(kannadaFlag){
            DatabaseHandler dbHandler = new DatabaseHandler(mContext, QuranDataProvider.QURAN_KAN_DATABASE);
            int localKanndaTextVersion = dbHandler.getTextVersion();
            dbHandler.closeDatabase();
            Log.i(TAG, "localKanndaTextVersion  is "+ localKanndaTextVersion+" intLatestVersion is "+intLatestVersion);
            if(localKanndaTextVersion < intLatestVersion){
                //Log.i(TAG, "localKanndaTextVersion(should be lesser)  is "+ localKanndaTextVersion +" intLatestVersion(should be greater) is "+intLatestVersion );

                String url = QuranFileUtils.getKanSearchDatabaseUrl();
                String notificationTitle = mContext.getString(R.string.download_kan_db);
                Intent intent = ServiceIntentHelper.getDownloadIntent(mContext, url,
                        QuranFileUtils.getQuranDatabaseDirectory(mContext), notificationTitle,
                        QuranDownloadService.DB_DOWNLOAD_KEY, QuranDownloadService.DOWNLOAD_TYPE_KAN_SEARCH_DB);
                intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME,
                        QuranDataProvider.QURAN_KAN_DATABASE);
                mContext.startService(intent);
            }
        }else{
            DatabaseHandler dbHandler = new DatabaseHandler(mContext, QuranDataProvider.QURAN_ARABIC_DATABASE);
            int localArabicTextVersion = dbHandler.getTextVersion();
            dbHandler.closeDatabase();
           // Log.i(TAG, "localArabicTextVersion  is "+ localArabicTextVersion);
            if(localArabicTextVersion < intLatestVersion){
               // Log.i(TAG, "localArabicTextVersion(should be lesser)  is "+ localArabicTextVersion +" intLatestVersion(should be greater) is "+intLatestVersion );

                String url = QuranFileUtils.getKanSearchDatabaseUrl();
                String notificationTitle = mContext.getString(R.string.download_arabic_db);
                Intent intent = ServiceIntentHelper.getDownloadIntent(mContext, url,
                        QuranFileUtils.getQuranDatabaseDirectory(mContext), notificationTitle,
                        QuranDownloadService.DB_DOWNLOAD_KEY, QuranDownloadService.DOWNLOAD_TYPE_ARABIC_SEARCH_DB);
                intent.putExtra(QuranDownloadService.EXTRA_OUTPUT_FILE_NAME,
                        QuranDataProvider.QURAN_ARABIC_DATABASE);
                mContext.startService(intent);
            }
        }
        return;
    }


    public String getLatestDBVersion(String urlString){
        InputStream stream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);

            conn.connect();
            stream = conn.getInputStream();

            String result = "";
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            String line = "";
            while ((line = reader.readLine()) != null){
                result += line;
            }

            try { reader.close(); }
            catch (Exception e){ }
Log.i(TAG,"result is ---> "+result);
            return result;
        }
        catch (Exception e){
            Log.e(TAG, "error downloading translation data: " + e);
        }
        return null;
    }
}
