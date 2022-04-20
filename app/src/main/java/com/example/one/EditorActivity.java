package com.example.one;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.one.util.ToastUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditorActivity extends AppCompatActivity implements PermissionInterface {
    //声明控件
    private ImageButton mBtn_back;
    private PermissionHelper mPermissionHelper;
    private Button mBtn_release;
    private EditText mEt_title;
    private EditText mEt_text;
    private ImageButton mBtn_insert_picture;
    private RadioGroup rg;
    private RadioButton rb_study, rb_sport, rb_association_event, rb_other;
    private TextView tv_label_tip;
    private int id;

    DBUtils db;
    ResultSet rs;
    Thread t;
    int access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_page);
        //初始化并发起权限申请
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();

        //控件部分
        mBtn_back = findViewById(R.id.editor_page_1_button_back);
        mBtn_release = findViewById(R.id.editor_page_1_button_release);
        mEt_title = findViewById(R.id.editor_page_edittext_title);
        mEt_text = findViewById(R.id.editor_page_edittext_text);
        mBtn_insert_picture = findViewById(R.id.editor_page_2_button_insert_picture);
        rb_study = findViewById(R.id.editor_page_3_radiobutton_study);
        rb_sport = findViewById(R.id.editor_page_3_radiobutton_sport);
        rb_association_event = findViewById(R.id.editor_page_3_radiobutton_association_event);
        rb_other = findViewById(R.id.editor_page_3_radiobutton_other);
        rg = findViewById(R.id.editor_page_radiogroup_3);
        tv_label_tip = findViewById(R.id.editor_page_textview_label_tip);

        rg.setOnCheckedChangeListener(cBoxListener);

        //返回
        mBtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                intent = new Intent(EditorActivity.this, HomePage.class);
                startActivity(intent);
            }
        });
        mBtn_release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEt_title.getText().toString().equals("") || mEt_text.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "标题和内容不能为空", Toast.LENGTH_LONG).show();
                } else {
                    if (mEt_title.getText().toString().length() > 40 || mEt_text.getText().toString().length() > 500) {
                        Toast.makeText(getApplicationContext(), "标题或内容字数超过最大限制", Toast.LENGTH_LONG).show();
                    } else {
                        id = rg.getCheckedRadioButtonId();
                        String str_label;
                        if (R.id.editor_page_3_radiobutton_study == id) {
                            str_label = "study";
                            access = 1;
                        } else if (R.id.editor_page_3_radiobutton_sport == id) {
                            str_label = "sport";
                            access = 1;
                        } else if (R.id.editor_page_3_radiobutton_association_event == id) {
                            str_label = "association_event";
                            String sql = "select User_access from User where User_phone ='" + new SaveSharedPreference().getPhone() + "'";
                            DBUtils dbUtils = new DBUtils();
                            try {
                                t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rs = dbUtils.query(sql);
                                        try {
                                            if (rs.next()) {
                                                access = rs.getInt("User_access");
                                                rs.previous();
                                            }
                                        } catch (SQLException throwables) {
                                            throwables.printStackTrace();
                                        }
                                    }
                                });
                                t.start();
                                while (t.isAlive()) ;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            str_label = "other";
                            access = 1;
                        }
                        if (access != 1) {
                            Toast.makeText(getApplicationContext(), "没有权限发布社团活动消息！", Toast.LENGTH_LONG).show();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "发布成功！", Toast.LENGTH_LONG).show();
                                    Date PDate = new Date();
                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String strDate = dateFormat.format(PDate);

                                    String sql = "insert into Forumt(F_title, Forumt_content,Forumt_date,User_phone,User_name,F_likenum,F_collectnum,F_commentnum,F_lable) " +
                                            "values ('" + mEt_title.getText().toString() + "', '" + mEt_text.getText().toString() + "','" + strDate + "','" + new SaveSharedPreference().getPhone() + "'" +
                                            ",'" + new SaveSharedPreference().getUsername() + "','" + 0 + "','" + 0 + "','" + 0 + "','" + str_label + "');";
                                    DBUtils dbUtils = new DBUtils();
                                    try {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                dbUtils.update(sql);
                                            }
                                        }).start();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //跳转页面
                                    Intent intent = null;
                                    intent = new Intent(EditorActivity.this, HomePage.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private RadioGroup.OnCheckedChangeListener cBoxListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if (R.id.editor_page_3_radiobutton_study == i) {
                tv_label_tip.setText("您选择的标签：" + rb_study.getText().toString());
                setTitle(String.valueOf(rb_study.getText()));
            } else if (R.id.editor_page_3_radiobutton_sport == i) {
                tv_label_tip.setText("您选择的标签：" + rb_sport.getText().toString());
                setTitle(String.valueOf(rb_sport.getText()));
            } else if (R.id.editor_page_3_radiobutton_association_event == i) {
                tv_label_tip.setText("您选择的标签：" + rb_association_event.getText().toString());
                setTitle(String.valueOf(rb_association_event.getText()));
            } else {
                tv_label_tip.setText("您选择的标签：" + rb_other.getText().toString());
                setTitle(String.valueOf(rb_other.getText()));
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        //设置该界面所需的全部权限
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, //存储权限
                Manifest.permission.CAMERA, //相机权限
                Manifest.permission.READ_CALENDAR
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        //权限请求用户已经全部允许
        initViews();
    }

    @Override
    public void requestPermissionsFail() {
        //权限请求不被用户允许。可以提示并退出或者提示权限的用途并重新发起权限申请。
        //finish(); //这里就直接闪退了
        ToastUtil.showMsg(EditorActivity.this, "建议打开相应的权限");
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    private void initViews() {
        //已经拥有所需权限，可以放心操作任何东西了
    }

}