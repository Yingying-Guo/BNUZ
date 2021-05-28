import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TCPConnect extends Application {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	
	private TextField tfIP = new TextField();
	private TextField tfBegin = new TextField();
	private TextField tfEnd = new TextField();
	private Button btScan = new Button("扫描");
	private TextArea text = new TextArea();
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		//Create UI
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
		
		//Set properties for UI
		paneForIP.setAlignment(Pos.CENTER_LEFT);
		tfIP.setAlignment(Pos.BOTTOM_RIGHT);
		tfBegin.setAlignment(Pos.BOTTOM_RIGHT);
		tfEnd.setAlignment(Pos.BOTTOM_RIGHT);
		paneForIP.setHalignment(btScan, HPos.RIGHT);
		
		btScan.setOnAction(e -> connect());
		
		text.setWrapText(true);
		text.setEditable(false);
		Pane paneForText = new Pane();
		paneForText.getChildren().add(text);
		
		BorderPane pane = new BorderPane();
		pane.setTop(paneForIP);
		pane.setCenter(paneForText);
	
		Scene scene = new Scene(pane,300,250);
		primaryStage.setTitle("端口扫描工具");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private void connect() {
		//Get value from text fields
		String str = new String("耗时:");
		str = setStr(str);
		text.setText(str);
		
	}
	
	private boolean isConnect(int currPort) {
		String host = tfIP.getText();
		try{
			InetAddress address = InetAddress.getByName(host);
			//转换类型
		}catch(UnknownHostException e){
			System.out.println("无法找到 "+ host);
			return false;
		}
		try {
			Socket socket = new Socket(host,currPort);        //建立连接
			socket.close();        //关闭连接
			return true;
		}catch(IOException e) {
			return false;
		}
	}
	
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
}
