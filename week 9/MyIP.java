public class MyIP {
	private int part1,part2,part3,part4;
	private String ip[];
	public MyIP(String address) {
		String ip[] = address.split("\\.");
		this.part1 = Integer.parseInt(ip[0]);
		this.part2 = Integer.parseInt(ip[1]);
		this.part3 = Integer.parseInt(ip[2]);
		this.part4 = Integer.parseInt(ip[3]);
	}
	public static String getIP(int part1,int part2,
			int part3,int part4 ) {
		return "" + part1 + "." + 
				part2 + "." + part3 + "." + part4; 
	}
	public int getPart1() {
		return part1;
	}
	public int getPart2() {
		return part2;
	}
	public int getPart3() {
		return part3;
	}
	public int getPart4() {
		return part4;
	}
}
