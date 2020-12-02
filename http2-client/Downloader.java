package wu.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import wu.test.policy.*;



public class Downloader extends Thread implements Listener {

	private Buffer buff;
	private ParseMPD parse;
	private HTTP2 http2client;
	private Policy policy;
	private SizePredictor sp;

	public Downloader(Buffer buff, ParseMPD parse, HTTP2 http2client, Policy policy) {
		this.buff = buff;
		this.parse = parse;
		// 初始化时进行mpd文件解析
		parse.parseMPD();
		this.http2client = http2client;
		this.policy = policy;
		this.sp = new SizePredictor();
	}

	// HTTP1.1下载mpd文件并保存在相应位置
	public void mpd_downloader_HTTP1_1(String url, String filePath)
			throws URISyntaxException, IOException, InterruptedException {
		double[] down_info = new double[3];
		down_info = http2client.Sendhttp1_1Request_nopush(url, filePath);
		// 更新策略信息
		policy.update(down_info);
	}

	// HTTP2下载mpd文件并保存在相应位置
	public void mpd_downloader_HTTP2(String url, String filePath)
			throws URISyntaxException, IOException, InterruptedException {
		double[] down_info = new double[3];
		down_info = http2client.Sendhttp_2Request_nopush(url, filePath);
		// 更新策略信息
		policy.update(down_info);
	}

	// 初始化下载(HTTP1_1没有push)
	public void ini_downloader_nopush_HTTP1_1() throws URISyntaxException, IOException, InterruptedException {
		double[] down_info = new double[3];
		// 下载前ini_down个segment的基本层!!!全部都是l0层
		for (int i = 0; i < Integer.parseInt(MyPropertiesPro.getProperty("conf.ini_down")); i++) {
			down_info = http2client.Sendhttp1_1Request_nopush(parse.getUrl_list_L0().get(String.valueOf(i)),
					MyPropertiesPro.getProperty("conf.svc_base_path") + i + "-L0.svc");
			System.out.println("初始化一个chunk");
			Chunk c = new Chunk(i, 0, down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + i + "chunk layer是0" + "大小是" + down_info[0]);
			buff.putChunk(c);
			System.out.println("在buffer中插入chunk成功");
			// 下载完成后更新策略信息
			policy.update(down_info);
			// 更新策略中segment的信息
			policy.setSegID(i);
			policy.setchunkLayer(0);
		}
	}

	// 初始化下载(HTTP2没有push)
	public void ini_downloader_nopush_HTTP2() throws URISyntaxException, IOException, InterruptedException {
		double[] down_info = new double[3];
		// 下载前ini_down个segment的基本层!!!全部都是l0层
		for (int i = 0; i < Integer.parseInt(MyPropertiesPro.getProperty("conf.ini_down")); i++) {
			down_info = http2client.Sendhttp_2Request_nopush(parse.getUrl_list_L0().get(String.valueOf(i)),
					MyPropertiesPro.getProperty("conf.svc_base_path") + i + "-L0.svc");
			System.out.println("初始化一个chunk");
			Chunk c = new Chunk(i, 0, down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + i + "chunk layer是0" + "大小是" + down_info[0]);
			buff.putChunk(c);
			System.out.println("在buffer中插入chunk成功");
			// 下载完成后更新策略信息
			policy.update(down_info);
			// 更新策略中segment的信息
			policy.setSegID(i);
			policy.setchunkLayer(0);
			// 如果策略是加大小预测的，还要将下载信息（大小）存储起来

		}
	}

