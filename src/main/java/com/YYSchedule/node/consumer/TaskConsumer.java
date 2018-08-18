/**
 * 
 */
package com.YYSchedule.node.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.node.config.Config;
import com.YYSchedule.store.ftp.FtpConnFactory;

/**
 * @author ybt
 *
 * @date 2018年8月2日  
 * @version 1.0  
 */
@Component("TaskConsumer")
public class TaskConsumer
{
	@Autowired
	private Config config;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private FtpConnFactory ftpConnFactory;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	
	public void startThreadPool()
	{
		int task_consumer_thread_num = config.getTask_consumer_thread_num();
		
		for(int i = 0; i < task_consumer_thread_num; i++)
		{
			TaskConsumerThread taskConsumerThread = new TaskConsumerThread(config, jmsTemplate, ftpConnFactory);
			threadPoolExecutor.execute(taskConsumerThread);
		}
	}
}
