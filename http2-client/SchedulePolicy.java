package wu.test;

public interface SchedulePolicy {
    
	//决定下一个segment的下载层次
	//返回string类型的数组，数组中存放chunk ID，layer，url
	String[] nextLayerToDownloader();
	
	//更新函数
	void update(double[] temp);
	
	//获取segid
	int getSegID();
	
	//获取layernum
	int getLayerNum();
	
	//获取chunk大小
	double getSize();
	
	//获取URL列表
	
	
	//获取当前的下载层次
	int getPreviousLayer();
	
	public void setSegID(int segID);
	
	public void setchunkLayer(int chunkLayer);
	
	public double getLast_down_bandwidth();
	
	public void setLast_down_bandwidth(double last_down_bandwidth);
	
	public double getLast_down_time();
	
	public void setLast_down_time(double last_down_time) ;
}
