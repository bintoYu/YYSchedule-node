/**
 * 
 */
package com.YYSchedule.node.test;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.rpc.domain.container.Context;
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
	
	private String queue = "test";
	
	@Before
	public void init()
	{
		applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		
		jmsTemplate = (JmsTemplate) applicationContext.getBean("jmsTemplate");
	}
	
	@Test
	public void sendContext()
	{
		Long taskId = (long) 1;
		for(int i = 0; i < 2; i++)
		{
			Context context = new Context();
			context.setTaskId(taskId);
			context.setPriority(JobPriority.MEDIUM);
			context.setTaskPhase(TaskPhase.COMMON);
			context.setProgramId(taskId);
			context.setProgramName("test");
			context.setScriptName("test");
			context.setScriptPath("test");
			context.setScriptMd5("test");
			context.setExecutableName("test");
			context.setExecutablePath("test");
			context.setExecutableMd5("test");
			taskId++;
			ActiveMQUtils.sendContext(jmsTemplate, queue, context,JobPriority.HIGHER.getValue());
		
		}
		
		long queueSize = ActiveMQUtils.getQueueSize(jmsTemplate, "test");
		System.out.println(queueSize);
		
		long queueSize1 = ActiveMQUtils.getQueueSize(jmsTemplate, "test");
		System.out.println(queueSize1);
	}
	
	@Test
	public void receiveContext()
	{
		Context receiveContext;
		try {
			receiveContext = ActiveMQUtils.receiveContext(jmsTemplate, queue);
			System.out.println(receiveContext.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void getQueueSize()
	{
		System.out.println(jmsTemplate.hashCode());
		
		long queueSize = ActiveMQUtils.getQueueSize(jmsTemplate, "test");
		System.out.println(queueSize);
		
//		long queueSize1 = ActiveMQUtils.getQueueSize(jmsTemplate, "test1");
//		System.out.println(queueSize1);
		
		jmsTemplate = (JmsTemplate) applicationContext.getBean("jmsTemplate");
		
		System.out.println(jmsTemplate.hashCode());
		
		Context receiveContext;
		try {
			receiveContext = ActiveMQUtils.receiveContext(jmsTemplate, queue);
			System.out.println(receiveContext.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
