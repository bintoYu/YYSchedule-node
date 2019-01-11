/**
 * 
 */
package com.YYSchedule.node.test;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.job.JobPriority;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.node.applicationContext.ApplicationContextHandler;
import com.YYSchedule.store.util.ActiveMQUtils;

/**
 * @author ybt
 *
 * @date 2018年7月18日  
 * @version 1.0  
 */
public class ActivemqTest
{
	private AbstractApplicationContext applicationContext;
	
	private JmsTemplate jmsTemplate;
	
	private String queue = "192.168.101.51:7000:distributeTaskQueue";
	
	@Before
	public void init()
	{
		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		
		jmsTemplate = (JmsTemplate) applicationContext.getBean("jmsTemplate");
	}
	
//	@Test
//	public void sendTask()
//	{
//		Long taskId = (long) 1;
//		for(int i = 0; i < 2; i++)
//		{
//			Task task = new Task();
//			task.setTaskId(taskId);
//			task.setTaskPhase(TaskPhase.COMMON);
//
//			taskId++;
//			ActiveMQUtils.sendTask(jmsTemplate, queue, task,JobPriority.HIGHER.getValue());
//		
//		}
//		
//		long queueSize = ActiveMQUtils.getQueueSize(jmsTemplate, "192.168.101.59:7000:distributeTaskQueue");
//		System.out.println(queueSize);
//	}
	

	
//	@Test
//	public void getQueueSize()
//	{
//		long queueSize = ActiveMQUtils.getQueueSize(jmsTemplate, "192.168.101.51:7000:distributeTaskQueue");
//		System.out.println(queueSize);
//		
////		long queueSize1 = ActiveMQUtils.getQueueSize(jmsTemplate, "test1");
////		System.out.println(queueSize1);
//		
//
//	}
//	
	@Test
	public void receiveTask()
	{
		Task receiveTask;
		jmsTemplate = (JmsTemplate) applicationContext.getBean("jmsTemplate");
		
		try {
			System.out.println(queue);
			receiveTask = ActiveMQUtils.receiveTask(jmsTemplate, queue);
			System.out.println(receiveTask.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
