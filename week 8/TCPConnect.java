import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	//端口扫描元件
	private TextField tfIP = new TextField();//IP地址框(不可编辑)
	private TextField tfBegin = new TextField();//开始端口框
	private TextField tfEnd = new TextField();//结束端口框
	private Button btScan = new Button("扫描");//扫描按钮
	//文本信息显示区（不可编辑）
	private TextArea text = new TextArea();
	//域名转换元件
	private TextField tfAddress = new TextField();
	private Button btTranlaste = new Button("转换");
	//延时扫描设定元件
	private TextField tfTime = new TextField("0");
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		//关于端口与域名、IP间转换的面板
		GridPane paneForIP = new GridPane();
		paneForIP.setHgap(5);
		paneForIP.setVgap(5);
		paneForIP.add(new Label("IP地址："), 0, 0);
		paneForIP.add(tfIP, 1, 0);
		paneForIP.add(new Label("开始端口："), 0, 1);
		paneForIP.add(tfBegin, 1, 1);
		paneForIP.add(new Label("结束端口："), 0, 2);
		paneForIP.add(tfEnd, 1, 2);
		paneForIP.add(btScan, 1, 3);
		paneForIP.add(new Label("域名地址："), 3, 1);
		paneForIP.add(tfAddress, 3, 2);
		paneForIP.add(btTranlaste, 3, 3);
		
		paneForIP.setAlignment(Pos.CENTER_LEFT);//设置面板中的内容靠左
		tfIP.setEditable(false);//设置IP框不可编辑
		tfBegin.setAlignment(Pos.BOTTOM_RIGHT);//设置文本框中内容靠右
		tfEnd.setAlignment(Pos.BOTTOM_RIGHT);
		tfAddress.setAlignment(Pos.BOTTOM_RIGHT);
		paneForIP.setHalignment(btScan, HPos.RIGHT);//设置该元件在水平方向靠右
		paneForIP.setHalignment(btTranlaste, HPos.RIGHT);
		
		btScan.setOnAction(e -> connect());//点击触发事件，调用connect
		btTranlaste.setOnAction(e -> tranlaste());//点击触发事件，调用translate
		
		//关于信息显示的面板
		text.setWrapText(true);
		text.setEditable(false);//设置文本区域不可编辑
		text.setPrefSize(490, 125);//设置文本区域宽为490，高为125
		Pane paneForText = new Pane();
		paneForText.getChildren().add(text);

		//关于延时扫描设定的面板
		tfTime.setAlignment(Pos.BOTTOM_RIGHT);
		
		HBox hbTime = new HBox();
		hbTime.getChildren().add(new Label("端口连接延时："));
		hbTime.getChildren().add(tfTime);
		hbTime.getChildren().add(new Label("(单位为毫秒)"));
		
		//总面板
		BorderPane pane = new BorderPane();
		pane.setTop(paneForIP);
		pane.setCenter(paneForText);
		pane.setBottom(hbTime);
	
		Scene scene = new Scene(pane,500,300);
		primaryStage.setTitle("端口扫描工具");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//点击连接按钮
	private void connect() {
		if(!ping(tfIP.getText(),5,5000)) {
			text.setText("主机不存在或者有防火墙！");
		}else {
			String str = new String("耗时:");
			str = setStr(str);//设置文本框显示的内容
			text.setText(str);	
		}	
	}
	//将域名转换为IP地址
	private void tranlaste() {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(tfAddress.getText());
		}catch(UnknownHostException e) {
			e.printStackTrace();
			text.setText("转换失败！请输入正确的域名！");
		}
		tfIP.setText(address.getHostAddress());
	}
	//检测该端口是否可连接
	private boolean isConnect(int currPort) {
		String host = tfIP.getText();
		try {
			InetAddress address = InetAddress.getByName(host);
			//转换类型
		}catch(UnknownHostException e) {
			System.out.println("无法找到 "+ host);
			return false;
		}
		try {
			Socket socket = new Socket();        //建立连接
			socket.connect(new InetSocketAddress(host, currPort), Integer.parseInt(tfTime.getText()));
			socket.close();        //关闭连接
			return true;
		}catch(IOException e) {
			return false;
		}
	}
	//设置文本框显示的内容
	private String setStr(String str) {
		int startPort = 0,endPort = 0,currPort = 0;
		long startTime = 0;
		String port = new String();
		try {
			startPort = Integer.parseInt(tfBegin.getText());//获得起始端口号
			endPort = Integer.parseInt(tfEnd.getText());//获得终止端口号
			if(startPort<1||startPort>65535||endPort<1||endPort>65535){
				//检查端口是否在合法范围1～65535
				System.out.printf("端口范围必须在1～65535以内!");
				return null;
			}else if(startPort>endPort){ //比较起始端口和终止端口
				System.out.println("端口输入有误! 起始端口必须小于终止端口");
				return null;
			}
		
			startTime = System.currentTimeMillis();   //获取开始时间
			for(currPort = startPort;currPort <= endPort;currPort++) {
				//逐个判断是否可连接
				if(isConnect(currPort)) {
					port += "\n[" + currPort + "] is open";
				}
			}
		}catch(NumberFormatException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis(); //获取结束时间
		//计算用时
		str += (endTime - startTime + "ms");
		return str + port;
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
    private static int getCheckResult(String line) {  // System.out.println("控制台输出的结果为:"+line);  
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",    Pattern.CASE_INSENSITIVE);  
        Matcher matcher = pattern.matcher(line);  
        while (matcher.find()) {
            return 1;
        }
        return 0; 
    }
}
