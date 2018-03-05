package com.demo.shawn.signature;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    Button btn;
    PopupWindow popupWindow;
    SignatureView signatureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        verifyStoragePermissions();
    }

    public void clk(View v) {
        popupWindow = new PopupWindow(this);
        popupWindow.setContentView(View.inflate(this, R.layout.popupwindow, null));
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(btn, Gravity.CENTER, 0, 0);
        View view = popupWindow.getContentView();
        signatureView = (SignatureView) view.findViewById(R.id.signView);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_reset).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                Log.e("111", path);
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    signatureView.SaveBitmapToFile(path);
                }
            case R.id.btn_cancel:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                break;
            case R.id.btn_reset:
                signatureView.reset();
                break;
        }
    }

    public void verifyStoragePermissions() {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
