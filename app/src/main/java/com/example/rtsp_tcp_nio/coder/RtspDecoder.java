package com.example.rtsp_tcp_nio.coder;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RtspDecoder {
    //处理音视频的编解码的类MediaCodec
    private MediaCodec video_decoder;
    //显示画面的Surface
    private Surface surface;
    //视频数据
    private BlockingQueue<byte[]> video_data_Queue = new ArrayBlockingQueue<byte[]>(10000);

    private ByteBuffer[] inputBuffers;
    private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    private boolean isRuning = false;

    public RtspDecoder(Surface surface) {
        this.surface = surface;
        try {
            initMediaCodec();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRunning() {
        video_data_Queue.clear();
        if (video_decoder != null) {
            video_decoder.stop();
            video_decoder.release();
            video_decoder = null;
        }
    }

    //添加视频数据
    public void setVideoData(byte[] data) {
        try {
            video_data_Queue.offer(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initMediaCodec() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
        byte[] header_sps = {0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 10, 2, -1, -107};
        byte[] header_pps = { 0, 0, 0, 1, 104, -18, 56, -128 };
            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        if (video_decoder != null) {
            video_decoder.stop();
            video_decoder.release();
            video_decoder = null;
        }
        video_decoder = MediaCodec.createDecoderByType("video/avc");
        video_decoder.configure(format, surface, null, 0);
        video_decoder.start();
        inputBuffers = video_decoder.getInputBuffers();
        isRuning = true;
        runDecodeVideoThread();
    }

    /**
     */
    private void runDecodeVideoThread() {

        Thread t = new Thread() {

            @SuppressLint({ "NewApi", "WrongConstant" })
            public void run() {

                while (isRuning) {

                    int inIndex = -1;
                    try {
                        inIndex = video_decoder.dequeueInputBuffer(-1);
                    } catch (Exception e) {
                        return;
                    }
                    try {

                        if (inIndex >= 0) {
                            ByteBuffer buffer = inputBuffers[inIndex];
                            buffer.clear();

                            if (!video_data_Queue.isEmpty()) {
                                byte[] data;
                                data = video_data_Queue.take();
                                buffer.put(data);
                                video_decoder.queueInputBuffer(inIndex, 0, data.length,
                                        System.nanoTime() / 1000, 0);
                            } else {
                                video_decoder.queueInputBuffer(inIndex, 0, 0,
                                        System.nanoTime() / 1000, 0);
                            }
                        }

                        int outIndex = -1;
                        try {
                            outIndex = video_decoder.dequeueOutputBuffer(info, 0);
                        } catch (Exception e) {
                            return;
                        }
                        if (outIndex >= 0) {
                            video_decoder.releaseOutputBuffer(outIndex, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        };

        t.start();
    }

}