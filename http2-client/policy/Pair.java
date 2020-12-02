package wu.test.policy;

public class Pair<Double> {
	
	private double first;
	private double second;

	public Pair() {
		first = 0;
		second = 0;
	}

	public Pair(double first, double second) {
		this.first = first;
		this.second = second;
	}

	public double getFirst() {
		return first;
	}

	public double getSecond() {
		return second;
	}

	public void setFirst(double newValue) {
		first = newValue;
	}

	public void setSecond(double newValue) {
		second = newValue;
	}
}
