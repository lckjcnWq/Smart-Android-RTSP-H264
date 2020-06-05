package com.example.rtsp_tcp_nio.view;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.blankj.utilcode.util.FileUtils;
import com.example.rtsp_tcp_nio.Constant;
import com.example.rtsp_tcp_nio.R;
import com.example.rtsp_tcp_nio.coder.RtspDecoder;
import com.example.rtsp_tcp_nio.listener.H264StreamInterface;
import com.example.rtsp_tcp_nio.listener.VideoStreamImpl;
import com.example.rtsp_tcp_nio.utils.AppUtils;
import com.example.rtsp_tcp_nio.utils.TCP4RtspUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RtspPullStreamActivity extends AppCompatActivity {
    private String filePath;

    private boolean isFirst = true;
    InputStream is;
    private RtspDecoder mPlayer = null;
    private String rtsp_url;
    private TCP4RtspUtil client;
    private String tag = RtspPullStreamActivity.class.getName();
    private TextView tv_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_rtsp);
        tv_show=findViewById(R.id.tv_show);
        rtsp_url = getIntent().getStringExtra(Constant.RTSP_URL);
        //边播放边保存到SD卡文件目录
        filePath = getFilesDir()+ "/test.h264";
        FileUtils.createOrExistsFile(filePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getRtspStream();
                    client.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getRtspStream() throws Exception {
        client = new TCP4RtspUtil(rtsp_url, new VideoStreamImpl(new H264StreamInterface() {
            private OutputStream out = new FileOutputStream(filePath);

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void process(final byte[] stream) {
                try {
                    this.out.write(stream);
                    if (AppUtils.isIFrame(stream)) {
//                        Log.i(tag,"我是I帧");
                    }else {
//                        Log.i(tag,"我是B帧");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(stream!=null)
                                tv_show.setText("接收数据大小:"+stream.length +"   H264文件保存位置为："+filePath);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        out.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

            }
        }));
        client.doStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client!=null){
            client.doStop();
        }
    }
}
