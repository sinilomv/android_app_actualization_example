package com.github.exaples.android_app_actualization_example.httpcall;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class UpdateApp extends AsyncTask<String,Void,Void> {

    private Activity context;
    private Intent intent;

    public void setContext(Activity contextf){
        context = contextf;
    }

    private int API = Build.VERSION.SDK_INT;

    @Override
    protected Void doInBackground(String... arg0) {


        try {

            URL url = new URL(arg0[0]);
            //URL url = new URL("http://192.168.11.122:8080/KEFAS/docs_apk/1.1/seihodocs.apk");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            Uri contentUri;
            File storageDir;
            File outputFile;

            if(API >= 24){

                storageDir = new File(context.getApplicationContext().getExternalCacheDir().getPath());

                outputFile = new File(storageDir, "kefas.apk");
                contentUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName(),
                        outputFile);

                intent = new Intent(Intent.ACTION_VIEW, contentUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else{
                storageDir = new File(context.getApplicationContext().getExternalCacheDir().getPath());

                outputFile = new File(storageDir, "kefas.apk");
                contentUri = Uri.fromFile(outputFile);

                intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            if(!storageDir.isDirectory()){
                storageDir.mkdir();
            }

            if(outputFile.exists()){
               outputFile.delete();
            }

            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }

            fos.close();
            is.close();


            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
            context.finish();


        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());

        }
        return null;
    }


}