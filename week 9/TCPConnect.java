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
	//�˿�ɨ��Ԫ��
	private TextField tfStartIP = new TextField();//��ʼIP��ַ��
	private TextField tfEndIP = new TextField();//����IP��ַ��
	private TextField tfBegin = new TextField();//��ʼ�˿ڿ�
	private TextField tfEnd = new TextField();//�����˿ڿ�
	private Button btScan = new Button("ɨ��");//ɨ�谴ť
	//�ı���Ϣ��ʾ�������ɱ༭��
	private TextArea text = new TextArea();
	//�˳���ť
	private Button btLogout = new Button("�˳�");
	//��ʱɨ���趨Ԫ��
	private TextField tfTime = new TextField("0");
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		//���ڶ˿���������IP��ת�������
		GridPane paneForIP = new GridPane();
		paneForIP.setHgap(5);
		paneForIP.setVgap(5);
		paneForIP.add(new Label("��ʼIP��ַ��"), 0, 0);
		paneForIP.add(tfStartIP, 1, 0);
		paneForIP.add(new Label("����IP��ַ��"), 0, 1);
		paneForIP.add(tfEndIP, 1, 1);
		paneForIP.add(new Label("��ʼ�˿ڣ�"), 2, 0);
		paneForIP.add(tfBegin, 3, 0);
		paneForIP.add(new Label("�����˿ڣ�"), 2, 1);
		paneForIP.add(tfEnd, 3, 1);
		paneForIP.add(btScan, 4, 0);
		paneForIP.add(btLogout, 4, 1);
		
		paneForIP.setAlignment(Pos.CENTER);//��������е����ݿ���
		tfBegin.setAlignment(Pos.BOTTOM_RIGHT);//�����ı��������ݿ���
		tfEnd.setAlignment(Pos.BOTTOM_RIGHT);
		tfStartIP.setAlignment(Pos.BOTTOM_RIGHT);
		tfEndIP.setAlignment(Pos.BOTTOM_RIGHT);
		
		btScan.setOnAction(e -> connect());//��������¼�������connect
		btLogout.setOnAction(e -> logout());//��������¼�������logout
		
		//������Ϣ��ʾ�����
		text.setWrapText(true);
		text.setEditable(false);//�����ı����򲻿ɱ༭
		text.setPrefSize(650, 200);//�����ı������Ϊ490����Ϊ125
		HBox paneForText = new HBox();
		paneForText.getChildren().add(text);
		paneForText.setAlignment(Pos.CENTER);

		//������ʱɨ���趨�����
		tfTime.setAlignment(Pos.BOTTOM_RIGHT);
		
		HBox hbTime = new HBox();
		hbTime.getChildren().add(new Label("�˿�������ʱ��"));
		hbTime.getChildren().add(tfTime);
		hbTime.getChildren().add(new Label("(��λΪ����)"));
		hbTime.setAlignment(Pos.CENTER);
		
		//�����
		BorderPane pane = new BorderPane();
		pane.setTop(paneForIP);
		pane.setCenter(paneForText);
		pane.setBottom(hbTime);
	
		Scene scene = new Scene(pane,700,300);
		primaryStage.setTitle("�˿�ɨ�蹤��");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//������Ӱ�ť
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
	//���Ӹ�IP�µ�����ָ���˵�
	private void connectPort(String currIP) {
		int threadNumber = 5;
		long startTime = System.currentTimeMillis(); //��ȡ��ʼʱ��
		ExecutorService threadPool = Executors.newFixedThreadPool(threadNumber);
		for (int i = 0; i < threadNumber; i++) {
			ScanThread scanThread = new ScanThread(currIP, 
				Integer.parseInt(tfBegin.getText().trim()), 
				Integer.parseInt(tfEnd.getText().trim()),
				threadNumber, i, Integer.parseInt(tfTime.getText().trim()));
			threadPool.execute(scanThread);
		}
		threadPool.shutdown();
		// ÿ���в鿴һ���Ƿ��Ѿ�ɨ�����
		try {
			while (!threadPool.isTerminated()) {
				Thread.sleep(1000);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis(); //��ȡ����ʱ��
		//������ʱ
		setStr("��ʱ" + (endTime - startTime) + "ms");
	}
	//�����ı�����ʾ������
	private void setStr(String add) {
		str = str + "\n" + add;
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
    private static int getCheckResult(String line) {  
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)",Pattern.CASE_INSENSITIVE);  
        Matcher matcher = pattern.matcher(line);  
        while (matcher.find()) {
            return 1;
        }
        return 0; 
    }
    //�˳�
  	private void logout() {
  		System.exit(0);
  	}
}

class ScanThread implements Runnable{
	private String IP;
	private int startPort,endPort;//��ʼ�˿ڣ������˿�
	private int threadNumber, serial, timeout; // �߳��������ǵڼ����̣߳���ʱʱ��
	private String openPort = "";//���ŵĶ˿���Ϣ
	
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
				//���˿��Ƿ��ںϷ���Χ1��65535
				System.out.printf("�˿ڷ�Χ������1��65535����!");
			}else if(startPort>endPort){//�Ƚ���ʼ�˿ں���ֹ�˿�
				System.out.println("�˿���������! ��ʼ�˿ڱ���С����ֹ�˿�");
			}else {
				Socket socket = new Socket();
				SocketAddress socketAddress;
				for (currPort = startPort + serial;
						currPort <= endPort;currPort += threadNumber) {
					try {        
						socket.connect(new InetSocketAddress(IP, currPort),
								timeout);//��������
						socket.close();//�ر�����
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