package com.example.rtsp_tcp_nio.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.rtsp_tcp_nio.Constant;
import com.example.rtsp_tcp_nio.R;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private EditText rtsp_edt;
    private Button mBtnRtspPull, mBtnRtspSv, mBtnRtspTv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rtsp_edt = (EditText) findViewById(R.id.rtsp_edt);
        XXPermissions.with(this)
                .constantRequest()
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {

                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {

                    }
                });
        mBtnRtspPull = findViewById(R.id.btn_rtsp_pull);
        mBtnRtspSv = findViewById(R.id.btn_rtsp_sv);
        mBtnRtspTv = findViewById(R.id.btn_rtsp_tv);
        mBtnRtspPull.setOnClickListener(this);
        mBtnRtspSv.setOnClickListener(this);
        mBtnRtspTv.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rtsp_pull:
                toActivity(RtspPullStreamActivity.class);
                break;
            case R.id.btn_rtsp_sv:
                toActivity(RtspSurfaceViewActivity.class);
                break;
            case R.id.btn_rtsp_tv:
                toActivity(RtspTextureViewActivity.class);
                break;
        }
    }

    private void toActivity(Class<?> cls) {
        //视频流地址，例如：rtsp://192.168.1.168:80/0
        String url = rtsp_edt.getText().toString().trim();
        if (TextUtils.isEmpty(url) || !url.startsWith("rtsp://")) {
            Toast.makeText(MainActivity.this, "RTSP视频流地址错误！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, cls);
        intent.putExtra(Constant.RTSP_URL, url);
        startActivity(intent);
    }
}
