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
@Component("TaskExecutorPool")
public class TaskExecutorPool
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
		int executor_num = config.getExecutor_num();
		
		for(int i = 0; i < executor_num; i++)
		{
			TaskExecutor taskExecutor = new TaskExecutor(config, ftpConnFactory, taskQueue);
			threadPoolExecutor.execute(taskExecutor);
		}
	}
}
