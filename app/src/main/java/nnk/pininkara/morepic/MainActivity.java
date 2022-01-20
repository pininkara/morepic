package nnk.pininkara.morepic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import nnk.pininkara.morepic.utils.NetUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText tag_edit,num_edit,factor_edit,sub_edit;
    Button tag_btn,start_btn;

    List<String> tags=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        tag_edit=findViewById(R.id.tag_edit);
        num_edit=findViewById(R.id.num_edit);
        factor_edit=findViewById(R.id.factor_edit);
        sub_edit=findViewById(R.id.sub_edit);
        tag_btn=findViewById(R.id.tag_btn);
        start_btn=findViewById(R.id.start_btn);

        tag_btn.setOnClickListener(this);
        start_btn.setOnClickListener(this);
    }


    /**
     * Button点击事件监听
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tag_btn:
               String tag=tag_edit.getText().toString().trim();
                Log.d("nnk", "onClick: "+tag);
               if (tag.equals("")){
                   Toast.makeText(this, "TAG不能为空！", Toast.LENGTH_SHORT).show();
               }else if(Pattern.matches("\\W+", tag)){
                   Toast.makeText(this, "TAG只能有字母数字下划线", Toast.LENGTH_SHORT).show();
               }else {
                   tags.add(tag);
                   Log.d("nnk223", "onClick: ");
                   Toast.makeText(this, tag+"已添加！", Toast.LENGTH_SHORT).show();
               }
                break;
            case R.id.start_btn:
                int num= num_edit.getText().toString().trim().equals("") ?1:Integer.parseInt(num_edit.getText().toString().trim());
                int factor= factor_edit.getText().toString().trim().equals("") ?100:Integer.parseInt(factor_edit.getText().toString().trim());
                String sub= sub_edit.getText().toString().trim().equals("") ?String.valueOf(System.currentTimeMillis()):sub_edit.getText().toString().trim();
                if (tags.size()!=0){
                    Toast.makeText(this, "任务进行中，请等待~", Toast.LENGTH_SHORT).show();
                        new Thread(()->{
                            try {
                                NetUtils.getResult(this,tags,num,factor,sub);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();

                }
                break;
        }
    }
}