package wu.test;

public class Chunk {
	//数据成员	
	private int layerNum;
    private int segmentID;
    private double size;
    //默认构造方法
    public Chunk(){}
    //含参构造方法
    public Chunk(int segID,int layernum,double s)
    {
    	this.segmentID=segID;
    	this.layerNum=layernum;
    	size=s;
    }
    //含参构造方法(没有大小)
    //因为使用push策略的话不知道每个块的大小
    public Chunk(int segID,int layernum)
    {
    	this.segmentID=segID;
    	this.layerNum=layernum;
    }
    //获取chunk大小
    public double getChunkSize()
    {
    	return size;
    }
    //获取chunk层次
    public int getLayerNum()
    {
    	return layerNum;
    }
    //获取chunkID
    public int getSegmentID()
    {
    	return segmentID;
    }
}
