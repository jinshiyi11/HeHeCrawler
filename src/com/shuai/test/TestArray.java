package com.shuai.test;

public class TestArray {

	public static void main(String[] args) {
		String a1="xxxx";
		String a2=a1;
		a2="yyyy";
		System.out.println("a1:"+a1);
		System.out.println("a2:"+a2);
		
		
		String[] infos={"aaa","bbb","ccc"};
		Other.main1(new String[]{"aaa","bbb","ccc"});
	}
}
	
	class Other{
		public static void main1(String[] args) {
			for(String s:args){
				System.out.println(s);
			}
		}
	}

//}
