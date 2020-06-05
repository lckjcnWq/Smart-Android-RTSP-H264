package com.example.rtsp_tcp_nio.view;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rtsp_tcp_nio.Constant;
import com.example.rtsp_tcp_nio.R;
import com.example.rtsp_tcp_nio.coder.RtspDecoder;
import com.example.rtsp_tcp_nio.listener.H264StreamInterface;
import com.example.rtsp_tcp_nio.listener.VideoStreamImpl;
import com.example.rtsp_tcp_nio.utils.TCP4RtspUtil;

public class RtspTextureViewActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener {
    private String filePath;
    private RtspDecoder mPlayer = null;
    private String rtsp_url;
    private TCP4RtspUtil client;
    private String tag = RtspTextureViewActivity.class.getName();
    private TextView tv_show;
    private TextureView mTextureView;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_rtsp_texture);
        tv_show = findViewById(R.id.tv_show);
        mTextureView = findViewById(R.id.tuxtureview);
        rtsp_url = getIntent().getStringExtra(Constant.RTSP_URL);
        //边播放边保存到SD卡文件目录
        //      filePath = getFilesDir() + "/test.h264";
        //      FileUtils.createOrExistsFile(filePath);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        isFirst = true;
        mPlayer = new RtspDecoder(new Surface(mTextureView.getSurfaceTexture()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getRtspStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void getRtspStream() throws Exception {
        client = new TCP4RtspUtil(rtsp_url, new VideoStreamImpl(new H264StreamInterface() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void process(final byte[] stream) {
                try {
                    if (stream != null && stream.length > 0) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            mPlayer.setVideoData(stream);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_show.setText(
                                        "接收数据大小:" + stream.length + "   H264文件保存位置为：" + filePath);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.i(tag, "getRtspStream Exception  =" + e.toString());
                    e.printStackTrace();
                }
            }
        }));
        client.doStart();
        client.play();
    }

    @Override
    protected void onDestroy() {
        isFirst = false;
        super.onDestroy();
    }

    private void release() {
        if (mPlayer != null) {
            mPlayer.stopRunning();
        }
        if (client != null) {
            client.doStop();
        }
    }
}
