package wu.test.policy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import wu.test.policy.Pair;

public class SizePredictor {
	
	//key:segmentID
	//value:list分别按由低到高的顺序存放chunk大小
	//L0-L1层
//	private LinkedHashMap<Integer,List<Pair<Double>>> predictor1;
//	//L0-L2层
//	private LinkedHashMap<Integer,List<Pair<Double>>> predictor2;
	
	private static ArrayList<Pair<Double>> list1;
	private static ArrayList<Pair<Double>> list2;
	
	public SizePredictor()
	{
		SizePredictor.list1 = new ArrayList<Pair<Double>>();
		SizePredictor.list2 = new ArrayList<Pair<Double>>();
		
//		//L0-L1
//		LinkedHashMap <Integer,List<Pair<Double>>> predictor1 = new LinkedHashMap<>(10);
//		//L0-L2
//		LinkedHashMap <Integer,List<Pair<Double>>> predictor2 = new LinkedHashMap<>(10);
	}
	//线性回归预测
	public static double getPredictedNum(List<Pair<Double>> x,double j)
	{
		double avgL0 = 0,avgL1= 0,a,b,L1predict;
		double m = 0,n = 0;
		int len = x.size();
		for(int i = 0;i < len;i++)
		{
			avgL0 += x.get(i).getFirst();
			avgL1 += x.get(i).getSecond();
		}
		avgL0 = avgL0 / len;
		avgL1 = avgL1 / len;
		
		for(int i = 0;i < len;i++)
		{
			m += (x.get(i).getFirst() - avgL0)*(x.get(i).getSecond() - avgL1);
			n += (x.get(i).getFirst() - avgL0)*(x.get(i).getFirst() - avgL0);
		}
		b = m/n;
		a = avgL1 - b*avgL0;
		L1predict = a + b*j;
		return L1predict;
		
	}
	
	
	
	
	public static void main(String[] args)
	{
		
		//最里层开始初始化
//		HashMap<Integer,Double> map1 = new HashMap<Integer,Double>();
//		HashMap<Integer,Double> map2 = new HashMap<Integer,Double>();
//		HashMap<Integer,Double> map3 = new HashMap<Integer,Double>();
//		HashMap<Integer,Double> map4 = new HashMap<Integer,Double>();
//		HashMap<Integer,Double> map5 = new HashMap<Integer,Double>();
//		HashMap<Integer,Double> map6 = new HashMap<Integer,Double>();
		Pair<Double> pair1 = new Pair<Double>();
		Pair<Double> pair2 = new Pair<Double>();
		List<Pair<Double>> list = new ArrayList<Pair<Double>>();
		
		pair1.setFirst(1.0);
		pair1.setSecond(2.0);
		pair2.setFirst(2.0);
		pair2.setSecond(4.0);
		
		list.add(pair1);
		list.add(pair2);		
		System.out.println("线性回归预测值为："+getPredictedNum(list,5.0));
	}
	public static ArrayList<Pair<Double>> getList1() {
		return list1;
	}
	public static void setList1(ArrayList<Pair<Double>> list1) {
		SizePredictor.list1 = list1;
	}
	public static ArrayList<Pair<Double>> getList2() {
		return list2;
	}
	public static void setList2(ArrayList<Pair<Double>> list2) {
		SizePredictor.list2 = list2;
	}
	
}
