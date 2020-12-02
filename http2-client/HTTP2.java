package wu.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;



import wu.test.policy.Pair;
import wu.test.policy.SizePredictor;

public class HTTP2 {
	double lastIntervalTime = 0;
	double sumSize = 0;
	double avgBandWidth = 0;
	
	private ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<Path>>> promisesMap;

	public ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<Path>>> getPromisesMap() {
		return promisesMap;
	}

	public void setPromisesMap(ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<Path>>> promisesMap) {
		this.promisesMap = promisesMap;
	}

	Function<HttpRequest, HttpResponse.BodyHandler<Path>>
	// promiseHandler = (HttpRequest req) ->
	// HttpResponse.BodyHandlers.ofFile(Paths.get(req.uri().getPath()).getFileName());
	promiseHandler = (HttpRequest req) -> HttpResponse.BodyHandlers.ofFile(
			Path.of("/Users/wuyue/Desktop/TOS-II-360p", Paths.get(req.uri().getPath()).getFileName().toString()));

	// 标志push是否接收完毕
	public static boolean[] isEnd = { true };

	// 使用push发送http2请求
	public double[] Sendhttp2Request_push(String url, String filePath, String push_list)
			throws URISyntaxException, IOException, InterruptedException {
		promisesMap = new ConcurrentHashMap<>();
		// Function<HttpRequest, HttpResponse.BodyHandler<Path>> promiseHandler =
		// (HttpRequest req) ->
		// HttpResponse.BodyHandlers.ofFile(Paths.get(req.uri().getPath()).getFileName());
		
		// long file_size = 0;
		double[] temp = new double[3];

		System.setProperty("javax.net.ssl.trustStore",
				"/Library/Java/JavaVirtualMachines/jdk-11.0.3.jdk/Contents/Home/lib/security/cacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		
		// 初始化httpclient客户端
		HttpClient client = HttpClient.newBuilder().build();
      
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).version(HttpClient.Version.HTTP_2)
				.header("pushStrategy", "specified").header("promiseList", push_list).build();

		byte[] httpRequestSize = request.toString().getBytes();
		System.out.println("request size = "+httpRequestSize[0]);
//        //开始计时
//      	long time1 = System.currentTimeMillis(); 
//        //path.of()是下载文件保存的，并且只指定了request的保存路径
//      
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(Paths.get(filePath)), pushPromiseHandler())
//        
//        
//                //thenApply当第一个Future(描述一个异步运算的结果)运行结束，返回CompletableFuture<String>对象转换为CompleTableFuture<Quote>对象
//                .thenApply(HttpResponse::body)
//                //thenAccept接收CompletableFuture执行完毕后的返回值作为参数，不必等待那些还未返回的结果
//                //main source是response保存的文件
//                .thenAccept((b) -> System.out.println("\nMain resource:\n" + b))
//                //执行完毕后返回结果
//                //join()方法。主线程生成并启动了子线程，如果子线程中有大量的计算，主线程往往在子线程之前结束。
//                //如果主线程处理完其他事情以后需要用到子线程的处理结果，也就是主线程需要等待子线程执行完毕以后再结束
//                //这时就要用到join（）方法：等待该线程终止
//                .join();
//                
//        
//        //判断下载的文件大小
//        
//        
//        //结束计时
//        long time2 = System.currentTimeMillis();              

		// 记录push_list
		String[] p_list = push_list.split(",");
		System.out.println("push list length :" + p_list.length);
		String[] path = url.split("/");
		String requestSegmentName = path[path.length - 1];
		System.out.println("request segment name:" + requestSegmentName);
		AtomicInteger atomicInt = new AtomicInteger(1);

		long startTime = System.currentTimeMillis();

