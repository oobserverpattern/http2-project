package wu.test.policy;

import wu.test.Buffer;
import wu.test.MyPropertiesPro;
import wu.test.ParseMPD;
import wu.test.SchedulePolicy;

public class PureBufferBasedPolicyWithServerPush implements SchedulePolicy {
	public PureBufferBasedPolicyWithServerPush(ParseMPD mpd, Buffer buffer) {
		this.mpd = mpd;
		this.buffer = buffer;
	}

	public String[] nextLayerToDownloader() {
		String[] result = new String[3];
		// 如果buffer长度小于最小阈值
		if (buffer.getAvail_seg() < Integer.parseInt(MyPropertiesPro.getProperty("conf.min_buffer"))) {
			// 请求基本层
			System.out.println("buffer长度:" + buffer.getAvail_seg() + "小于buffer最小阈值。请求基本层。");
			segID = segID + 1;
			chunkLayer = 0;
			result[0] = String.valueOf(segID);
			result[1] = String.valueOf(chunkLayer);
			result[2] = mpd.getUrl_list_L0().get(String.valueOf(segID));
			return result;
		}
		// 如果buffer长度大于最小阈值，下载最高层
		else {
			System.out.println("buffer长度:" + buffer.getAvail_seg() + "大于buffer最小阈值。请求最高层。");
			segID = segID + 1;
			chunkLayer = 2;
			result[0] = String.valueOf(segID);
			result[1] = String.valueOf(chunkLayer);
			result[2] = mpd.getUrl_list_L2().get(String.valueOf(segID));
			return result;
		}
	}

	public void update(double[] temp) {
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
	private Buffer buffer;
	// 下载信息
	// 上一次的下载带宽
	private double last_down_bandwidth;
	// 上一次的下载时间
	private double last_down_time;
}
