package wu.test;

public class Player extends Thread{
	
	private Buffer buf;	
	
	//构造方法
	public Player(Buffer buf) {
		this.buf = buf;	
	}

//	//记录播放层次
//	int numofLevel0 = 0;
//	int numofLevel1 = 0;
//	int numofLevel2 = 0;
//	
//	//记录播放之前的质量层次
//	int level0 = 0;
//	//记录播放之后的质量层次
//	int level1 = 0;
//	//记录质量切换次数
//	int numofLevelChange = 0;
	
	
	//播放一个segment
	public void consume_item() throws InterruptedException
	{
	   
//		PlayResult.print("result/play_result.txt","********************开始播放********************");
//		PlayResult.print("result/play_result.txt","播放之前buffer长度为:"+buf.getAvail_seg());
//		PlayResult.print("result/play_result.txt","当前播放的segment ID是:"+buf.getSegment(0).getSegmentID());
//		level0 = buf.getSegment(0).getMaxAvailableLayer(); 
//		PlayResult.print("result/play_result.txt", "当前播放的segment的最大可用层为"+buf.getSegment(0).getMaxAvailableLayer());
//		
//		if(buf.getSegment(0).getMaxAvailableLayer() == 1)
//		{
//			numofLevel0++;
//		}
//		
//		if(buf.getSegment(0).getMaxAvailableLayer() == 2)
//		{
//			numofLevel1++;
//		}
//		if(buf.getSegment(0).getMaxAvailableLayer() == 3)
//		{
//			numofLevel2++;
//		}
//		
		buf.popFront();	
		sleep(2000);
		//播放以后如果buffer长度大于0 进行统计
//		if(buf.getAvail_seg() > 0)
//		{
//		level1 =buf.getSegment(0).getMaxAvailableLayer();
//		if(level0 != level1)
//		{
//			numofLevelChange++;
//		}
//		PlayResult.print("result/play_result.txt", "********************播放完毕********************");
//		PlayResult.print("result/play_result.txt", "下一个播放的segment的最大可用层为:"+level1);
//		PlayResult.print("result/play_result.txt","l0一共播放了"+numofLevel0+"个");
//		PlayResult.print("result/play_result.txt","l1一共播放了"+numofLevel1+"个");
//		PlayResult.print("result/play_result.txt","l2一共播放了"+numofLevel2+"个");
//		PlayResult.print("result/play_result.txt", "质量切换次数为"+numofLevelChange+"次");
//		PlayResult.print("result/play_result.txt", "播放完毕buffer长度为"+buf.getAvail_seg());
//		//PlayResult.print("result/play_result.txt", "总计播放暂停次数为："+timeofStall);
//		PlayResult.print("result/play_result.txt","*************************************************************");
//	    }
		//如果播放完一个buffer是空的
//		else 
//			System.out.println("出错了！！！！");
	}

	
	//整个播放过程
	public void run()
	{
		long startTime =  System.currentTimeMillis();
		PlayResult.print("result/play_result.txt", "开始播放。当前时间为"+startTime);
		int count = 0;
		while(true)
		{
			//播放一个segment
			try {
				consume_item();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(++count==Integer.parseInt(MyPropertiesPro.getProperty("conf.total_segment")))
			{
				break;
			}			
		}		
	}
}
