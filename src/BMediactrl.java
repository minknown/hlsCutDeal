import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 现在的问题：
 * 1：列表启动未就绪（启动时未操作其他组件）
 * 2：运行卡住了,且无法获得CMD结果并加载到进度条来。
 * 3：退出按钮点击无识别。
 */

//启动执行的事情：实现这个接口+implements Initializable
public class BMediactrl implements Initializable {
    @FXML
    private TextArea res;
    @FXML
    private ProgressBar pro;
    @FXML
    private ListView wait;
    @FXML
    private Button btncopy ;
    //启动就绪初始化函数

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //列表框处理-列表框组件显示main.list
        wait.getItems().clear();
        wait.setEditable(true);
        for(String temp: BMedia.list){

            File thetemp=new File(temp);
            wait.getItems().add(thetemp.getName());

        }
        btncopy.setText("CopyT");
        pro.setVisible(true);
        pro.setProgress(0);
    }

    //复制按钮的绑定事件
    @FXML
    public void btncopy(Event event) throws Exception{


        //操作剪辑版：https://www.jianshu.com/p/060a0f42bf36
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = new StringSelection(res.getText());
        clipboard.setContents(trans, null);
        JOptionPane dhk=new JOptionPane();
        dhk.showMessageDialog(null,"将处理结果复制到剪辑版成功。","Copy",JOptionPane.WARNING_MESSAGE);
    }

    //开始mp4->m3u8和退出程序的绑定事件
    @FXML
    public void btn(Event event) throws Exception{


        //String rr=RuncmdSim(main.ffmpeg+" -i E:\\mp4\\demo.mp4");
        //System.out.println(main.ffmpeg+" -i E:\\mp4\\demo.mp4");
        //System.out.println(rr);
        //System.exit(6);
        //-
        EventType cc=event.getEventType();
        EventTarget dd=event.getTarget();
        Button button=(Button)dd;
        System.out.println("[组件事件响应]"+cc+":"+button.getText());
        if(button.getText().equals("退出程序")){System.exit(0);}
        //输入框文本框显示。。。
        res.setText("开始处理...");
        res.setEditable(false);
        //进程结束。。。
        RuncmdSim("cmd.exe /c taskkill /f /t /im ffmpeg.exe");
        Thread.sleep(1000);


        //列表框处理
        for(String temp: BMedia.list){
            File thetemp=new File(temp);
            if(thetemp.length()>1000){
                //编辑框文本框显示内容
                res.setText(res.getText()+"\r\n"+"开始处理"+thetemp.getName()+"...");
                //创建存放处理结果的文件夹(demo目录)
                File newdir= new File(BMedia.mpdir+"\\"+thetemp.getName().replaceAll (".mp4",""));
                if(!newdir.isDirectory()){newdir.mkdir();}
                //获得CMD命令行
                String cmd=Getcmd(temp,newdir+"\\"+"ouput.mp4");
                System.out.println("获得的CMD命令为:"+cmd);
                // 执行CMD命令行
                // RuncmdSim():简单执行，RuncmdFuza()：复杂执行，可能有多线程功能（该方法由尽力哥写的，技术支持QQ411182157）。
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RuncmdFuza(cmd);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();

            }
        }

        System.out.println("[按钮事件运行结束]");
    }

    //方法函数：时间转化,将字符串"00:02:01"转为121秒
    public int Trantime(String timetext){
        int videotime=0;
        String[] splittemp=timetext.split(":");
        if(splittemp.length==3){
            videotime=Integer.parseInt(splittemp[0])*3600+Integer.parseInt(splittemp[1])*60+Double.valueOf(splittemp[2]).intValue();
        }
        return videotime;
    }
    //从字符串中找到两字符串中间的字符串，如“AABBCC”，传入AA,CC，得到BB
    public String getmid(String fulltest,String textStart,String textEnd){
        Pattern pattern = Pattern.compile(textStart+"(.*?)"+textEnd);
        Matcher matcher = pattern.matcher(fulltest.trim());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }else{
            return "";
        }
    }

    //方法函数：得到cmd命令
    public String Getcmd(String inputname,String outputname){
        String ms="30";
        String temp= BMedia.ffmpeg+" -i "+inputname+" -c:v h264 -c:a aac -strict -2 -f hls -hls_time "+ms+" -hls_list_size 0 "+outputname;
        return temp;
    }


    //方法函数：RuncmdSim:执行cmd命令（简单型命令）
    public String RuncmdSim(String command) throws Exception{
        if(command==null){command="cmd.exe /c dir d:";}
        //java.lang.Runtime.getRuntime().exec(command);
        Process process = java.lang.Runtime.getRuntime().exec(command);
        InputStream inputStream = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"gb2312"));
        String line = null;
        String Lineall=null;
        while((line = br.readLine()) != null) {
            Lineall=Lineall+"\r\n"+line;
        }
        return Lineall;
    }


    //方法函数：RuncmdFuza:执行cmd命令（复杂型命令），
    //由ThreadPoolExecutor executor、RuncmdFuza方法、clearStream方法构成。
    private static ThreadPoolExecutor executor;
    static {
        //根据实际情况创建线程池
        executor = new ThreadPoolExecutor(10,
                20,
                5,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                new ThreadPoolExecutor.AbortPolicy());
    }
    public  boolean RuncmdFuza(String cmd) throws IOException, InterruptedException {


        Process process =java.lang.Runtime.getRuntime().exec(cmd);

        //消费正常日志
        clearStream(process.getInputStream());
        //消费错误日志
        clearStream(process.getErrorStream());

        //i为返回值，判断是否执行成功
        int i = process.waitFor();
        if (i != 0) {

            return false;
        }
        return true;

    }

    private  void clearStream(InputStream stream) {
        //处理buffer的线程
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean finish=false;
                String line = null;
                String alltimestr=null;
                String nowtimestr=null;
                int alltime=0;
                int nowtime=0;
                double jd=0;

                try (BufferedReader in = new BufferedReader(new InputStreamReader(stream));) {

                    while ((line = in.readLine()) != null) {

                        //line是cmd输出的每行字符串，从格式中提取总视频时长秒数至alltime变量，当前处理的秒数到nowtime变量
                        if(alltime>0){
                            //0不能作为除数,精确度变化为保留2位小数。
                            jd=(double)((double)nowtime/(double)alltime);
                            jd=new BigDecimal(jd).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                            //设置进度条，jd=0.5进度条则为一半。
                            pro.setProgress(jd);
                            if (jd > 0.95 && finish==false)  {
                                finish=true;
                                res.setText(res.getText()+"\r\n"+"处理结束。");
                            }

                        }

                        //提取过程（符合总秒数的cmd行）
                        if(line.trim().startsWith("Duration")){
                            alltimestr=getmid(line,"Duration: ",",");
                            alltime=Trantime(alltimestr);
                            System.out.println("****"+"[总长进度:"+alltime+"]"+line);
                        }
                        //提取过程（符合当前处理秒数的cmd行）
                        if(line.indexOf("time=")!=-1 && line.indexOf("bitrate=")!=-1) {
                            nowtimestr=getmid(line,"time="," bit");
                            nowtime=Trantime(nowtimestr);
                            System.out.println("%%%%"+"[当前进度:"+nowtime+"]"+line+"(jd进度率："+jd+")");
                        }
                        //其他不相干的行，可以 System.out.println（“----”+line）输出;

                    }

                } catch (IOException e) {
                    //logger.error("exec error : {}", e);
                }
            }
        });
    }

}
