### RTSP长连接进行H264拉流

本案例是基于RTSP协议进行H264拉流的案列，并且把H264流以文件的格式保存到本地以及显示在常见的TextureView或者SurfaceView上

### 当前内容

1.拉流H264保存到本地

2.拉流H264显示在SurfaceView上

3.拉流H264显示在TextureView上

### 使用说明

初始化

```java
client = new TCP4RtspUtil("rtsp路径", new VideoStreamImpl(new H264StreamInterface() {
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
 public void process(final byte[] stream) {

 });
client.doStart();
client.play();
```

通过上面就能获取到H264流的回调








