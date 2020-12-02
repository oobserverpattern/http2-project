package wu.test.policy;

import wu.test.ParseMPD;
import wu.test.SchedulePolicy;

public class AllofLayersPolicy implements SchedulePolicy{

	public AllofLayersPolicy(ParseMPD mpd)
	{
		this.mpd = mpd;
	}
	@Override
	//下一个要下载的层次，返回值是要下载的chunk的ID，layer以及URL
	public String[] nextLayerToDownloader() {
		String[] result = new String[3];
		//如果下一个下载的segment是0层
		//下一个下载的还是这个segment，层次为1
		if(chunkLayer == 0)
		{
			chunkLayer = chunkLayer+1;
		    //将int类型强制转换成string类型
			result[0] =  String.valueOf(segID); 
			result[1] =  String.valueOf(chunkLayer);
			result[2] =  mpd.getUrl_list_L1().get(String.valueOf(segID));
			return result;
		}

		//如果上一个下载的segment是1层
		//下一个下载的还是这个segment，层次是2
		if(chunkLayer == 1)
		{
			chunkLayer = chunkLayer+1;
			//将int类型强制转换成string类型
			result[0] =  String.valueOf(segID); 
			result[1] =  String.valueOf(chunkLayer);
			result[2] =  mpd.getUrl_list_L2().get(String.valueOf(segID));
			return result;
		}
		//如果上一个下载的segment是2层
		//下载后一个segment的0层
		if(chunkLayer == 2)
		{
			chunkLayer = 0;
			segID = segID + 1;
			//将int类型强制转换成string类型
			result[0] =  String.valueOf(segID); 
			result[1] =  String.valueOf(chunkLayer);
			result[2] =  mpd.getUrl_list_L0().get(String.valueOf(segID));	
			return result;
		}	
		else 
		{
			System.out.println("出错了");
			return result;
		}
	}
	
	@Override
	//根据下载结构更新策略信息（带宽，时间，大小等
	//形参是由down函数传入的，类型可以再改
	//先默认temp数组的第一个值是下载文件大小，第二个值是下载时间。第三个值是下载带宽
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