		client.sendAsync(request,
				BodyHandlers.ofFile(Path.of("/Users/wuyue/Desktop/" + "TOS-II-360p", requestSegmentName)),
				(HttpRequest initiatingRequest, HttpRequest pushPromiseRequest,
						Function<HttpResponse.BodyHandler<Path>, CompletableFuture<HttpResponse<Path>>> acceptor) -> {
					acceptor.apply(HttpResponse.BodyHandlers.ofFile(Path.of("/Users/wuyue/Desktop/" + "TOS-II-360p",
							Paths.get(pushPromiseRequest.uri().getPath()).getFileName().toString())))
							.thenAccept(resp -> {
								System.out.println("[" + atomicInt.getAndIncrement() + "] Pushed response: "
										+ resp.uri() + ", headers: " + resp.headers());
								recordDowndloadInfo(startTime, resp, temp,filePath,p_list);
								
								// 如果个数等于push_list的长度，说明接收push的内容完毕
								if (atomicInt.getAcquire() - 1 == p_list.length) {
									System.out.println("个数相等");
									recordDowndloadInfo(startTime, resp, temp, filePath,p_list);
									
									System.out.println(temp[0]);
									System.out.println(temp[1]);
									System.out.println(temp[2]);
									isEnd[0] = true;
								}
								// 如果个数不等，
								else {
									System.out.println("个数不等.应该push的个数是：" + p_list.length + "现在push的个数是："
											+ (atomicInt.getAcquire() - 1));
									//recordDowndloadInfo(startTime, resp, temp, filePath);
									isEnd[0] = false;
									
								}
							});
					System.out.println("Promise request: " + pushPromiseRequest.uri());
					System.out.println("Promise request: " + pushPromiseRequest.headers());
				}).thenAccept(baseSegmentResponse -> {
					System.out.println("**********baseSegmentResponse*************");
					recordBaseLayerDowndloadInfo(startTime, baseSegmentResponse, temp, filePath);
				}).join();
		
