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
	//�˿�ɨ��Ԫ��
	private TextField tfIP = new TextField();//IP��ַ��(���ɱ༭)
	private TextField tfBegin = new TextField();//��ʼ�˿ڿ�
	private TextField tfEnd = new TextField();//�����˿ڿ�
	private Button btScan = new Button("ɨ��");//ɨ�谴ť
	//�ı���Ϣ��ʾ�������ɱ༭��
	private TextArea text = new TextArea();
	//����ת��Ԫ��
	private TextField tfAddress = new TextField();
	private Button btTranlaste = new Button("ת��");
	//��ʱɨ���趨Ԫ��
	private TextField tfTime = new TextField("0");
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		//���ڶ˿���������IP��ת�������
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
		paneForIP.add(new Label("������ַ��"), 3, 1);
		paneForIP.add(tfAddress, 3, 2);
		paneForIP.add(btTranlaste, 3, 3);
		
		paneForIP.setAlignment(Pos.CENTER_LEFT);//��������е����ݿ���
		tfIP.setEditable(false);//����IP�򲻿ɱ༭
		tfBegin.setAlignment(Pos.BOTTOM_RIGHT);//�����ı��������ݿ���
		tfEnd.setAlignment(Pos.BOTTOM_RIGHT);
		tfAddress.setAlignment(Pos.BOTTOM_RIGHT);
		paneForIP.setHalignment(btScan, HPos.RIGHT);//���ø�Ԫ����ˮƽ������
		paneForIP.setHalignment(btTranlaste, HPos.RIGHT);
		
		btScan.setOnAction(e -> connect());//��������¼�������connect
		btTranlaste.setOnAction(e -> tranlaste());//��������¼�������translate
		
		//������Ϣ��ʾ�����
		text.setWrapText(true);
		text.setEditable(false);//�����ı����򲻿ɱ༭
		text.setPrefSize(490, 125);//�����ı������Ϊ490����Ϊ125
		Pane paneForText = new Pane();
		paneForText.getChildren().add(text);

		//������ʱɨ���趨�����
		tfTime.setAlignment(Pos.BOTTOM_RIGHT);
		
		HBox hbTime = new HBox();
		hbTime.getChildren().add(new Label("�˿�������ʱ��"));
		hbTime.getChildren().add(tfTime);
		hbTime.getChildren().add(new Label("(��λΪ����)"));
		
		//�����
		BorderPane pane = new BorderPane();
		pane.setTop(paneForIP);
		pane.setCenter(paneForText);
		pane.setBottom(hbTime);
	
		Scene scene = new Scene(pane,500,300);
		primaryStage.setTitle("�˿�ɨ�蹤��");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//������Ӱ�ť
	private void connect() {
		if(!ping(tfIP.getText(),5,5000)) {
			text.setText("���������ڻ����з���ǽ��");
		}else {
			String str = new String("��ʱ:");
			str = setStr(str);//�����ı�����ʾ������
			text.setText(str);	
		}	
	}
	//������ת��ΪIP��ַ
	private void tranlaste() {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(tfAddress.getText());
		}catch(UnknownHostException e) {
			e.printStackTrace();
			text.setText("ת��ʧ�ܣ���������ȷ��������");
		}
		tfIP.setText(address.getHostAddress());
	}
	//���ö˿��Ƿ������
	private boolean isConnect(int currPort) {
		String host = tfIP.getText();
		try {
			InetAddress address = InetAddress.getByName(host);
			//ת������
		}catch(UnknownHostException e) {
			System.out.println("�޷��ҵ� "+ host);
			return false;
		}
		try {
			Socket socket = new Socket();        //��������
			socket.connect(new InetSocketAddress(host, currPort), Integer.parseInt(tfTime.getText()));
			socket.close();        //�ر�����
			return true;
		}catch(IOException e) {
			return false;
		}
	}
	//�����ı�����ʾ������
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
	//ping���ܵ�ʵ��
    public static boolean ping(String ipAddress, int pingTimes, int timeOut) {  
        BufferedReader in = null;  
        Runtime r = Runtime.getRuntime();  // ��Ҫִ�е�ping����,��������windows��ʽ������  
        String pingCommand = "ping " + ipAddress + " -n " + pingTimes    + " -w " + timeOut;  
        try {   // ִ�������ȡ���  
            System.out.println(pingCommand);   
            Process p = r.exec(pingCommand);   
            if (p == null) {    
                return false;   
            }
            // ���м�����,�������Ƴ���=23ms TTL=62�����Ĵ���  
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));               
            int connectedCount = 0;   
            String line = null;   
            while ((line = in.readLine()) != null) {    
                connectedCount += getCheckResult(line);   
            }   // �����������=23ms TTL=62����������,���ֵĴ���=���Դ����򷵻���  
            return connectedCount == pingTimes;  
        } catch (Exception ex) {   
            ex.printStackTrace();   // �����쳣�򷵻ؼ�  
            return false;  
        } finally {   
            try {    
                in.close();   
            } catch (IOException e) {    
                e.printStackTrace();   
            }  
        }
    }
    //��line����=18ms TTL=16����,˵���Ѿ�pingͨ,����1,��t����0.
    private static int getCheckResult(String line) {  // System.out.println("����̨����Ľ��Ϊ:"+line);  
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",    Pattern.CASE_INSENSITIVE);  
        Matcher matcher = pattern.matcher(line);  
        while (matcher.find()) {
            return 1;
        }
        return 0; 
    }
}
