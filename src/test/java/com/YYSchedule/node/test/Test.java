/**
 * 
 */
package com.YYSchedule.node.test;

import com.YYSchedule.common.pojo.Task;
import com.alibaba.fastjson.JSONObject;

/**
 * @author Administrator
 *
 * @date 2018年10月11日 
 * @version 1.0  
 */
public class Test
{
	public static void main(String[] args)
	{
		Task task1 = null;
		Task task2 = new Task();
		task2.setTaskId(1111L);
		task2.setExecutorId("test");
		
		String json = JSONObject.toJSONString(task2);
		System.out.println(json);
		task1 = JSONObject.parseObject(json,Task.class);
		System.out.println(task1);
	}
}