	// 初始化下载(push)
	public void ini_downloader_push() throws URISyntaxException, IOException, InterruptedException {
		double[] down_info = new double[3];

		String push_list = null;
		push_list = MyPropertiesPro.getProperty("conf.pushList") + "0-L0.svc" + ","
				+ MyPropertiesPro.getProperty("conf.pushList") + "1-L0.svc" + ","
				+ MyPropertiesPro.getProperty("conf.pushList") + "2-L0.svc" + ","
				+ MyPropertiesPro.getProperty("conf.pushList") + "3-L0.svc" + ","
				+ MyPropertiesPro.getProperty("conf.pushList") + "4-L0.svc";
		// 下载MPD文件
		down_info = http2client.Sendhttp2Request_push(MyPropertiesPro.getProperty("conf.mpd_path"),
				MyPropertiesPro.getProperty("conf.mpd_save"), push_list);
		// push过来的应该是seg0-L0，seg1-L0，seg2-L0，seg3-L0，seg4-L0
		// 初始化5个chunk
		for (int i = 0; i < 5; i++) {
			System.out.println("进入初始化下载。初始化一个chunk");
			Chunk c = new Chunk(i, 0);
			System.out.println("准备向buffer中插入的chunk id是" + i + "chunk layer是0");
			buff.putChunk(c);
			System.out.println("在buffer中插入chunk成功");
		}

		// 下载完成后更新策略信息
		policy.update(down_info);
		// 更新策略中segment的信息,更新为最后一个，即4
		policy.setSegID(4);
		policy.setchunkLayer(0);
	}

