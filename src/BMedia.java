import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

/**
 * 本例程为学习JavaFx，处理剪辑版，两字符串取中间的问题，调用cmd回流命令等学习，依赖文件lctrl.java和vv.fxml(由SceneBuiler创建)
 */
public class BMedia extends Application {


    //定义公共变量
    // 单声道转多声道命令：F:\ffmpeg\bin\ffmpeg.exe-i e:\a.wav -ac 1 e:\output.wav
    //媒体处理，将一个比较大的mp4文件分割成多个t3-m3u8视频片段，利于网页或APP在线播放不卡顿。节省服务器带宽和提高用户播放流畅度体验。
    public static String ffmpeg="F:\\ffmpeg\\bin\\ffmpeg.exe";
    public static String mpdir="E:\\mp4";
    public static ArrayList<String> list = new ArrayList<String>();
    //main入口
    public static void main(String[] args) throws Exception{
        //System.out.println(c.getClass().toString());
        //启动初始化
        System.out.println("开始运行媒体转化程序...");
        File dir= new File(mpdir);
        if(!dir.isDirectory()){
            dir.mkdir();
        }

        //获取待处理的MP4列表
        File[] childrenFiles=dir.listFiles();

        for (File childFile : childrenFiles) {
            if (childFile.isFile()) {
                if(childFile.getAbsolutePath().endsWith(".mp4")){
                    list.add(childFile.getAbsolutePath());
                }}}


        //显示窗口
        launch(args);
    }

    //主窗体
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader root=new FXMLLoader(getClass().getResource("BMedia.fxml"));

        Scene scene=new Scene(root.load());
        primaryStage.setTitle("Windows应用系统界面(媒体处理窗体)");
        primaryStage.setScene(scene);
        primaryStage.setOnShown((event)->{System.out.println("Windows应用系统界面启动完成啦!");});
        primaryStage.show();

    }


}
