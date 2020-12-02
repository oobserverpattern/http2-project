package wu.test;
import java.util.*;


public class Segment
{
//数据成员
private TreeMap<Integer, Chunk> chunks;
private int segmentID;
//从配置文件中独读出
private int totalLayers;
private int duration;

//成员方法

//默认构造方法           
public Segment() {}
//含参构造方法
public Segment(int segID)
{	
	this.segmentID=segID;
	this.chunks=new TreeMap<Integer, Chunk>();
}
//获取segment id
public int getSegmentID()
{
	return this.segmentID;
}
//设置segment id
public void setSegmentID(int id)
{
	this.segmentID=id;
}
//判断某一层是否有效
public Boolean getLayerExist(int layer)
{
	return chunks.containsKey(layer);
}
//向segment中插入一个chunk
public void insertChunk(Chunk chunk)
{
	int layer = chunk.getLayerNum();
	chunks.put(layer, chunk);
}
//获得最大可用层
public int getMaxAvailableLayer()
{
//	int pos=0;
//	//将3改为配置文件读入
//	//segment中chunk小于3个并且每一个chunk都是可用的
//	while(pos<3 && vaildStatus.get(pos)== true)
//	{
//		pos++;
//	}
//	return pos-1;
	return chunks.size();
}
//segment id的比较函数（相当于c++中的重载）
public Boolean Comparable(Segment seg)
{
	return segmentID<seg.getSegmentID();
}
}
