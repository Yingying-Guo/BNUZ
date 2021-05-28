import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TCPConnect extends Application {
	
	static String str = "";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	//端口扫描元件
	private TextField tfStartIP = new TextField();//开始IP地址框
	private TextField tfEndIP = new TextField();//结束IP地址框
	private TextField tfBegin = new TextField();//开始端口框
	private TextField tfEnd = new TextField();//结束端口框
	private Button btScan = new Button("扫描");//扫描按钮
	//文本信息显示区（不可编辑）
	private TextArea text = new TextArea();
	//退出按钮
	private Button btLogout = new Button("退出");
	//延时扫描设定元件
	private TextField tfTime = new TextField("0");
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		//关于端口与域名、IP间转换的面板
		GridPane paneForIP = new GridPane();
		paneForIP.setHgap(5);
		paneForIP.setVgap(5);
		paneForIP.add(new Label("开始IP地址："), 0, 0);
		paneForIP.add(tfStartIP, 1, 0);
		paneForIP.add(new Label("结束IP地址："), 0, 1);
		paneForIP.add(tfEndIP, 1, 1);
		paneForIP.add(new Label("开始端口："), 2, 0);
		paneForIP.add(tfBegin, 3, 0);
		paneForIP.add(new Label("结束端口："), 2, 1);
		paneForIP.add(tfEnd, 3, 1);
		paneForIP.add(btScan, 4, 0);
		paneForIP.add(btLogout, 4, 1);
		
		paneForIP.setAlignment(Pos.CENTER);//设置面板中的内容靠左
		tfBegin.setAlignment(Pos.BOTTOM_RIGHT);//设置文本框中内容靠右
		tfEnd.setAlignment(Pos.BOTTOM_RIGHT);
		tfStartIP.setAlignment(Pos.BOTTOM_RIGHT);
		tfEndIP.setAlignment(Pos.BOTTOM_RIGHT);
		
		btScan.setOnAction(e -> connect());//点击触发事件，调用connect
		btLogout.setOnAction(e -> logout());//点击触发事件，调用logout
		
		//关于信息显示的面板
		text.setWrapText(true);
		text.setEditable(false);//设置文本区域不可编辑
		text.setPrefSize(650, 200);//设置文本区域宽为490，高为125
		HBox paneForText = new HBox();
		paneForText.getChildren().add(text);
		paneForText.setAlignment(Pos.CENTER);

		//关于延时扫描设定的面板
		tfTime.setAlignment(Pos.BOTTOM_RIGHT);
		
		HBox hbTime = new HBox();
		hbTime.getChildren().add(new Label("端口连接延时："));
		hbTime.getChildren().add(tfTime);
		hbTime.getChildren().add(new Label("(单位为毫秒)"));
		hbTime.setAlignment(Pos.CENTER);
		
		//总面板
		BorderPane pane = new BorderPane();
		pane.setTop(paneForIP);
		pane.setCenter(paneForText);
		pane.setBottom(hbTime);
	
		Scene scene = new Scene(pane,700,300);
		primaryStage.setTitle("端口扫描工具");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//点击连接按钮
	private void connect() {
		MyIP startIP = new MyIP(tfStartIP.getText().trim());
		MyIP endIP = new MyIP(tfEndIP.getText().trim());
		String currIP = null;
		for(int i = startIP.getPart1();i <= endIP.getPart1();i++)
			for(int j = startIP.getPart2();j <= endIP.getPart2();j++)
				for(int k = startIP.getPart3();k <= endIP.getPart3();k++)
					for(int l = startIP.getPart4();l <= endIP.getPart4();l++) {
						currIP = MyIP.getIP(i, j, k, l);
						if(ping(currIP,5,5000)) {
							setStr(currIP);
							System.out.println(str);
							connectPort(currIP);
						}
						text.setText(str);
					}
	}
	//连接该IP下的所有指定端点
	private void connectPort(String currIP) {
		int threadNumber = 5;
		long startTime = System.currentTimeMillis(); //获取开始时间
		ExecutorService threadPool = Executors.newFixedThreadPool(threadNumber);
		for (int i = 0; i < threadNumber; i++) {
			ScanThread scanThread = new ScanThread(currIP, 
				Integer.parseInt(tfBegin.getText().trim()), 
				Integer.parseInt(tfEnd.getText().trim()),
				threadNumber, i, Integer.parseInt(tfTime.getText().trim()));
			threadPool.execute(scanThread);
		}
		threadPool.shutdown();
		// 每秒中查看一次是否已经扫描结束
		try {
			while (!threadPool.isTerminated()) {
				Thread.sleep(1000);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis(); //获取结束时间
		//计算用时
		setStr("用时" + (endTime - startTime) + "ms");
	}
	//设置文本框显示的内容
	private void setStr(String add) {
		str = str + "\n" + add;
	}
	//ping功能的实现
    public static boolean ping(String ipAddress, int pingTimes, int timeOut) {  
        BufferedReader in = null;  
        Runtime r = Runtime.getRuntime();  // 将要执行的ping命令,此命令是windows格式的命令  
        String pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;  
        try {   // 执行命令并获取输出  
            System.out.println(pingCommand);   
            Process p = r.exec(pingCommand);   
            if (p == null) {    
                return false;   
            }
            // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数  
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));               
            int connectedCount = 0;   
            String line = null;   
            while ((line = in.readLine()) != null) {    
                connectedCount += getCheckResult(line);   
            }   // 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真  
            return connectedCount == pingTimes;  
        } catch (Exception ex) {   
            ex.printStackTrace();   // 出现异常则返回假  
            return false;  
        } finally {   
            try {    
                in.close();   
            } catch (IOException e) {    
                e.printStackTrace();   
            }  
        }
    }
    //若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
    private static int getCheckResult(String line) {  
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",Pattern.CASE_INSENSITIVE);  
        Matcher matcher = pattern.matcher(line);  
        while (matcher.find()) {
            return 1;
        }
        return 0; 
    }
    //退出
  	private void logout() {
  		System.exit(0);
  	}
}

class ScanThread implements Runnable{
	private String IP;
	private int startPort,endPort;//开始端口，结束端口
	private int threadNumber, serial, timeout; // 线程数，这是第几个线程，超时时间
	private String openPort = "";//开放的端口信息
	
	public ScanThread(String IP,int startPort,int endPort, 
			int threadNumber,int serial,int timeout) {
		this.IP = IP;
		this.startPort = startPort;
		this.endPort = endPort;
		this.threadNumber = threadNumber;
		this.serial = serial;
		this.timeout = timeout;
		this.openPort = "";
	}

	@Override
	public void run() {
		int currPort = 0;
		try {
			if(startPort<1||startPort>65535||endPort<1||endPort>65535){
				//检查端口是否在合法范围1～65535
				System.out.printf("端口范围必须在1～65535以内!");
			}else if(startPort>endPort){//比较起始端口和终止端口
				System.out.println("端口输入有误! 起始端口必须小于终止端口");
			}else {
				Socket socket = new Socket();
				SocketAddress socketAddress;
				for (currPort = startPort + serial;
						currPort <= endPort;currPort += threadNumber) {
					try {        
						socket.connect(new InetSocketAddress(IP, currPort),
								timeout);//建立连接
						socket.close();//关闭连接
						this.openPort += "\n[" + currPort + "] is open";
						TCPConnect.str += openPort;
					}catch(IOException e) {	
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
}