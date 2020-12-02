package wu.test;

public class SafetyPushPolicy implements SchedulePolicy{
	
	public SafetyPushPolicy(ParseMPD mpd)
	{
		this.mpd = mpd;
	}
	
	public String[] nextLayerToDownloader() {

		String[] result = new String[3];
		Buffer buffer = new Buffer();
		//如果buffer长度小于buffer阈值
		//push连续三个seg的基本层
		if(buffer.getAvail_seg() < Integer.parseInt(MyPropertiesPro.getProperty("conf.min_buffer")))
		{
			//仅仅请求下一个segment的了l0层
			segID = segID+1;
			chunkLayer = 0;
			result[0] = String.valueOf(segID);
			result[1] =  String.valueOf(chunkLayer);
			result[2] =  mpd.getUrl_list_L0().get(String.valueOf(segID));
			return result;						
		}
		
		//否则依据带宽进行下载
		else 
		{
			//如果带宽足够下载三层
			if(last_down_bandwidth > (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_BBB_total_size"))) *8/last_down_time)
			{
				segID = segID+1;
				chunkLayer = 2;
				result[0] = String.valueOf(segID);
				result[1] =  String.valueOf(chunkLayer);
				//url为segID的l2层。即仅请求最高层
				result[2] =  mpd.getUrl_list_L2().get(String.valueOf(segID));
				return result;
			
			}
			//如果带宽足够下载前两层  
			if(last_down_bandwidth > Double.parseDouble((MyPropertiesPro.getProperty("conf.svc_BBB_L0_avg_size"))+Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_BBB_L1_avg_size"))) *8/last_down_time)
			{
				segID = segID+1;
				chunkLayer = 1;
				result[0] = String.valueOf(segID);
				result[1] =  String.valueOf(chunkLayer);
				//url为segID的l1层。即仅请求l1层。
				result[2] =  mpd.getUrl_list_L1().get(String.valueOf(segID));
				return result;
				
			}
			//否则仅下载l0层
			else 
			{
				segID = segID+1;
				chunkLayer = 0;
				result[0] = String.valueOf(segID);
				result[1] =  String.valueOf(chunkLayer);
				//url为segID的l0层。即仅请求l0层。
				result[2] =  mpd.getUrl_list_L0().get(String.valueOf(segID));
				return result;	
			}				
		}
	}
		
	public void update(double[] temp) {
		//更新下载文件大小
		size = temp[0];
		//更新下载下载时间
		last_down_time =temp[1];
		//更新带宽
		last_down_bandwidth = temp[2];
	}

	public void setSegID(int segID) {
		this.segID = segID;
	}
	
	public void setchunkLayer(int chunkLayer) {
		this.chunkLayer = chunkLayer;
	}
	
	@Override
	public int getSegID() {
		return segID;
	}

	@Override
	public int getLayerNum() {
		return chunkLayer;
	}

	@Override
	public double getSize() {
		return size;
	}

	@Override
	public int getPreviousLayer() {
		return previousLayer;
	}
	
	
	public double getLast_down_bandwidth() {
		return last_down_bandwidth;
	}

	public void setLast_down_bandwidth(double last_down_bandwidth) {
		this.last_down_bandwidth = last_down_bandwidth;
	}

	public double getLast_down_time() {
		return last_down_time;
	}

	public void setLast_down_time(double last_down_time) {
		this.last_down_time = last_down_time;
	}
	
    //数据成员
	//chunk的信息
	private int segID;
	private int chunkLayer;
	private int previousLayer;
	private double size;
	private ParseMPD mpd;
	//下载信息
	//上一次的下载带宽
	private double last_down_bandwidth;
	//上一次的下载时间
	private double last_down_time;
}
