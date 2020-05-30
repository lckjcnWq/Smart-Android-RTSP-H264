package com.example.rtsp_tcp_nio.listener;

import java.io.IOException;

public interface RtspInterface {
    void doStart() throws IOException;
    void doStop();
}