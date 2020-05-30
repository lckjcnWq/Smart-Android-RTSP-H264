package com.example.rtsp_tcp_nio.listener;

public interface VideoStreamInterface {
    void onVideoStream(byte[] var1);
    void releaseResource();
}