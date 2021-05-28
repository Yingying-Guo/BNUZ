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
	private Button btScan = new Button("ɨ��");
	private TextArea text = new TextArea();
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		//Create UI
		GridPane paneForIP = new GridPane();
		paneForIP.setHgap(5);
		paneForIP.setVgap(5);
		paneForIP.add(new Label("IP��ַ��"), 0, 0);
		paneForIP.add(tfIP, 1, 0);
		paneForIP.add(new Label("��ʼ�˿ڣ�"), 0, 1);
		paneForIP.add(tfBegin, 1, 1);
		paneForIP.add(new Label("�����˿ڣ�"), 0, 2);
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
		primaryStage.setTitle("�˿�ɨ�蹤��");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private void connect() {
		//Get value from text fields
		String str = new String("��ʱ:");
		str = setStr(str);
		text.setText(str);
		
	}
	
	private boolean isConnect(int currPort) {
		String host = tfIP.getText();
		try{
			InetAddress address = InetAddress.getByName(host);
			//ת������
		}catch(UnknownHostException e){
			System.out.println("�޷��ҵ� "+ host);
			return false;
		}
		try {
			Socket socket = new Socket(host,currPort);        //��������
			socket.close();        //�ر�����
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
			startPort = Integer.parseInt(tfBegin.getText());//�����ʼ�˿ں�
			endPort = Integer.parseInt(tfEnd.getText());//�����ֹ�˿ں�
			if(startPort<1||startPort>65535||endPort<1||endPort>65535){
				//���˿��Ƿ��ںϷ���Χ1��65535
				System.out.printf("�˿ڷ�Χ������1��65535����!");
				return null;
			}else if(startPort>endPort){ //�Ƚ���ʼ�˿ں���ֹ�˿�
				System.out.println("�˿���������! ��ʼ�˿ڱ���С����ֹ�˿�");
				return null;
			}
		
			startTime = System.currentTimeMillis();   //��ȡ��ʼʱ��
			for(currPort = startPort;currPort <= endPort;currPort++) {
				//����ж��Ƿ������
				if(isConnect(currPort)) {
					port += "\n[" + currPort + "] is open";
				}
			}
		}catch(NumberFormatException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis(); //��ȡ����ʱ��
		//������ʱ
		str += (endTime - startTime + "ms");
		return str + port;
	}
}
