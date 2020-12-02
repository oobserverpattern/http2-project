package wu.test;

import java.io.IOException;
import java.net.URISyntaxException;

import wu.test.policy.*;;

public class test {
	static int signal;
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException 
	{
		
		//配置文件读取
		String pFilePath = "resources/config.properties";
		MyPropertiesPro mp = new MyPropertiesPro(pFilePath);
		
		Buffer buf = new Buffer(10);
		ParseMPD parse = new ParseMPD();
		HTTP2 http2client = new HTTP2();
		
		//混合算法
		//Policy mypolicy = new Policy(new BufferBasedWithServerPushPolicy(parse,buf),"BufferBasedWithServerPushPolicy");
		//Policy mypolicy = new Policy(new BufferBasedPolicyWithHTTP_2(parse,buf),"BufferBasedPolicyWithHTTP_2");
		
		//throughput based 
		//Policy mypolicy = new Policy(new PureThroughputBasedPolicyWithServerPush(parse),"PureThroughputBasedPolicyWithServerPush");
		//Policy mypolicy = new Policy(new PureThroughputBasedPolicy(parse),"PureThroughputBasedPolicy");
		
		//buffer based 
		//Policy mypolicy = new Policy(new PureBufferBasedPolicyWithServerPush(parse,buf),"PureBufferBasedPolicyWithServerPush");
		//Policy mypolicy = new Policy(new PureBufferBasedPolicy(parse,buf),"PureBufferBasedPolicy");
		
		//混合算法+size predict
		Policy mypolicy = new Policy(new BufferBasedPolicyWithServerPushAndSizePredict(parse,buf),"BufferBasedPolicyWithServerPushAndSizePredict");
		//Policy mypolicy = new Policy(new BufferBasedPolicyWithSizePredict(parse,buf),"BufferBasedPolicyWithSizePredict");
		
		Downloader down = new Downloader(buf,parse,http2client,mypolicy);	
		//播放线程
		Player play = new Player(buf);		
		//下载mpd文件
		System.out.println("开始下载");
		long startTime =  System.currentTimeMillis();
   
		
		//使用push初始化下载
		//请求mpd文件，push连续5个segment
		down.ini_downloader_push();
		System.out.println("**********初始化下载结束**********");
		long endTime =  System.currentTimeMillis();
		PlayResult.print("result/down_result.txt", "使用server push启动时间为"+(endTime-startTime));
	
		
		//不使用push(HTTP1.1)初始化下载
//		down.mpd_downloader_HTTP1_1(MyPropertiesPro.getProperty("conf.mpd_path"),MyPropertiesPro.getProperty("conf.mpd_save"));
//		System.out.println("mpd文件下载结束");
//		down.ini_downloader_nopush_HTTP1_1();
//		System.out.println("**********初始化下载结束**********");
//		long endTime =  System.currentTimeMillis();
//		PlayResult.print("result/down_result.txt", "不使用server push启动时间为"+(endTime-startTime));
	
		 
		//不使用push(HTTP2)初始化下载 
//		down.mpd_downloader_HTTP2(MyPropertiesPro.getProperty("conf.mpd_path"),MyPropertiesPro.getProperty("conf.mpd_save"));
//		System.out.println("mpd文件下载结束");
//		down.ini_downloader_nopush_HTTP2();
//		System.out.println("**********初始化下载结束**********"); 
//		long endTime =  System.currentTimeMillis();
//		PlayResult.print("result/down_result.txt", "不使用server push启动时间为"+(endTime-startTime));
		
		
		//启动播放线程
		down.start();
		//启动播放线程
		play.start();
		
		
		
//		Buffer buffer=new Buffer(10);
//		
//		Chunk c1 = new Chunk(1,0,100);
//		Chunk c2 = new Chunk(2,0,100);
//		Chunk c3 = new Chunk(3,0,100);
//		Chunk c4 = new Chunk(1,1,100);
//		Chunk c5 = new Chunk(1,2,100);
//		
//		buffer.putChunk(c1);
//		buffer.putChunk(c2);
//		buffer.putChunk(c3);
//		buffer.putChunk(c4);
//		buffer.putChunk(c5);
//		
//		System.out.println("buffer size: "+buffer.getSize());
//		 
//		if(buffer.getSize() > 0)
//		{
//			System.out.println("buffer中第1个segmentID是："+buffer.getSegment(0).getSegmentID());
//			System.out.println("buffer中第1个segmentID的最大可用层是："+buffer.getSegment(0).getMaxAvailableLayer());
//			System.out.println("buffer中第2个segmentID是："+buffer.getSegment(1).getSegmentID());
//			System.out.println("buffer中第2个segmentID的最大可用层是："+buffer.getSegment(1).getMaxAvailableLayer());
//			System.out.println("buffer中第3个segmentID是："+buffer.getSegment(2).getSegmentID());
//			System.out.println("buffer中第3个segmentID的最大可用层是："+buffer.getSegment(2).getMaxAvailableLayer());
//			
//			buffer.popFront();
//			System.out.println("buffer size: "+buffer.getSize());
//			System.out.println("buffer中第1个segmentID是："+buffer.getSegment(0).getSegmentID());
//			System.out.println("buffer中第1个segmentID的最大可用层是："+buffer.getSegment(0).getMaxAvailableLayer());
//		}
		
	}

}
