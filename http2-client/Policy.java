package wu.test;

public class Policy {
	
	//持有对策略类的引用
	private SchedulePolicy policy;
	private String name;
	
	public Policy(SchedulePolicy policy,String name) {
		this.policy = policy;
		this.setName(name);
	}
	
	public String[] nextLayerToDownloader() {
		return this.policy.nextLayerToDownloader();
	}

	public void update(double[] temp) {
		this.policy.update(temp);
	}
	
	public int getSegID() {
		return this.policy.getSegID();
	}
	
	public int getLayerNum(){
		return this.policy.getLayerNum();
	}
	
	public double getSize() {
		return this.policy.getSize();
	}
	
	public int getPreviousLayer() {
		return this.policy.getPreviousLayer();
	}
	
	public void setSegID(int segID) {
		this.policy.setSegID(segID);
	}
	
	public void setchunkLayer(int chunkLayer) {
		this.policy.setchunkLayer(chunkLayer); 
	}
	
	public double getLast_down_bandwidth() {
		return this.policy.getLast_down_bandwidth();
	}
	
	public void setLast_down_bandwidth(double last_down_bandwidth) {
		this.policy.setLast_down_bandwidth(last_down_bandwidth);
	}
	
	public double getLast_down_time() {
		return policy.getLast_down_time();
	}
	
	public void setLast_down_time(double last_down_time) {
		this.policy.setLast_down_time(last_down_time);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