		return temp;
		// 记录push文件的大小
		// 为什么是数组类型。匿名对象存在一种风险：内部对象在使用外部对象的变量时，若外部对象已被销毁导致变量为空，所以变量必须是final类型
		// 在初始化这个局部变量以后我又对他进行了赋值，所有报错。网上搜到的解决方案是将这个变量类型改成数组类型。
//		double[] sizeofPushfile = new double[1] ;
////		//记录所有文件的大小
//		double sizeofTotlefile = 0;	
//		//从pushpromise map中获得大小
//		 promisesMap.entrySet().forEach((entry) -> {
//			 
//	            System.out.println("Request = " + entry.getKey()
//	                    + ", \nResponse = " + entry.getValue().join().body());
//	            File file = entry.getValue().join().body().toFile();	          
//	            sizeofPushfile[0] = file.length() + sizeofPushfile[0];
//	            System.out.println("push的文件大小为"+file.length()+"B");	            
//	        });          

//		//获得请求文件的大小，单位B
//		File f= new File(filePath);
//		if (f.exists() && f.isFile()){
//			System.out.println("请求的文件大小为"+f.length()+"B");
//		}else{
//			System.out.println("file doesn't exist or is not a file");
//		}
//		sizeofTotlefile = sizeofPushfile[0]+f.length();
//		
//		//获得当前带宽。单位是kbps。
//		//时间单位转换成秒，文件大小单位转换为b，
//		bandwidth = (sizeofTotlefile * 8)/ (double)(time2-time1);
//
//		//下载文件大小
//        temp[0] = sizeofTotlefile;
//        System.out.println("输出：下载的文件大小为"+sizeofTotlefile+"B");
//        //下载文件时间
//        temp[1] = time2-time1;
//        System.out.println("输出：文件的下载时间为 "+ temp[1]+"ms");
//        //当前下载带宽
//        temp[2] = bandwidth;
//        System.out.println("输出：当前的下载带宽为"+temp[2]+"kbps");
//        
//
//        System.out.println("\nPush promises map size: " + promisesMap.size() + "\n");       
//        return temp;
	}

	//记录大小 用作大小预测
	double sizeofL0 = 0;
	double sizeofL1 = 0;
	double sizeofL2 = 0;	
	//接收一个push文件以后调用该函数统计下载信息
	public void recordDowndloadInfo(long startTime, HttpResponse resp, double[] result, String filePath,String[] p_list) {
		
		// 计算push 文件大小
		String[] path = resp.uri().toString().split("/");
		String localPath = path[path.length - 2] + "/" + path[path.length - 1];
		//localpath 是BBB-II-720p/BBB-II-720p.seg101-L0.svc
		File file = new File("/Users/wuyue/Desktop/" + localPath);
		
		if (file != null && file.exists()) {
			
			//对文件名进行处理用作大小预测
			//对BBB-II-720p/BBB-II-720p.seg101-L0.svc进行处理
			String[] temp1 = localPath.split("/");
			System.out.println("1###################");
			System.out.println(temp1[1]);
			//temp1[1]是BBB-II-720p.seg101-L0.svc，再次处理
			String[] temp2 = temp1[1].split("\\.");
			System.out.println("2###################");
			System.out.println(temp2[0]);
			//temp2[1]是seg101-L0，现在需要检查最后一个字母是多少确定是第几层
			//取出这个字符串的最后一个字符
			String lastNum = temp2[1].substring(temp2[1].length() - 1);
			System.out.println("3###################");
			System.out.println("3###################"+lastNum);
			
			//如果push的文件是2个，说明下载到最高层
			//将三层全部放到大小预测容器中
			if(p_list.length == 2)
			{
				//如果是L0层
				if(Integer.parseInt(lastNum) == 0)
				{
					sizeofL0 = file.length();
				}
				//如果是L1层
				if(Integer.parseInt(lastNum) == 1)
				{
					sizeofL1 = file.length();
					//只有l0、l1是push过来的，要记录l2的大小只能从正常下载的去取
					//放到大小预测的容器中
					Pair<Double> pair1 = new Pair<Double>();
					Pair<Double> pair2 = new Pair<Double>();
					pair1.setFirst(sizeofL0);
					pair1.setSecond(sizeofL1);
					pair2.setFirst(sizeofL0);
					sizeofL2 = new File(filePath).length();
					pair2.setSecond(sizeofL2);
					System.out.println("sizeofL0:"+sizeofL0);
					System.out.println("sizeofL1:"+sizeofL1);
					System.out.println("sizeofL2:"+sizeofL2);
					
					SizePredictor.getList1().add(pair1);
					SizePredictor.getList2().add(pair2);
				}
				
			}
				
			
			
			System.out.println("本次下载的文件名:" + file.getName());
            
			double fileSize = file.length();
			System.out.println("本次下载的文件大小:" + fileSize + "B");
			
			sumSize = sumSize + fileSize;
			double intervalTime = System.currentTimeMillis() - startTime;
			System.out.println("本次下载时间:" + intervalTime + " ms");
			lastIntervalTime = intervalTime;
			
			

			//计算之前push文件大小
			for(int i = 0;i< p_list.length;i++)
			{
				File file1 = new File("/Users/wuyue/Desktop/" + p_list[i]);
				sumSize = sumSize + file1.length();
			}
			
			// 计算请求文件大小
			File f = new File(filePath);
			System.out.println("------------------");
			System.out.println("总的文件大小为:" + (sumSize + f.length()) + "B");
			// System.out.println("总的文件大小为:" + sumSize+ "B");
			System.out.println("总的下载时间为:" + lastIntervalTime + "ms");
			avgBandWidth = ((sumSize + f.length()) / lastIntervalTime) * 8;
			// avgBandWidth =(sumSize/lastIntervalTime)*8;
			System.out.println("下载带宽为:" + avgBandWidth + "Kb/s");
			System.out.println("------------------");

			result[0] = sumSize + f.length();
			// result[0] = sumSize;
			result[1] = intervalTime;
			result[2] = avgBandWidth;
			
		} else {
			System.out.println("Download Error！");
		}
	}

	public void recordBaseLayerDowndloadInfo(long startTime, HttpResponse resp, double[] result, String filePath) {

		File file = new File(filePath);
		System.out.println("请求文件大小为：" + file.length());
		System.out.println("请求文件下载时间为：" + (System.currentTimeMillis() - startTime));
		

	}

	// 不使用push发送http1.1请求
	// url：下载文件的URL。filepath：保存地址
	public double[] Sendhttp1_1Request_nopush(String url, String filepath)
			throws URISyntaxException, IOException, InterruptedException {
		System.out.println("下载文件为:"+url);
		System.out.println("****************************************");
		double bandwidth = 0;
		double[] temp = new double[3];
		// 安全证书问题
		System.setProperty("javax.net.ssl.trustStore",
				"/Library/Java/JavaVirtualMachines/jdk-11.0.3.jdk/Contents/Home/lib/security/cacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		// 初始化请求
		
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
				// .headers("Content-Type","application/x-www-form-urlencoded","Accept","text/plain","mykey1",
				.GET().version(HttpClient.Version.HTTP_1_1).build();
		// 初始化http客户端
		HttpClient client = HttpClient.newBuilder().build();
		// 开始计时
		Instant inst1 = Instant.now();
		// 发出请求并接收
		HttpResponse<Path> response = client.send(request, BodyHandlers.ofFile(Paths.get(filepath)));
		// 结束计时
		Instant inst2 = Instant.now();
		//Thread.sleep(500);
		File f = new File(filepath);
		
		// 获得下载文件的大小，单位B
		if (f.exists() && f.isFile()) {
			System.out.println("下载的文件大小为" + f.length() + "B");
		} else {
			System.out.println("file doesn't exist or is not a file");
		}
		// 获得当前带宽。单位是kbps。
		// 时间单位转换成秒，文件大小单位转换为b，
		bandwidth = ((double) f.length() * 8 / (double) (Duration.between(inst1, inst2).toMillis()));
		// System.out.println("当前的下载带宽为"+bandwidth+"kbps");

		// System.out.println("status code:"+response.statusCode());
		// System.out.println("response body:"+response.body());

		// 下载文件大小
		temp[0] = f.length();
		System.out.println("输出：下载的文件大小为" + temp[0] + "B");
		// 下载文件时间
		temp[1] = Duration.between(inst1, inst2).toMillis();
		System.out.println("输出：文件的下载时间为 " + temp[1] + "ms");
		// 当前下载带宽
		temp[2] = bandwidth;
		System.out.println("输出：当前的下载带宽为" + temp[2] + "kbps");
		return temp;
	}

	//不使用push发送http2请求
	public double[] Sendhttp_2Request_nopush(String url, String filepath)
			throws URISyntaxException, IOException, InterruptedException {
		System.out.println("下载文件为:"+url);
		System.out.println("****************************************");
		double bandwidth = 0;
		double[] temp = new double[3];
		// 安全证书问题
		System.setProperty("javax.net.ssl.trustStore",
				"/Library/Java/JavaVirtualMachines/jdk-11.0.3.jdk/Contents/Home/lib/security/cacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
		// 初始化请求
		
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
				// .headers("Content-Type","application/x-www-form-urlencoded","Accept","text/plain","mykey1",
				.GET().version(HttpClient.Version.HTTP_2).build();
		// 初始化http2客户端
		HttpClient client = HttpClient.newBuilder().build();
		// 开始计时
		Instant inst1 = Instant.now();
		// 发出请求并接收
		HttpResponse<Path> response = client.send(request, BodyHandlers.ofFile(Paths.get(filepath)));
		//BodyHandlers.ofFile(Paths.get(filepath))
		// 结束计时
		Instant inst2 = Instant.now();
		System.out.println("file path为" + filepath);
		File f = new File(filepath);
		
		// 获得下载文件的大小，单位B
		if (f.exists() && f.isFile()) {
			System.out.println("下载的文件大小为" + f.length() + "B");
//			Thread.sleep(3000);
//			System.out.println("下载的文件（新）大小为" + f.length() + "B");
			//System.out.println("下载的文件为" + filepath);
		} else {
			System.out.println("file doesn't exist or is not a file");
		}
		// 获得当前带宽。单位是kbps。
		// 时间单位转换成秒，文件大小单位转换为b，
		bandwidth = ((double) f.length() * 8 / (double) (Duration.between(inst1, inst2).toMillis()));
		// System.out.println("当前的下载带宽为"+bandwidth+"kbps");

		// System.out.println("status code:"+response.statusCode());
		// System.out.println("response body:"+response.body());

		// 下载文件大小
		temp[0] = f.length();
		System.out.println("输出：下载的文件大小为" + temp[0] + "B");
		// 下载文件时间
		temp[1] = Duration.between(inst1, inst2).toMillis();
		System.out.println("输出：文件的下载时间为 " + temp[1] + "ms");
		// 当前下载带宽
		temp[2] = bandwidth;
		System.out.println("输出：当前的下载带宽为" + temp[2] + "kbps");
		return temp;
	}
	
	private HttpResponse.PushPromiseHandler<Path> pushPromiseHandler() {
		return HttpResponse.PushPromiseHandler.of(promiseHandler, promisesMap);
	}
	// 测试
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		HTTP2 http2 = new HTTP2();
		String url = "https://192.168.1.2:8080/svc/content/TOS-II-360p/TOS-II-360p.seg20-L0.svc";
		String filePath = "/Users/wuyue/Desktop/TOS-II-360p/TOS-II-360p.seg20-L0.svc";
		
		String push_list = "TOS-II-360p.seg21-L0.svc" + ",TOS-II-360p.seg22-L0.svc" + ",TOS-II-360p.seg23-L0.svc"
				+ ",TOS-II-360p.seg24-L0.svc" + ",TOS-II-360p.seg25-L0.svc" + ",TOS-II-360p.seg125-L0.svc";
		System.out.println("PUSH_LIST是"+push_list);
		http2.Sendhttp2Request_push(url, filePath, push_list);
		//http2.Sendhttp_2Request_nopush(url,filePath);
		//计算文件大小测试
//		File f = new File(filePath);
//		System.out.println("下载文件大小为" + f.length());
	}
}


