/**
 * 
 */
package com.YYSchedule.node.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.YYSchedule.node.config.Config;
import com.YYSchedule.node.queue.TaskWaitingQueue;
import com.YYSchedule.store.ftp.FtpConnFactory;

/**
 * @author ybt
 *
 * @date 2018年8月2日  
 * @version 1.0  
 */
@Component("TaskConsumerPool")
public class TaskConsumerPool
{
	@Autowired
	private Config config;
	
	@Autowired
	private FtpConnFactory ftpConnFactory;
	
	@Autowired
	private ThreadPoolTaskExecutor threadPoolExecutor;
	
	@Autowired
	private TaskWaitingQueue taskQueue;
	
	public void startThreadPool()
	{
		int task_consumer_thread_num = config.getTask_consumer_num();
		
		for(int i = 0; i < task_consumer_thread_num; i++)
		{
			TaskConsumer taskConsumerThread = new TaskConsumer(config, taskQueue);
			threadPoolExecutor.execute(taskConsumerThread);
		}
	}
}
