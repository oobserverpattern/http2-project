package wu.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;


public class MyPropertiesPro {
	private static Properties properties;

	

	public MyPropertiesPro(String filePath) {
		try {
			// 新建一个拥有 config.properties 相对项目路径的 File 对象
			File propertiesFile = new File("resources/config.properties");
			// 初始化输入流 reader，利用相对项目路径生成的 File 来获取其绝对路径，并且以 utf-8 形式读取 properties 配置文件
			InputStreamReader propertiesReader = new InputStreamReader(
					new FileInputStream(propertiesFile.getAbsolutePath()), "UTF-8");
			// 通过文件流读取配置文件
			properties = new Properties();
			properties.load(propertiesReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

}

	
		

