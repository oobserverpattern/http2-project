package wu.test;
import java.util.ArrayList;


public class Buffer {
	//数据成员
	private ArrayList<Segment> segments;
	//buffer最大长度(定值)
	private int maxSize;
	//private volatile int chunkNums;
	private int avail_seg;
	private int write_pos,read_pos;
	//private Lock lock;
	//成员方法
	//默认构造方法
	public Buffer() {}
	//含参构造方法
	public Buffer(int size)
	{
		maxSize=size;
		avail_seg=0;
		write_pos=0;
		read_pos=0;
		segments = new ArrayList<>(maxSize);
	    //Lock lock = new ReentrantLock();
	}
	//判断基本层是否满
	public Boolean isBaselayerFull() 
	{
		if(maxSize==avail_seg)
			return true;
		else if(maxSize>avail_seg)
			return false;
		else return true;
	}
	//判断buffer是否满
	public Boolean isFull()
	{
		//基本层不满，buffer肯定不满
		if(!isBaselayerFull())
			return false;
		//基本层满了
		else
//			for(int i=0;i<avail_seg;i++)
//			{
//				//将3改为配置文件读入
//				//如果第i个segment不满三层
//				if((buffer.get(i).getMaxAvailableLayer()+1)!=3)
//					return false;
//			}
			for (Segment tmp: segments) {
				if (tmp.getMaxAvailableLayer() < 3) {
					return false;
				}
			}
		return true;
	}
	
	//判断buffer是否空
	public Boolean isEmpty()
	{
		return avail_seg==0;
	}
	
