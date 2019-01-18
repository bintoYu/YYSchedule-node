/**
 * 
 */
package com.YYSchedule.node.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

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
	
	private PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<Task>();
	
	
	public synchronized PriorityBlockingQueue<Task> getTaskQueue()
	{
		return taskQueue;
	}
	
	public synchronized boolean addToTaskQueue(Task task)
	{
		boolean isAdded = false;
		if(task == null)
		{
			return false;
		}
		
		if (taskQueue.size() <= MAX_QUEUE_SIZE - 2)
		{
			isAdded = taskQueue.add(task);
		}
		else
		{
			LOGGER.error("TaskQueue超过最大容量, size : [ " + taskQueue.size() + " ].");
		}
		
		return isAdded;
	}
	
	public synchronized void addToTaskQueue(Set<Task> taskSet)
	{
		if(taskSet.isEmpty())
		{
			return ;
		}
		
		if (taskQueue.size() <= MAX_QUEUE_SIZE - taskSet.size() - 1)
		{
			boolean isAdded = taskQueue.addAll(taskSet);
			if (isAdded)
			{
				LOGGER.info("成功更新TaskQueue, size : [ " + taskQueue.size() + " ].");
			}
		}
		else
		{
			LOGGER.error("TaskQueue超过最大容量, size : [ " + taskQueue.size() + " ].");
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
