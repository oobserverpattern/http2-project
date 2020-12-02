package wu.test.policy;

import wu.test.Buffer;
import wu.test.MyPropertiesPro;
import wu.test.ParseMPD;
import wu.test.SchedulePolicy;

public class BufferBasedWithServerPushPolicy implements SchedulePolicy {

	public BufferBasedWithServerPushPolicy(ParseMPD mpd, Buffer buffer) {
		this.mpd = mpd;
		this.buffer = buffer;
	}

	@Override
	public String[] nextLayerToDownloader() {

		String[] result = new String[3];
		double Vup = 0;
		double Vdown = 0;
		double bandwidthLevelPlus = 0;
		double bandwidththisLevel = 0;
		// 如果buffer长度小于最小值 请求l0
		// 采用server push连续5个segment的l0
		if (buffer.getAvail_seg() < Integer.parseInt(MyPropertiesPro.getProperty("conf.min_buffer"))) {
			System.out.println("buffer长度:" + buffer.getAvail_seg() + "小于最小阈值。请求连续5个segment。");
			segID = segID + 1;
			chunkLayer = 0;
			result[0] = String.valueOf(segID);
			result[1] = String.valueOf(chunkLayer);
			result[2] = mpd.getUrl_list_L0().get(String.valueOf(segID));
			return result;

		}

//		if (buffer.getAvail_seg() >= Integer.parseInt(MyPropertiesPro.getProperty("conf.min_buffer"))
//				&& buffer.getAvail_seg() <= buffer.getMaxSize()) 
		else {
			System.out.println("**********buffer长度大于最小阈值 质量提升或下降**********");
			// 先判断当前的下载层次
			// 如果下载的是l0层
			if (chunkLayer == 0) {
				// Vup=((l0+l1)/(l0+l1+l2))*threshold_buffer 论文中是比特率 我是用的是大小，单位是B,因为是比值，单位没有影响
				Vup = (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size"))
						+ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L1_avg_size")))
						/ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_total_size"))
						* Double.parseDouble(MyPropertiesPro.getProperty("conf.threshold_buffer"));

				// 层次加1以后所需的下载带宽
				bandwidthLevelPlus = (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size"))
						+ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L1_avg_size"))) * 8
						/ (Double.parseDouble(MyPropertiesPro.getProperty("conf.segment_duration")) * 1000);

				// 两个条件都满足，质量提升，下载l1层
				if (buffer.getAvail_seg() > Vup && last_down_bandwidth > bandwidthLevelPlus) {
					System.out.println("**********上一次下载的是l0>>>质量提升>>>下载带宽是:" + last_down_bandwidth + ">>>质量提升所需带宽为:"
							+ bandwidthLevelPlus);
					segID = segID + 1;
					chunkLayer = 1;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l1层的url
					result[2] = mpd.getUrl_list_L1().get(String.valueOf(segID));
					return result;
				}

				// else 只能质量不变 因为下载是的l0
				else {
					System.out.println("**********上一次下载的是l0>>>质量不变>>>下载带宽是:" + last_down_bandwidth + "质量提升所需带宽为:"
							+ bandwidthLevelPlus);

					segID = segID + 1;
					chunkLayer = 0;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l0层
					result[2] = mpd.getUrl_list_L0().get(String.valueOf(segID));
					return result;
				}
			}

			// 如果下载的是l1层
			if (chunkLayer == 1) {
				// Vup值为threshold_buffer 因为质量提升一个层次是2 再除以三层的总大小
				Vup = Double.parseDouble(MyPropertiesPro.getProperty("conf.threshold_buffer"));
				// Vdown 值为 （当前层次的比特率/所有层的比特率）* 因为是比值 所有单位不换了
				Vdown = (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size"))
						+ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L1_avg_size")))
						/ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_total_size"))
						* Double.parseDouble(MyPropertiesPro.getProperty("conf.threshold_buffer"));
				// 层次加1 也就是三层的总下载带宽
				bandwidthLevelPlus = Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_total_size")) * 8
						/ (Double.parseDouble(MyPropertiesPro.getProperty("conf.segment_duration")) * 1024);

				bandwidththisLevel = (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L0_avg_size"))
						+ Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_L1_avg_size"))) * 8
						/ (Double.parseDouble(MyPropertiesPro.getProperty("conf.segment_duration")) * 1024);
				// 两个条件都满足，质量提升，下载l2层
				if (buffer.getAvail_seg() > Vup && last_down_bandwidth > bandwidthLevelPlus) {
					System.out.println("**********上一次下载的是l1>>>质量提升>>>下载带宽是:" + last_down_bandwidth + ">>>质量提升所需带宽为:"
							+ bandwidthLevelPlus);
					segID = segID + 1;
					chunkLayer = 2;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l2层的url
					result[2] = mpd.getUrl_list_L2().get(String.valueOf(segID));
					return result;
				}
				// 满足以下条件 质量下降
				else if (buffer.getAvail_seg() < Vdown || last_down_bandwidth < bandwidththisLevel) {
					System.out.println("**********上一次下载的是l1>>>质量下降>>>下载带宽是:" + last_down_bandwidth + ">>>质量下降满足的带宽要求为"
							+ bandwidththisLevel);
					segID = segID + 1;
					chunkLayer = 0;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l0层的url
					result[2] = mpd.getUrl_list_L0().get(String.valueOf(segID));
					return result;
				}
				// 否砸质量不变
				else {
					System.out.println("**********上一次下载的是l1>>>质量不变>>>下载带宽是>>>" + last_down_bandwidth + ">>>质量提升所需带宽为:"
							+ bandwidthLevelPlus + "质量下降满足的带宽要求为" + bandwidththisLevel);
					segID = segID + 1;
					chunkLayer = 1;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l1层的url
					result[2] = mpd.getUrl_list_L1().get(String.valueOf(segID));
					return result;
				}
			}
			// 如果下载的是l2
			// 只能质量不变或者质量下降
			if (chunkLayer == 2) {
				// Vdown值为threshold_buffer 因为质量层次是2 和所有层的比值是1
				Vdown = Double.parseDouble(MyPropertiesPro.getProperty("conf.threshold_buffer"));
				bandwidththisLevel = (Double.parseDouble(MyPropertiesPro.getProperty("conf.svc_total_size"))) * 8
						/ (Double.parseDouble(MyPropertiesPro.getProperty("conf.segment_duration")) * 1000);
				// 满足一下条件 质量下降
				if (buffer.getAvail_seg() < Vdown || last_down_bandwidth < bandwidththisLevel) {
					System.out.println("**********上一次下载的是l2>>>质量下降>>>下载带宽是:" + last_down_bandwidth + "质量提升所需带宽为:"
							+ bandwidththisLevel);
					segID = segID + 1;
					chunkLayer = 1;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l1层的url
					result[2] = mpd.getUrl_list_L1().get(String.valueOf(segID));
					return result;
				}
				// 否则质量不变
				else {
					System.out.println("**********上一次下载是的l2>>>质量不变>>>下载带宽是:" + last_down_bandwidth);
					segID = segID + 1;
					chunkLayer = 2;
					result[0] = String.valueOf(segID);
					result[1] = String.valueOf(chunkLayer);
					// 传入的URL是l1层的url
					result[2] = mpd.getUrl_list_L2().get(String.valueOf(segID));
					return result;

				}
			}
		}
		return result;
	}

	@Override
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
