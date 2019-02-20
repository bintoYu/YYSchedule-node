/**
 * 
 */
package com.YYSchedule.node.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.Task;

/**
 * 
 * @author ybt
 *
 * @date 2019年1月11日  
 * @version 1.0
 */
@Component
@Scope("singleton")
public class TaskWaitingQueue
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskWaitingQueue.class);
	
	@Value("#{config.max_queue_size}")
	private int MAX_QUEUE_SIZE;
	
	private LinkedBlockingQueue<Task> taskQueue;
	
	@PostConstruct
	public void init()
	{
		taskQueue = new LinkedBlockingQueue<Task>(MAX_QUEUE_SIZE);
	}
	
	public synchronized LinkedBlockingQueue<Task> getTaskQueue()
	{
		return taskQueue;
	}
	
	public void addToTaskQueue(Task task)
	{
		try
		{
			taskQueue.put(task);
		} catch (InterruptedException e)
		{
			LOGGER.error("无法将task[" + task.getTaskId() + "] 放入TaskWaitingQueue中！");
			e.printStackTrace();
		}
	}
	
	public synchronized List<Long> getTaskIdList()
	{
		List<Long> taskIdList = new ArrayList<Long>();
		for (Task task : taskQueue)
		{
			taskIdList.add(task.getTaskId());
		}
		return taskIdList;
	}
	
	public synchronized int size()
	{
		return taskQueue.size();
	}
	
	public Task takeTask()
	{
		Task task = null;
		try
		{
			task = taskQueue.take(); 
		} catch (Exception e)
		{
			LOGGER.error("无法从TaskQueue中取出task" + e.getMessage(), e);
		}
		
		return task;
	}
	
}
