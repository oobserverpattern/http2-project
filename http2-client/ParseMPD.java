package wu.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ParseMPD {
	//数据成员
	private TreeMap<String, String> url_list_L0;
	private TreeMap<String, String> url_list_L1;
	private TreeMap<String, String> url_list_L2;
    private String separ = "=";
	//读取文件的相对路径,改到配置文件中
    //第一步视频
//	static String filePath0 = "resources/BBB_L0_MPD.txt";
//	static String filePath1 = "resources/BBB_L1_MPD.txt";
//	static String filePath2 = "resources/BBB_L2_MPD.txt";
//
//    //第二部视频
//	static String filePath0 = "resources/ED_L0_MPD.txt";
//	static String filePath1 = "resources/ED_L1_MPD.txt";
//	static String filePath2 = "resources/ED_L2_MPD.txt";
	
    

  //第三部视频
	static String filePath0 = "resources/TOS_L0_MPD.txt";
	static String filePath1 = "resources/TOS_L1_MPD.txt";
	static String filePath2 = "resources/TOS_L2_MPD.txt";
	

	
	public TreeMap<String, String> getUrl_list_L0() {
		return url_list_L0;
	}
	public void setUrl_list_L0(TreeMap<String, String> url_list_L0) {
		this.url_list_L0 = url_list_L0;
	}
	public TreeMap<String, String> getUrl_list_L1() {
		return url_list_L1;
	}
	public void setUrl_list_L1(TreeMap<String, String> url_list_L1) {
		this.url_list_L1 = url_list_L1;
	}
	public TreeMap<String, String> getUrl_list_L2() {
		return url_list_L2;
	}
	public void setUrl_list_L2(TreeMap<String, String> url_list_L2) {
		this.url_list_L2 = url_list_L2;
	}
	
	//无参构造方法
	public ParseMPD() {
		
		File file0 = new File(filePath0);
		File file1 = new File(filePath1);
		File file2 = new File(filePath2);
		if (!file0.exists()) {
			System.out.println("文件不存在");
			try {
				file0.createNewFile();
			} catch (IOException e) {
				System.out.println("路径:" + filePath0 + "创建失败");
				e.printStackTrace();
			}
		}
		if (!file1.exists()) {
			System.out.println("文件不存在");
			try {
				file1.createNewFile();
			} catch (IOException e) {
				System.out.println("路径:" + filePath1 + "创建失败");
				e.printStackTrace();
			}
		}
		if (!file2.exists()) {
			System.out.println("文件不存在");
			try {
				file2.createNewFile();
			} catch (IOException e) {
				System.out.println("路径:" + filePath2 + "创建失败");
				e.printStackTrace();
			}
		}
	}	
	//成员方法，使用时调用
	//解析mpd文件
	//从本地文件将url读入TreeMap<Integer, String>中
	//key是segment ID，value是
	public void parseMPD()
	{
		parseMPD_L0();
		parseMPD_L1();
		parseMPD_L2();		
	}
	//解析l0
	public TreeMap<String, String> parseMPD_L0() {
		
		url_list_L0 = new TreeMap<String, String>();
		File file0 = new File(filePath0);
		BufferedReader reader0 = null;
		try {
			//System.out.println("L0------以行为单位读取文件内容，一次读一整行：");
			reader0 = new BufferedReader(new FileReader(file0));
			String tempString0 = null;
			int line = 1;
			//0层
			while ((tempString0 = reader0.readLine()) != null) {
				//System.out.println("line " + line + ": " + tempString0);
				if (!tempString0.startsWith("#")) {
					String[] strArray = tempString0.split("=");
					url_list_L0.put(strArray[0], strArray[1]);
					
				}
				line++;
			}
			reader0.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader0 != null) {
				try {
					reader0.close();
				} catch (IOException e1) {
				}
			}
		}
		for (Map.Entry entry : url_list_L0.entrySet()) {
			//System.out.println(entry.getKey() + "=" + entry.getValue());
		}		
		return url_list_L0;
	}
	//解析l1
	public TreeMap<String, String> parseMPD_L1() {
		
		url_list_L1 = new TreeMap<String, String>();
		File file1 = new File(filePath1);
		BufferedReader reader1 = null;
		try {
			//System.out.println("L1------以行为单位读取文件内容，一次读一整行：");
			reader1 = new BufferedReader(new FileReader(file1));
			String tempString1 = null;
			int line = 1;
			//0层
			while ((tempString1 = reader1.readLine()) != null) {
				//System.out.println("line " + line + ": " + tempString1);
				if (!tempString1.startsWith("#")) {
					String[] strArray = tempString1.split("=");
					url_list_L1.put(strArray[0], strArray[1]);
				}
				line++;
			}
			reader1.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader1 != null) {
				try {
					reader1.close();
				} catch (IOException e1) {
				}
			}
		}
		for (Map.Entry entry : url_list_L1.entrySet()) {
			//System.out.println(entry.getKey() + "=" + entry.getValue());
		}		
		return url_list_L1;
	}
	//解析l2
	public TreeMap<String, String> parseMPD_L2() {
		
		url_list_L2 = new TreeMap<String, String>();
		File file2 = new File(filePath2);
		BufferedReader reader2 = null;
		try {
			//System.out.println("L2------以行为单位读取文件内容，一次读一整行：");
			reader2 = new BufferedReader(new FileReader(file2));
			String tempString0 = null;
			int line = 1;
			//0层
			while ((tempString0 = reader2.readLine()) != null) {
				//System.out.println("line " + line + ": " + tempString0);
				if (!tempString0.startsWith("#")) {
					String[] strArray = tempString0.split("=");
					url_list_L2.put(strArray[0], strArray[1]);
				}
				line++;
			}
			reader2.close();			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader2 != null) {
				try {
					reader2.close();
				} catch (IOException e1) {
				}
			}
		}
		for (Map.Entry entry : url_list_L2.entrySet()) {
			//System.out.println(entry.getKey() + "=" + entry.getValue());
		}		
		return url_list_L2;
	}
	
	public static void main(String[] args)
	{
		ParseMPD parse = new ParseMPD();
		parse.parseMPD();		
	}
}

