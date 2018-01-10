package com.example.seongjun.biocube;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
        //String uploadImgPath = "http://fungdu0624.phps.kr/biocube/users/"+((UserMainActivity)getActivity()).userID+"/"+attachmentFileName;
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
            }

            // create a buffer of  maximum size
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

            if(serverResponseCode == 200){
                result = "success";
            }

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
