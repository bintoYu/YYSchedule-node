/**
 * 
 */
package com.YYSchedule.node.queue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.pojo.Task;

/**
 * Priority Task Queue Producer
 * 		thread for generating and keeping global task queue
 * @author yanwei
 * @date 2013-1-6 上午11:22:20
 * 
 */
public class GlobalTaskQueueProducer implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTaskQueueProducer.class);
	
	private final BlockingQueue<Task> queue;
	
	private Task task;
	
	private List<Task> taskList;
	
	private Queue taskQueue;
	
	/**
	 * 
	 */
	public GlobalTaskQueueProducer(Queue taskQueue) {
		this.taskQueue = taskQueue;
		this.queue = taskQueue.getGlobalTaskQueue();
	}
	
	public GlobalTaskQueueProducer(Queue taskQueue, Task task) {
		this.taskQueue = taskQueue;
		this.queue = taskQueue.getGlobalTaskQueue();
		this.task = task;
	}
	
	public GlobalTaskQueueProducer(Queue taskQueue, List<Task> taskList) {
		this.taskQueue = taskQueue;
		this.queue = taskQueue.getGlobalTaskQueue();;
		System.out.println("priority:" + taskQueue);
		this.taskList = taskList;
		taskList = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Priority task queue producer start ...");
		Set<Task> taskSet = produce();
		
		if(taskSet != null && taskSet.size() != 0 && queue.addAll(taskSet)) {
			LOGGER.info("Succeed to update global task queue, size : [ " + queue.size() + " ].");
		} else {
			LOGGER.info("No task will be added to global task queue, size : [ " + queue.size() + " ].");
		}
		taskSet = null;
	}

	/**
	 * 将task或taskList存入PriorityTaskQueue中
	 * @return
	 */
	private Set<Task> produce() {
		
		Set<Task> taskSet = new HashSet<Task>();
		
		// get upper bound of task num to be added
		int limit = taskQueue.getMaxQueueSize();
		if (task != null && !taskQueue.getDistributeTaskIdList().contains(task.getTaskId())) {
			limit = limit - queue.size() - 1;
			taskSet.add(task);
		} else if (taskList != null && taskList.size() != 0) {
			limit = limit - queue.size() - taskList.size();
			taskSet.addAll(taskList);
		}
		return taskSet;
	}

	/**
	 * 
	 */
	public void shutdown() {
		Thread.currentThread().interrupt();
	}
}
