/**
 * 
 */
package com.YYSchedule.node.test;

import java.util.BitSet;

/**
 * @author Administrator
 *
 * @date 2018年7月31日  
 * @version 1.0  
 */
public class Test1
{
	 public static void main(String[] args)
	{
		 BitSet processFlagSet = new BitSet(3);
		 
		 processFlagSet.set(0);
		 processFlagSet.set(2);
		 
//		 System.out.println(processFlagSet.toString());
		 System.out.println(processFlagSet.get(0));
	}
}