	//向缓冲区中放入一个chunk
	public void putChunk(Chunk c) {
		synchronized (this) {
			//buffer满或者基本层满并且下一个要下载的是基本层
			while (isFull() || (isFull() == false && isBaselayerFull() == true && c.getLayerNum() == 0)) {
				try {
					System.out.println("buffer满了");
					// buffer满，生产阻塞
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// buffer不满，继续下载
			// if(isFull()==false)
			{
				System.out.println("buffer不满");
				// 如果是l0层的segment
				if (c.getLayerNum() == 0) {
					avail_seg++;
				}

				for (int i = 0; i < segments.size(); i++) {

					// buffer中有相应的segmentID
					if (segments.get(i).getSegmentID() == c.getSegmentID()) {
						segments.get(i).insertChunk(c);
						return;
					}
				}
				{// buffer中没有相同id的segment
					Segment sg = new Segment(c.getSegmentID());
					sg.insertChunk(c);
					insertSegment(sg);
				}
			}
			this.notifyAll();
		}
	}

	//将segment按从小到达顺序插入buffer中
	public void insertSegment(Segment seg)
	{
		//如果buffer不满
		if(segments.size()<maxSize)
		{
			segments.add(seg);
			insertsort(segments);
		}
	}
	
	int timeofStall = 0;
	//记录播放层次
		int numofLevel0 = 0;
		int numofLevel1 = 0;
		int numofLevel2 = 0;
		
		//记录播放之前的质量层次
		int level0 = 0;
		//记录播放之后的质量层次
		int level1 = 0;
		//记录质量切换次数
		int numofLevelChange = 0;
	//取出buffer中第一个segment
	public void popFront()
	{
		
		synchronized (this)
		{
			
			//如果buffer为空
			while(segments.isEmpty()==true)
			{
				try {
					timeofStall++;
					System.out.println("buffer空了");
					// buffer满，生产阻塞
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				 
			}
			PlayResult.print("result/play_result.txt","********************开始播放********************");
			PlayResult.print("result/play_result.txt","播放之前buffer长度为:"+getAvail_seg());
			PlayResult.print("result/play_result.txt","当前播放的segment ID是:"+getSegment(0).getSegmentID());
			level0 = getSegment(0).getMaxAvailableLayer(); 
			PlayResult.print("result/play_result.txt", "当前播放的segment的最大可用层为"+getSegment(0).getMaxAvailableLayer());
			
			if(getSegment(0).getMaxAvailableLayer() == 1)
			{
				numofLevel0++;
			}
			
			if(getSegment(0).getMaxAvailableLayer() == 2)
			{
				numofLevel1++;
			}
			if(getSegment(0).getMaxAvailableLayer() == 3)
			{
				numofLevel2++;
			}
			//buffer不空，继续播放
			segments.remove(0);
			avail_seg--;
			this.notifyAll();
			
			//播放完毕以后再判断buffer是否为空
			//如果buffer为空
			while(segments.isEmpty()==true)
			{
				try {
					timeofStall++;
					System.out.println("buffer空了");
					
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				 
			}
			
			level1 =getSegment(0).getMaxAvailableLayer();
			if(level0 != level1)
			{
				numofLevelChange++;
			}
			PlayResult.print("result/play_result.txt", "********************播放完毕********************");
			PlayResult.print("result/play_result.txt", "下一个播放的segment的最大可用层为:"+level1);
			PlayResult.print("result/play_result.txt","l0一共播放了"+numofLevel0+"个");
			PlayResult.print("result/play_result.txt","l1一共播放了"+numofLevel1+"个");
			PlayResult.print("result/play_result.txt","l2一共播放了"+numofLevel2+"个");
			PlayResult.print("result/play_result.txt","当前播放层次为："+String.format("%.2f",(numofLevel0*0+numofLevel1*1+numofLevel2*2)/((numofLevel0+numofLevel1+numofLevel2)*1.0)));
			PlayResult.print("result/play_result.txt","当前播放码率为："+(((numofLevel0 * Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size")))
																	  +(numofLevel1 * (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size"))+Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L1_avg_size"))))
																	  +(numofLevel2 * Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_total_size"))))*8/1024/2/(numofLevel0+numofLevel1+numofLevel2)));
			PlayResult.print("result/play_result.txt", "质量切换次数为"+numofLevelChange+"次");
			PlayResult.print("result/play_result.txt", "播放完毕buffer长度为"+getAvail_seg());
			PlayResult.print("result/play_result.txt", "总计播放暂停次数为："+timeofStall);
			PlayResult.print("result/play_result.txt","*************************************************************");
			
		}
	}
	
	//获取buffer长度
	public int getSize()
	{
		return segments.size();
	}
	//获取buffer最大长度
	public int getMaxSize()
	{
		return maxSize;
	}
	//获取当前下载位置
	public int getWritePosition()
	{
		return write_pos;
	}
	//设置当前下载位置
	public void setWritePosition(int w_pos)
	{
		write_pos=w_pos;
	}
	//获取当前播放位置
	public int getReadPosition()
	{
		return read_pos;
	}
	//设置当前播放位置
	public void setReadPosition(int r_pos)
	{
		read_pos=r_pos;
	}
	//获取buffer
	public ArrayList<Segment> getBuffer()
	{
		return segments;
	}
	//获取buffer中某一个可用的segment
	Segment getSegment(int num)
	{
		return segments.get(num);
	}
	//获取buffer中可用segment的数量
	public int getAvail_seg()
	{
		return avail_seg;
	}

	//重载插入排序
	public void insertsort(ArrayList<Segment> buf)
	{
		Segment temp;
		for(int i=1;i<buf.size();i++)
			//待排segment ID小于有序序列的最后一个元素时，向前插入
			if(buf.get(i).getSegmentID()<buf.get(i-1).getSegmentID())
			{
				temp=buf.get(i);
				for(int j=i;j>=0;j--)
				{
					if(j>0&&buf.get(j-1).getSegmentID()>temp.getSegmentID())
						buf.get(j).setSegmentID(buf.get(j-1).getSegmentID());
					else
					{
						buf.get(j).setSegmentID(temp.getSegmentID());
						break;
					}
				}				
			}
	}
}
