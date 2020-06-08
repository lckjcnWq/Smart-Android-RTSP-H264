package com.example.rtsp_tcp_nio.coder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.RequiresApi;
import com.example.rtsp_tcp_nio.Constant;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class RtspDecoder {
    private final static String tag = RtspDecoder.class.getName();
    private static final String VCODEC_MIME = "video/avc";
    private static RtspDecoder instance;
    //处理音视频的编解码的类MediaCodec
    private MediaCodec video_decoder;
    //显示画面的Surface
    private Surface surface;
    private ConcurrentLinkedQueue<byte[]> streams;
    private AtomicBoolean status = new AtomicBoolean(false);
    private boolean isRuning = false;

    //    public static RtspDecoder getInstance(Surface surface) {
    //        if (instance == null) {
    //            instance = new RtspDecoder(surface);
    //        }
    //        return instance;
    //    }

    public RtspDecoder(Surface surface) {
        this.surface = surface;
        this.streams = new ConcurrentLinkedQueue();
        this.status.set(true);
        initMediaCodec();
    }

    private void initMediaCodec() {
        try {
            if (video_decoder != null) {
                video_decoder.stop();
                video_decoder.release();
                video_decoder = null;
            }
            video_decoder = MediaCodec.createDecoderByType(VCODEC_MIME);
            // Create the format settinsg for the MediaCodec
            //            MediaFormat format = MediaFormat.createVideoFormat(VCODEC_MIME, 1280, 720);// MIMETYPE: a two-part identifier for file formats and format contents
            MediaFormat format = MediaFormat.createVideoFormat(VCODEC_MIME, 1920,
                    1080);// MIMETYPE: a two-part identifier for file formats and format contents
            // Set the PPS and SPS frame
            byte[] header_sps = { 0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 10, 2, -1, -107 };
            byte[] header_pps = { 0, 0, 0, 1, 104, -18, 56, -128 };
            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            // Configure the Codec
            video_decoder.configure(format, surface, null, 0);
            // Start the codec
            video_decoder.start();
            isRuning = true;
            Log.i(tag, "initMediaCodec  ------>");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runDecodeVideoThread();
                }
            }).start();
        } catch (Exception e) {
            Log.i(tag, "initMediaCodec  Exception  ------>" + e.toString());
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        isRuning = false;
        if (status.compareAndSet(true, false)) {
            releaseResource();
        }
        if (video_decoder != null) {
            video_decoder.stop();
            video_decoder.release();
            video_decoder = null;
        }
    }

    public void setVideoData(byte[] stream) {
        streams.add(stream);
        Log.i("msh", "tmp11  -------->=" + streams.isEmpty());
        this.waiteUp();
    }

    private synchronized void waiteUp() {
        this.notifyAll();
    }

    public void releaseResource() {
        if (status.compareAndSet(true, false)) {
            Log.i(tag, "releaseResource  streams-------");
            streams.clear();
            this.waiteUp();
        }
    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void runDecodeVideoThread() {
        while (status.get() && isRuning) {
            try {
                //                Log.i(tag, "inIndex before--------------------------------------------");
                int inIndex = video_decoder.dequeueInputBuffer(-1);
                //                Log.i(tag, "inIndex after-------------------------------------------->  " + inIndex);
                if (inIndex >= 0) {
                    ByteBuffer buffer = video_decoder.getInputBuffer(inIndex);
                    buffer.clear();
                    //                    Log.i(tag, "inIndex clear-------------------------------------------->  ");
                    if (!streams.isEmpty()) {
                        byte[] data = (byte[]) streams.poll();
                        buffer.put(data);
                        Log.i(tag, "inIndex data--------------------------------------------"
                                + data.length);
                        video_decoder.queueInputBuffer(inIndex, 0, data.length,
                                System.nanoTime() / 1000, 0);
                    } else {
                        video_decoder.queueInputBuffer(inIndex, 0, 0, System.nanoTime() / 1000, 0);
                    }
                }
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                int outIndex = video_decoder.dequeueOutputBuffer(info, 0);
                //                Log.i(tag, "inIndex ------=" + inIndex + "   ,outIndex----->" + outIndex);
                if (outIndex >= 0) {
                    video_decoder.releaseOutputBuffer(outIndex, true);
                }
            } catch (Exception var4) {
                Log.e(Constant.LOG_TAG, "unpackRtp Exception= :" + var4.toString());
                var4.printStackTrace();
            }
        }
    }
}