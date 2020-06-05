package com.example.rtsp_tcp_nio.utils;

/**
 * <pre>
 * author : Administrator
 * time : 2020/06/05
 * </pre>
 */
public class AppUtils {
    /**
     * 判斷是否是Izhen
     */
    public static boolean isIFrame(byte[] frame) {
        int framType = frame[4] & 0x1F;
        if (framType == 5 || framType == 7 || framType == 8) {
            return true;
        }
        return false;
    }
}
