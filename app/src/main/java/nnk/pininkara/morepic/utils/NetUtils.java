// 2022/1/20 18:07

package nnk.pininkara.morepic.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.hutool.http.HttpRequest;

public class NetUtils {


    public static void getResult(Context context,List<String> tags,int num,int factor,String sub) throws IOException {
        Log.d("nnk223", "getResult: "+num+"factor:"+factor);
        List<ImageData> data = getResponse(tags,num);
        List<ImageData> chosenData = chosenData(data,factor);
        download(context,chosenData,sub);

    }

    private static List<ImageData> getResponse(List<String> tags,int num) throws IOException {
        String param = "";
        List<ImageData> data=new ArrayList<>();
        for (String tag : tags) {
            param = param + "+" + tag;
        }
        param = param.substring(1);
        Log.d("nnk223", "getResponse: "+"https://yande.re/post?tags="+param);
            String tempResponse = HttpRequest.get("https://yande.re/post?tags="+param)
                    .timeout(30000)
                    .execute().body();
            List<ImageData> imageData = getData(tempResponse);
            if (!data.addAll(imageData)){
                Log.d("nnk223", "getResponse: 出错了");
            }

        Log.d("nnk223", "getResponse: "+data);
        return data;
    }

    private static List<ImageData> getData(String response) throws IOException {
        String line;
        List<String> json = new ArrayList<>();
        List<ImageData> data = new ArrayList<>();

        Gson gson = new Gson();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8))));

        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("Post.register")) {
                if (!line.contains("Post.register_tags")) {
                    String string = line.trim().substring(14);
                    json.add(string.substring(0, (string.length() - 1)));
                }
            }
        }
        for (String s : json) {
            data.add(gson.fromJson(s, ImageData.class));
        }
        Log.d("nnk223", "getData: "+data);
        return data;
    }

    private static List<ImageData> chosenData(List<ImageData> data,int factor){
        List<ImageData> chosen=new ArrayList<>();
        for (ImageData imageData : data) {
            Log.d("nnk223", "Score: "+imageData.getScore());
            if (imageData.getScore()>=factor){
                chosen.add(imageData);
            }
        }
        Log.d("nnk223", "chosenData: "+chosen);
        return chosen;
    }

    private static boolean download(Context context,List<ImageData> chosenData,String sub){
        for (ImageData data : chosenData) {
            String fileUrl = data.getFile_url();
            Log.d("nnk223", "download: "+fileUrl);
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
            //指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/morePic/"+sub+"/"+fileUrl.substring(fileUrl.lastIndexOf("/") + 1));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle("下载中~");
            request.setDescription("morePic正在下载");
            request.setAllowedOverRoaming(false);
//            //设置文件存放目录
//            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "");
            //获取下载管理器
            DownloadManager downloadManager= (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            //将下载任务加入下载队列，否则不会进行下载
            downloadManager.enqueue(request);
        }
        return true;
    }
}

