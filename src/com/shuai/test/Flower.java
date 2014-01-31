package com.shuai.test;


public class Flower {
	int petalCount=0;
	String s="inital value";
	
	static int time=10;
	static String city=getCity();
	
	public Flower() {
		this(1);
		System.out.println("default");
		
	}
	
	public Flower(int count){
		petalCount=count;
		System.out.println("only int construct");
	}
	
	public Flower(int count,String name){
		petalCount=count;
		s=name;
	}
	
	
	
	public static String sing(){
		System.out.printf("asfsaf");
		return "sss";
	}
	
	public static String getCity(){
		return "house";
	}

	@Override
	public String toString() {
		return petalCount+" "+s;
	}
	
	
//	public static void main(String[] args){
//		Flower flower=new Flower();
////		return 0;
//	}

}

class BigFlower extends Flower{
	static String sayBig(){
		System.out.println("big!");
		return "big";
	}
}
