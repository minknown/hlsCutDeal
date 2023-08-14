# hlsCutDeal
基于HLS下，将视频切成多份TS的m3u8程序 -基于java和ffmpeg  
*每30秒一个片段，如果需要更改每个片段的时间，直接到源码中的cmd语句中修改即可*
````
//在以下定义参数，一个是ffmpeg的插件文件名，另一个视频存放的文件夹。
//文件夹内所有视频均会被切割转化成多个ts视频片段。
public static String ffmpeg="F:\\ffmpeg\\bin\\ffmpeg.exe";
    public static String mpdir="E:\\mp4";
````
