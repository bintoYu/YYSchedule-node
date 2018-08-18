package com.YYSchedule.node.test;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.YYSchedule.common.rpc.domain.task.TaskStatus;

/**
 * 
 */

/**
 * @author Administrator
 *
 * @date 2018年8月3日  
 * @version 1.0  
 */
public class B
{
	public static void main(String[] args)
	{
//		File file = new File("E:\\tmp\\new\\123\\1.txt");
//		//执行完毕后，将文件删除
//		try {
//			FileUtils.deleteDirectory(file.getParentFile());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		System.out.println(TaskStatus.FINISHED);
	}
}
