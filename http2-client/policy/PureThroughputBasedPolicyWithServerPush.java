package wu.test.policy;

import wu.test.MyPropertiesPro;
import wu.test.ParseMPD;
import wu.test.SchedulePolicy;

public class PureThroughputBasedPolicyWithServerPush implements SchedulePolicy{
	public PureThroughputBasedPolicyWithServerPush(ParseMPD mpd)
	{
		this.mpd = mpd;	
	}
	
	public String[] nextLayerToDownloader()
	{
		String[] result = new String[3];
		//如果当前带宽足够下载三层
		if(last_down_bandwidth > ((Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_total_size")) * 8/1024)
				/ (Double.parseDouble(MyPropertiesPro.getProperty("conf.segment_duration")))))
		{
			segID = segID + 1;
			chunkLayer = 2;
			result[0] = String.valueOf(segID);
			result[1] = String.valueOf(chunkLayer);
			// 传入的URL是l1层的url
			result[2] = mpd.getUrl_list_L2().get(String.valueOf(segID));
			return result;
		}
		//如果当前带宽足够下载两层
		if(last_down_bandwidth > (((Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size"))
				+ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L1_avg_size")))*8/1024)
			/ (Double.parseDouble(MyPropertiesPro.getProperty("conf.segment_duration")))))
		{
			segID = segID + 1;
			chunkLayer = 1;
			result[0] = String.valueOf(segID);
			result[1] = String.valueOf(chunkLayer);
			// 传入的URL是l1层的url
			result[2] = mpd.getUrl_list_L1().get(String.valueOf(segID));
			return result;
		}
		//否则只下载基本层
		else
		{
			segID = segID + 1;
			chunkLayer = 0;
			result[0] = String.valueOf(segID);
			result[1] = String.valueOf(chunkLayer);
			// 传入的URL是l1层的url
			result[2] = mpd.getUrl_list_L0().get(String.valueOf(segID));
			return result;
		}
	}

	public void update(double[] temp)
	{
		// 更新下载文件大小
		size = temp[0];
		// 更新下载下载时间
		last_down_time = temp[1];
		// 更新带宽
		last_down_bandwidth = temp[2];
		
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

	@Override
	public void setSegID(int segID) {
		this.segID = segID;

	}

	@Override
	public void setchunkLayer(int chunkLayer) {
		this.chunkLayer = chunkLayer;

	}

	@Override
	public double getLast_down_bandwidth() {
		return last_down_bandwidth;
	}

	@Override
	public void setLast_down_bandwidth(double last_down_bandwidth) {
		this.last_down_bandwidth = last_down_bandwidth;

	}

	@Override
	public double getLast_down_time() {
		return last_down_time;
	}

	@Override
	public void setLast_down_time(double last_down_time) {
		this.last_down_time = last_down_time;
	}

	// 数据成员
	// chunk的信息
	private int segID;
	private int chunkLayer;
	private int previousLayer;
	private double size;
	private ParseMPD mpd;
	// 下载信息
	// 上一次的下载带宽
	private double last_down_bandwidth;
	// 上一次的下载时间
	private double last_down_time;
}
