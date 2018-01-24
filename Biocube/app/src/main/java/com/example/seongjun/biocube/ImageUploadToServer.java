package com.example.seongjun.biocube;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Eunmi on 2018-01-10.
 */

/* 사진 서버에 업로드용 쓰레드 */
class ImageUploadToServer extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... params) {
        String result = "false";
        // 기타 필요한 내용
        String urlString = params[0];
        String attachmentName = params[1];
        String attachmentFileName = params[2];
        String uploadImgPath = params[3];

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            FileInputStream mFileInputStream = new FileInputStream(params[4]);

            URL connectUrl = new URL(urlString);

            // open connection
            HttpURLConnection con = (HttpURLConnection) connectUrl.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            con.setRequestProperty(params[1], params[4]);

            // write data
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            if(params[1].equals("uploadfile_for_diary")) {
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file_path\"" + lineEnd + lineEnd + uploadImgPath + lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
            } else if(params[1].equals("uploadfile_repimage")) {
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file_path\"" + lineEnd + lineEnd + uploadImgPath + lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"plant_name\"" + lineEnd + lineEnd + URLEncoder.encode(params[5], "UTF-8") + lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
            } else if(params[1].equals("uploadfile_for_manual")){

            }

            // create a buffer of maximum size
            int bytesAvailable = mFileInputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = mFileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = con.getResponseCode();
            String serverResponseMessage = con.getResponseMessage();
            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

            InputStream inStream = con.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            result = reader.readLine();

            //close the streams //
            mFileInputStream.close();
            dos.flush();
            dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(result.equals("success")) {
            return true;
        } else {
            return false;
        }
    }
}