	// 不使用push下载HTTP1.1
	public void produce_item_nopush() throws URISyntaxException, IOException, InterruptedException {

		String[] policy_result;
		// 下载信息(大小、时间、带宽)
		double[] down_info;
		// 每一次下载信息累加
		double[] down_info_temp = new double[3];
		// 先判断策略决定下载第几层
		// 如果不是l0层，则先将底层下载好

		policy_result = policy.nextLayerToDownloader();

		// 如果策略决定下载最高层
		// 则先下载最底层
		if (policy.getLayerNum() == 2) {
			// 请求l0层
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp1_1Request_nopush(
					parse.getUrl_list_L0().get(String.valueOf(policy.getSegID())),
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy.getSegID() + "-L0.svc");
			Chunk c0 = new Chunk(policy.getSegID(), 0, down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是0");
			buff.putChunk(c0);
			// 下载大小相加
			down_info_temp[0] = down_info_temp[0] + down_info[0];
			// 下载时间相加
			down_info_temp[1] = down_info_temp[1] + down_info[1];
			// 计算下载带宽(bit/ms)
			down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
			System.out.println("***********************一次下载结束***********************");

			// 请求l1层
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp1_1Request_nopush(
					parse.getUrl_list_L1().get(String.valueOf(policy.getSegID())),
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy.getSegID() + "-L1.svc");
			Chunk c1 = new Chunk(policy.getSegID(), 1, down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是1");
			buff.putChunk(c1);
			// 下载大小相加
			down_info_temp[0] = down_info_temp[0] + down_info[0];
			// 下载时间相加
			down_info_temp[1] = down_info_temp[1] + down_info[1];
			// 计算下载带宽(bit/ms)
			down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];

			System.out.println("***********************一次下载结束***********************");

			// 请求最高层，按照策略返回的信息开始下载
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp1_1Request_nopush(policy_result[2],
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
							+ ".svc");
			Chunk c2 = new Chunk(policy.getSegID(), policy.getLayerNum(), down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是2");
			buff.putChunk(c2);

			// 下载大小相加
			down_info_temp[0] = down_info_temp[0] + down_info[0];
			System.out.println("本次下载的文件总大小是:" + down_info_temp[0]);
			// 下载时间相加
			down_info_temp[1] = down_info_temp[1] + down_info[1];
			System.out.println("本次下载文件的总时间是:" + down_info_temp[1]);
			// 计算下载带宽(bit/ms)
			down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
			System.out.println("本次下载文件的总时间是:" + down_info_temp[2]);

			// 下载完毕以后更新策略信息
			policy.update(down_info_temp);
			// 更新策略中segment的信息
			policy.setSegID(Integer.parseInt(policy_result[0]));
			policy.setchunkLayer(Integer.parseInt(policy_result[1]));
			System.out.println("***********************一次下载结束***********************");

		}
		// 如果策略决定下载l1层
		if (policy.getLayerNum() == 1) {
			// 请求l0
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp1_1Request_nopush(
					parse.getUrl_list_L0().get(String.valueOf(policy.getSegID())),
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy.getSegID() + "-L0.svc");
			if (down_info == null) {
				System.out.println("video play end");

			} else {
				Chunk c0 = new Chunk(policy.getSegID(), 0, down_info[0]);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c0);
				// 下载大小相加
				down_info_temp[0] = down_info_temp[0] + down_info[0];
				// 下载时间相加
				down_info_temp[1] = down_info_temp[1] + down_info[1];
				// 计算下载带宽(bit/ms)
				down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
				System.out.println("***********************一次下载结束***********************");

				// 请求l1
				System.out.println("***********************一次下载开始***********************");
				down_info = http2client.Sendhttp1_1Request_nopush(policy_result[2],
						MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
								+ ".svc");
				Chunk c1 = new Chunk(policy.getSegID(), policy.getLayerNum(), down_info[0]);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
				System.out.println("准备向buffer中插入的chunk layer是1");
				buff.putChunk(c1);
				// 下载大小相加
				down_info_temp[0] = down_info_temp[0] + down_info[0];
				System.out.println("本次下载的文件总大小是:" + down_info_temp[0]);
				// 下载时间相加
				down_info_temp[1] = down_info_temp[1] + down_info[1];
				System.out.println("本次下载文件的总时间是:" + down_info_temp[1]);
				// 计算下载带宽(bit/ms)
				down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
				System.out.println("本次下载文件总带宽是:" + down_info_temp[2]);
				// 下载完毕以后更新策略信息
				policy.update(down_info_temp);
				// 更新策略中segment的信息
				policy.setSegID(Integer.parseInt(policy_result[0]));
				policy.setchunkLayer(Integer.parseInt(policy_result[1]));
				System.out.println("***********************一次下载结束***********************");
			}
		}
		// 如果策略决定下载l0层
		if (policy.getLayerNum() == 0) {
			// 请求l0
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp1_1Request_nopush(policy_result[2],
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
							+ ".svc");
			Chunk c0 = new Chunk(policy.getSegID(), policy.getLayerNum(), down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是0");
			buff.putChunk(c0);
			// 下载完毕更新策略信息
			policy.update(down_info);
			System.out.println("本次下载的文件总大小是:" + down_info[0]);
			System.out.println("本次下载文件的总时间是:" + down_info[1]);
			System.out.println("本次下载文件总带宽是:" + down_info[2]);
			// 更新策略中segment的信息
			policy.setSegID(Integer.parseInt(policy_result[0]));
			policy.setchunkLayer(Integer.parseInt(policy_result[1]));
			System.out.println("***********************一次下载结束***********************");
		}

	}

	// 不使用push下载HTTP2
	public void produce_item_nopush_HTTP_2() throws URISyntaxException, IOException, InterruptedException {

		String[] policy_result;
		// 下载信息(大小、时间、带宽)
		double[] down_info;
		// 每一次下载信息累加
		double[] down_info_temp = new double[3];
		// 先判断策略决定下载第几层
		// 如果不是l0层，则先将底层下载好

		policy_result = policy.nextLayerToDownloader();
		
		//记录大小 用作大小预测
		double sizeofL0 = 0;
		double sizeofL1 = 0;
		double sizeofL2 = 0;
		
		// 如果策略决定下载最高层
		// 则先下载最底层
		if (policy.getLayerNum() == 2) {
			// 请求l0层
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp_2Request_nopush(
					parse.getUrl_list_L0().get(String.valueOf(policy.getSegID())),
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy.getSegID() + "-L0.svc");
			Chunk c0 = new Chunk(policy.getSegID(), 0, down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是0");
			buff.putChunk(c0);
			
			// 如果是当前是需要大小预测的策略
			// 将下载块的大小放在相应的容器中用作大小预测
			sizeofL0 = down_info[0];
//			if ((policy.getName() == "BufferBasedPolicyWithServerPushAndSizePredict"
//					|| policy.getName() == "BufferBasedPolicyWithSizePredict")&&policy.getSegID() < 15) {
//				//先记录L0的大小
//				sizeofL0 = down_info[0];
//			}
			// 下载大小相加
			down_info_temp[0] = down_info_temp[0] + down_info[0];
			// 下载时间相加
			down_info_temp[1] = down_info_temp[1] + down_info[1];
			// 计算下载带宽(bit/ms)
			down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
			System.out.println("***********************一次下载结束***********************");

			// 请求l1层
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp_2Request_nopush(
					parse.getUrl_list_L1().get(String.valueOf(policy.getSegID())),MyPropertiesPro.getProperty("conf.svc_base_path") +  + policy.getSegID() + "-L1.svc");
			System.out.println("******测试测试测试下载文件为"+parse.getUrl_list_L1().get(String.valueOf(policy.getSegID())));
			System.out.println("******测试测试测试下载文件大小为"+down_info[0]);
			Chunk c1 = new Chunk(policy.getSegID(), 1, down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是1");
			buff.putChunk(c1);
			// 如果是当前是需要大小预测的策略
			// 将下载块的大小放在相应的容器中用作大小预测
			sizeofL1 = down_info[0];
//			if ((policy.getName() == "BufferBasedPolicyWithServerPushAndSizePredict"
//					|| policy.getName() == "BufferBasedPolicyWithSizePredict")&&policy.getSegID() < 15) {
//				sizeofL1 = down_info[0];
//			}
			// 下载大小相加
			down_info_temp[0] = down_info_temp[0] + down_info[0];
			// 下载时间相加
			down_info_temp[1] = down_info_temp[1] + down_info[1];
			// 计算下载带宽(bit/ms)
			down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];

			System.out.println("***********************一次下载结束***********************");

			// 请求最高层，按照策略返回的信息开始下载
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp_2Request_nopush(
					parse.getUrl_list_L2().get(String.valueOf(policy.getSegID())),MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
							+ ".svc");
			Chunk c2 = new Chunk(policy.getSegID(), policy.getLayerNum(), down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是2");
			buff.putChunk(c2);
			// 如果是当前是需要大小预测的策略
			// 将下载块的大小放在相应的容器中用作大小预测
			sizeofL2 = down_info[0];
//			if ((policy.getName() == "BufferBasedPolicyWithServerPushAndSizePredict"
//					|| policy.getName() == "BufferBasedPolicyWithSizePredict")&&policy.getSegID() < 15) {
//				sizeofL2 = down_info[0];
				Pair<Double> pair1 = new Pair<Double>();
				Pair<Double> pair2 = new Pair<Double>();
				//List<Pair<Double>> list = new ArrayList<Pair<Double>>();
				
				pair1.setFirst(sizeofL0);
				pair1.setSecond(sizeofL1);
				pair2.setFirst(sizeofL0);
				pair2.setSecond(sizeofL2);
				
				System.out.println("sizeofL0:"+sizeofL0);
				System.out.println("sizeofL1:"+sizeofL1);
				System.out.println("sizeofL2:"+sizeofL2);
				
				SizePredictor.getList1().add(pair1);
				SizePredictor.getList2().add(pair2);

//			}
			// 下载大小相加
			down_info_temp[0] = down_info_temp[0] + down_info[0];
			System.out.println("本次下载的文件总大小是:" + down_info_temp[0]);
			// 下载时间相加
			down_info_temp[1] = down_info_temp[1] + down_info[1];
			System.out.println("本次下载文件的总时间是:" + down_info_temp[1]);
			// 计算下载带宽(bit/ms)
			down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
			System.out.println("本次下载文件的总时间是:" + down_info_temp[2]);

			// 下载完毕以后更新策略信息
			policy.update(down_info_temp);
			// 更新策略中segment的信息
			policy.setSegID(Integer.parseInt(policy_result[0]));
			policy.setchunkLayer(Integer.parseInt(policy_result[1]));
			System.out.println("***********************一次下载结束***********************");

		}
		// 如果策略决定下载l1层
		if (policy.getLayerNum() == 1)

		{
			// 请求l0
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp_2Request_nopush(
					parse.getUrl_list_L0().get(String.valueOf(policy.getSegID())),
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy.getSegID() + "-L0.svc");
			if (down_info == null) {
				System.out.println("video play end");

			} else {
				Chunk c0 = new Chunk(policy.getSegID(), 0, down_info[0]);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c0);
				
				// 下载大小相加
				down_info_temp[0] = down_info_temp[0] + down_info[0];
				// 下载时间相加
				down_info_temp[1] = down_info_temp[1] + down_info[1];
				// 计算下载带宽(bit/ms)
				down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
				System.out.println("***********************一次下载结束***********************");

				// 请求l1
				System.out.println("***********************一次下载开始***********************");
				down_info = http2client.Sendhttp_2Request_nopush(policy_result[2],
						MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
								+ ".svc");
				Chunk c1 = new Chunk(policy.getSegID(), policy.getLayerNum(), down_info[0]);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
				System.out.println("准备向buffer中插入的chunk layer是1");
				buff.putChunk(c1);
				// 如果是当前是需要大小预测的策略
				// 将下载块的大小放在相应的容器中用作大小预测
				
				// 下载大小相加
				down_info_temp[0] = down_info_temp[0] + down_info[0];
				System.out.println("本次下载的文件总大小是:" + down_info_temp[0]);
				// 下载时间相加
				down_info_temp[1] = down_info_temp[1] + down_info[1];
				System.out.println("本次下载文件的总时间是:" + down_info_temp[1]);
				// 计算下载带宽(bit/ms)
				down_info_temp[2] = (down_info_temp[0] * 8) / down_info_temp[1];
				System.out.println("本次下载文件总带宽是:" + down_info_temp[2]);
				// 下载完毕以后更新策略信息
				policy.update(down_info_temp);
				// 更新策略中segment的信息
				policy.setSegID(Integer.parseInt(policy_result[0]));
				policy.setchunkLayer(Integer.parseInt(policy_result[1]));
				System.out.println("***********************一次下载结束***********************");
			}
		}
		// 如果策略决定下载l0层
		if (policy.getLayerNum() == 0) {
			// 请求l0
			System.out.println("***********************一次下载开始***********************");
			down_info = http2client.Sendhttp_2Request_nopush(policy_result[2],
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
							+ ".svc");
			Chunk c0 = new Chunk(policy.getSegID(), policy.getLayerNum(), down_info[0]);
			System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID());
			System.out.println("准备向buffer中插入的chunk layer是0");
			buff.putChunk(c0);
			
			// 下载完毕更新策略信息
			policy.update(down_info);
			System.out.println("本次下载的文件总大小是:" + down_info[0]);
			System.out.println("本次下载文件的总时间是:" + down_info[1]);
			System.out.println("本次下载文件总带宽是:" + down_info[2]);
			// 更新策略中segment的信息
			policy.setSegID(Integer.parseInt(policy_result[0]));
			policy.setchunkLayer(Integer.parseInt(policy_result[1]));
			System.out.println("***********************一次下载结束***********************");
		}
	}

	// 使用push下载
	public void produce_item_push() throws URISyntaxException, IOException, InterruptedException {
		// 记录policy结果：ID、layer、url
		String[] policy_result = new String[3];
		// 记录下载信息：时间、文件大小、带宽
		double[] down_info = new double[3];
		// 记录要push的url
		String push_list = null;
		// 调用策略
		policy_result = policy.nextLayerToDownloader();

		// 根据策略决定要初始化几个块
		// 应该先初始化L0层的chunk
		// 如果策略决定下载最高层
		if (policy.getLayerNum() == 2) {
			// 将要push的部分，即l0和l2的url保存在string中
			push_list = MyPropertiesPro.getProperty("conf.pushList") + policy.getSegID() + "-L0.svc" + ","
					+ MyPropertiesPro.getProperty("conf.pushList") + policy.getSegID() + "-L1.svc";
			
			System.out.println("*************push_list是:" + push_list);
			// 调用push下载函数
			down_info = http2client.Sendhttp2Request_push(policy_result[2],
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
							+ ".svc",
					push_list);
            // 
			// 先初始化l0层的chunk
			System.out.println("初始化一个L0层的chunk");
			// chunkID由策略传入。layer为0
			Chunk c0 = new Chunk(Integer.parseInt(policy_result[0]), 0);
			System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
			System.out.println("准备向buffer中插入的chunk layer是0");
			buff.putChunk(c0);
			System.out.println("在buffer中插入L0层chunk成功");

			// 初始化l1层的chunk
			System.out.println("初始化一个L1层的chunk");
			// chunkID由策略传入。layer为1
			Chunk c1 = new Chunk(Integer.parseInt(policy_result[0]), 1);
			System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
			System.out.println("准备向buffer中插入的chunk layer是1");
			buff.putChunk(c1);
			System.out.println("在buffer中插入L1层chunk成功");

			// 初始化l2层的chunk
			System.out.println("初始化一个L2层的chunk");
			// chunkID由策略传入。layer为1
			Chunk c2 = new Chunk(Integer.parseInt(policy_result[0]), 2);
			System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
			System.out.println("准备向buffer中插入的chunk layer是2");
			buff.putChunk(c2);
			System.out.println("在buffer中插入L2层chunk成功");
			
			System.out.println("本次下载的文件总大小是:" + down_info[0]);
			System.out.println("本次下载文件的总时间是:" + down_info[1]);
			System.out.println("本次下载文件的总带宽是:" + down_info[2]);

			// 更新策略信息
			policy.update(down_info);
			policy.setSegID(Integer.parseInt(policy_result[0]));
			// 因为请求了最高层
			policy.setchunkLayer(2);
		}

		// 如果策略决定下载l1层
		if (policy.getLayerNum() == 1) {

			// 将要push的部分，即l0的url保存在数组中
			push_list = MyPropertiesPro.getProperty("conf.pushList") + policy.getSegID() + "-L0.svc";
			// push_list = parse.getUrl_list_L0().get(String.valueOf(policy.getSegID()));
			System.out.println("*************push_list是:" + push_list);
			// 调用push下载函数
			down_info = http2client.Sendhttp2Request_push(policy_result[2],
					MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
							+ ".svc",
					push_list);
			// 先初始化l0层的chunk
			System.out.println("初始化一个L0层的chunk");
			// chunkID由策略传入。layer为0
			Chunk c0 = new Chunk(Integer.parseInt(policy_result[0]), 0);
			System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
			System.out.println("准备向buffer中插入的chunk layer是0");
			buff.putChunk(c0);
			System.out.println("在buffer中插入L0层chunk成功");

			// 初始化l1层的chunk
			System.out.println("初始化一个L1层的chunk");
			// chunkID由策略传入。layer为1
			Chunk c1 = new Chunk(Integer.parseInt(policy_result[0]), 1);
			System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
			System.out.println("准备向buffer中插入的chunk layer是1");
			buff.putChunk(c1);
			System.out.println("在buffer中插入L1层chunk成功");

			// 更新策略信息
			policy.update(down_info);
			policy.setSegID(Integer.parseInt(policy_result[0]));
			// 因为请求了l1层
			policy.setchunkLayer(1);
		}

		// 如果仅仅请求l0层
		if (policy.getLayerNum() == 0) {
			// 判断一下是不是bufferbasedpolicy中buffer小于最小阈值需要连续push的情况
			if ((policy.getName() == "BufferBasedWithServerPushPolicy"||policy.getName() == "BufferBasedPolicyWithServerPushAndSizePredict")
					&& buff.getAvail_seg() < Integer.parseInt(MyPropertiesPro.getProperty("conf.min_buffer"))) {
				push_list = MyPropertiesPro.getProperty("conf.pushList") + (policy.getSegID() + 1) + "-L0.svc" + ","
						+ MyPropertiesPro.getProperty("conf.pushList") + (policy.getSegID() + 2) + "-L0.svc" + ","
						+ MyPropertiesPro.getProperty("conf.pushList") + (policy.getSegID() + 3) + "-L0.svc" + ","
						+ MyPropertiesPro.getProperty("conf.pushList") + (policy.getSegID() + 4) + "-L0.svc" + ","
						+ MyPropertiesPro.getProperty("conf.pushList") + (policy.getSegID() + 5) + "-L-.svc";
				// 调用push下载函数
				down_info = http2client.Sendhttp2Request_push(policy_result[2],
						MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
								+ ".svc",
						push_list);
				// 初始化5个l0层的segment
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				// 原始请求
				Chunk c0 = new Chunk(Integer.parseInt(policy_result[0]), 0);
				System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c0);
				System.out.println("在buffer中插入L0层chunk成功");
				// 第1个
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				Chunk c1 = new Chunk(policy.getSegID() + 1, 0);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID() + 1);
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c1);
				System.out.println("在buffer中插入L0层chunk成功");
				// 第2个
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				Chunk c2 = new Chunk(policy.getSegID() + 2, 0);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID() + 2);
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c2);
				System.out.println("在buffer中插入L0层chunk成功");
				// 第3个
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				Chunk c3 = new Chunk(policy.getSegID() + 3, 0);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID() + 3);
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c3);
				System.out.println("在buffer中插入L0层chunk成功");
				// 第4个
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				Chunk c4 = new Chunk(policy.getSegID() + 4, 0);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID() + 4);
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c4);
				System.out.println("在buffer中插入L0层chunk成功");
				// 第5个
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				Chunk c5 = new Chunk(policy.getSegID() + 5, 0);
				System.out.println("准备向buffer中插入的chunk id是" + policy.getSegID() + 5);
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c5);
				System.out.println("在buffer中插入L0层chunk成功");

				// 更新策略信息
				policy.update(down_info);
				policy.setSegID(policy.getSegID() + 5);
				// 因为请求了l0层
				policy.setchunkLayer(0);

			}
			// 否则，不是BufferBasedWithServerPushPolicy策略中buffer小于最小阈值的情况
			// 正常请求一个l0层的segment
			// 调用push下载函数
			else {
//			down_info = http2client.Sendhttp2Request_push(policy_result[2],
//					MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L"
//							+ policy_result[1] + ".svc",
//					push_list);
				down_info = http2client.Sendhttp_2Request_nopush(policy_result[2],
						MyPropertiesPro.getProperty("conf.svc_base_path") + policy_result[0] + "-L" + policy_result[1]
								+ ".svc");
				// 只初始化l0层的chunk
				System.out.println("初始化一个L0层的chunk");
				// chunkID由策略传入。layer为0
				Chunk c0 = new Chunk(Integer.parseInt(policy_result[0]), 0);
				System.out.println("准备向buffer中插入的chunk id是" + Integer.parseInt(policy_result[0]));
				System.out.println("准备向buffer中插入的chunk layer是0");
				buff.putChunk(c0);
				System.out.println("在buffer中插入L0层chunk成功");

				System.out.println("更新之前输出。下载带宽是：" + down_info[2]);
				// 更新策略信息
				policy.update(down_info);
				policy.setSegID(Integer.parseInt(policy_result[0]));
				// 因为请求了l0层
				policy.setchunkLayer(0);
			}
		}
	}

	// 下载线程
	public void run() {
		// 如果改为push的话，循环次数也要改，只需要循环segment个数次
		for (int i = 0; i < (Integer.parseInt(MyPropertiesPro.getProperty("conf.total_chunk"))
				- Integer.parseInt(MyPropertiesPro.getProperty("conf.ini_down"))); i++) {
			try {
				
				produce_item_push();
				//produce_item_nopush();
				//produce_item_nopush_HTTP_2();

			} catch (URISyntaxException | IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 下载测试
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		// 配置文件读取
		String pFilePath = "resources/config.properties";
		MyPropertiesPro mp = new MyPropertiesPro(pFilePath);

		Buffer buf = new Buffer(10);
		ParseMPD parse = new ParseMPD();
		HTTP2 http2client = new HTTP2();
		// AllofLayersPolicy policy = new AllofLayersPolicy(parse);
		// LayeredPushPolicy policy = new LayeredPushPolicy(parse);
		// Policy mypolicy = new Policy(new RateBasedwithServerPushPolicy(parse),
		// "RateBasedwithServerPushPolicy");
		Policy mypolicy = new Policy(new BufferBasedWithServerPushPolicy(parse, buf),
				"BufferBasedWithServerPushPolicy");
		Downloader down = new Downloader(buf, parse, http2client, mypolicy);
		// 测试下载mpd文件
		System.out.println("开始下载mpd文件");
		down.mpd_downloader_HTTP2(MyPropertiesPro.getProperty("conf.mpd_path"),
				"/Users/wuyue/Desktop/BBB-II-720p/BBB.mpd");
		System.out.println("mpd文件下载结束\n");
		// 测试初始化下载
		System.out.println("开始初始化下载");
		down.ini_downloader_push();
		// down.ini_downloader_nopush();
		System.out.println("初始化下载结束");
		// 测试使用策略下载
		for (int i = 0; i < 894; i++) {
			System.out.println("开始正常下载");
			down.produce_item_push();
			// down.produce_item_nopush();
			System.out.println("正常下载结束");
		}
	}
}
