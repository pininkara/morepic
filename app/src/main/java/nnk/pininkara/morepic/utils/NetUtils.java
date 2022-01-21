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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.http.HttpRequest;

public class NetUtils {

    /**
     * 获得响应体
     *
     * @param allTag tag
     * @param start    检索数量
     * @return 图片列表
     * @throws IOException 读取响应体的io异常
     */
    public static List<ImageData> getResponse(String allTag, int start,int end) throws IOException {
        List<ImageData> data = new ArrayList<>();
        allTag = allTag.substring(1);
        for (int i = start; i <= end; i++) {
            String url = "https://yande.re/post?page=" + i + "&tags=" + allTag;
            Log.d("nnk223", "Url: " + url);
            String tempResponse = HttpRequest.get(url)
                    .timeout(30000)
                    .execute().body();
            List<ImageData> imageData = getData(tempResponse);
            if (!data.addAll(imageData)) {
                Log.d("nnk223", "getResponse: 出错了");
            }
        }
        Log.d("nnk223", "getResponse: " + data.size());
        return data;
    }

    /**
     * 从response中获取图片信息
     *
     * @param response 响应体
     * @return 图片信息
     * @throws IOException 读取响应体的io异常
     */
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
        return data;
    }

    /**
     * 筛选满足质量因子的图片
     *
     * @param data   图片列表
     * @param factor 质量因子
     * @return 筛选后的图片列表
     */
    public static List<ImageData> chosenData(List<ImageData> data, int factor) {
        List<ImageData> chosen = new ArrayList<>();
        for (ImageData imageData : data) {
            Log.d("nnk223", "Score: " + imageData.getScore());
            if (imageData.getScore() >= factor) {
                chosen.add(imageData);
            }
        }
        Log.d("nnk223", "chosenData: " + chosen.size());
        return chosen;
    }

    /**
     * 下载文件至标准目录
     *
     * @param context    上下文
     * @param chosenData 满足质量因子的图片列表
     * @param sub        子目录名
     */
    public static void downloadMethod(Context context, List<ImageData> chosenData, String sub) {
        File file=new File(Environment.DIRECTORY_PICTURES+"/morePic/"+sub+"/.nomedia");
        Log.d("nnk223", "FilePath: "+file.getPath());
        Log.d("nnk223", "exist: "+file.exists());
        if (!file.exists()){
            try {
                Log.d("nnk223", "文件创建: "+file.createNewFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (ImageData data : chosenData) {
            String fileUrl = data.getFile_url();
            Log.d("nnk223", "download: " + fileUrl);
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
            //指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "/morePic/" + sub + "/" + fileUrl.substring(fileUrl.lastIndexOf("/") + 1));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle("morePic");
            request.setDescription("morePic正在下载");
            request.setAllowedOverRoaming(false);
//            //设置文件存放目录
//            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "");
            //获取下载管理器
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            //将下载任务加入下载队列，否则不会进行下载
            downloadManager.enqueue(request);
        }
    }



}

