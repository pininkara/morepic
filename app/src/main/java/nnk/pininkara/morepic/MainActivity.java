package nnk.pininkara.morepic;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import nnk.pininkara.morepic.utils.ImageData;
import nnk.pininkara.morepic.utils.NetUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText tag_edit, start_edit, end_edit, factor_edit, sub_edit;
    Button tag_btn, start_btn, exit_btn;
    TextView textView;

    String allTag = "";
    String sub = "";
    List<ImageData> list = new ArrayList<>();
    int factor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        tag_edit = findViewById(R.id.tag_edit);
        start_edit = findViewById(R.id.start_edit);
        end_edit = findViewById(R.id.end_edit);
        factor_edit = findViewById(R.id.factor_edit);
        sub_edit = findViewById(R.id.sub_edit);
        tag_btn = findViewById(R.id.tag_btn);
        start_btn = findViewById(R.id.start_btn);
        exit_btn = findViewById(R.id.exit_btn);
        textView = findViewById(R.id.textView);

        tag_btn.setOnClickListener(this);
        start_btn.setOnClickListener(this);
        exit_btn.setOnClickListener(this);
    }


    /**
     * Button点击事件监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tag_btn:
                String tag = tag_edit.getText().toString().trim();
                Log.d("nnk", "onClick: " + tag);
                if (tag.equals("")) {
                    Toast.makeText(this, "TAG不能为空！", Toast.LENGTH_SHORT).show();
                } else if (Pattern.matches("\\W+", tag)) {
                    Toast.makeText(this, "TAG只能有字母数字下划线", Toast.LENGTH_SHORT).show();
                } else {
                    allTag = allTag + "+" + tag;
                    Toast.makeText(this, tag + "已添加！\n" + "当前tag有:" + allTag, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.start_btn:
                int start_num = start_edit.getText().toString().trim().equals("") ? 1 : Integer.parseInt(start_edit.getText().toString().trim());
                int end = end_edit.getText().toString().trim().equals("") ? 2 : Integer.parseInt(end_edit.getText().toString().trim());
                factor = factor_edit.getText().toString().trim().equals("") ? 100 : Integer.parseInt(factor_edit.getText().toString().trim());
                sub = sub_edit.getText().toString().trim().equals("") ? String.valueOf(System.currentTimeMillis()) : sub_edit.getText().toString().trim();
                Log.d("nnk223", "start: "+start_num);
                Log.d("nnk223", "end: "+end);
                if (start_num <= end && start_num >= 1) {
                    if (!allTag.equals("")) {
                        Toast.makeText(this, "任务进行中，请等待~", Toast.LENGTH_SHORT).show();
                        new Thread(() -> {
                            try {
                                list = NetUtils.getResponse(allTag, start_num, end);
                                handler.sendEmptyMessage(1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                } else {
                    Toast.makeText(this, "起止页码错误，请重新输入", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.exit_btn:
                finish();
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d("nnk223", "handleMessage: " + "已经完毕");
                    textView.setText("检索到" + list.size() + "张图片，即将进行筛选\n");
                    start_btn.setVisibility(View.GONE);
                    tag_btn.setVisibility(View.INVISIBLE);
                    processData();
                    break;
                case 2:
                    Log.d("nnk223", "handleMessage: " + "筛选完毕");
                    if (list.size() == 0) {
                        textView.append("oops!没有筛选到符合要求的图片，请增加数量或减少质量因子");
                        exit_btn.setVisibility(View.VISIBLE);
                    } else {
                        textView.append("筛选到" + list.size() + "张图片，正在添加下载任务\n");
                        download();
                    }

                    break;
                case 3:
                    Log.d("nnk223", "handleMessage: 下载添加完成");
                    textView.append("下载任务添加完成，您可以在通知中查看下载进度\n");
                    textView.append("感谢您的使用~");
                    exit_btn.setVisibility(View.VISIBLE);

            }
        }
    };

    private void processData() {
        new Thread(() -> {
            list = NetUtils.chosenData(list, factor);
            handler.sendEmptyMessage(2);
        }).start();
    }

    private void download() {
        new Thread(() -> {
            NetUtils.downloadMethod(this, list, sub);
            handler.sendEmptyMessage(3);
        }).start();
    }

    private void openAssignFolder(File file) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "file/*");
        try {
            startActivity(intent);
//            startActivity(Intent.createChooser(intent,"选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}