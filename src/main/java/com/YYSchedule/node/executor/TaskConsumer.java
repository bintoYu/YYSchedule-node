/**
 * 
 */
package com.YYSchedule.node.executor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.common.utils.RpcUtils;
import com.YYSchedule.node.command.CommandHandler;
import com.YYSchedule.node.config.Config;
import com.YYSchedule.node.process.ProcessResult;
import com.YYSchedule.node.queue.TaskWaitingQueue;
import com.YYSchedule.store.ftp.FtpConnFactory;
import com.YYSchedule.store.ftp.FtpUtils;
import com.YYSchedule.store.util.ActiveMQUtils_nospring;
import com.YYSchedule.store.util.QueueConnectionFactory;

/**
 * @author ybt
 * 
 * @date 2018年7月27日
 * @version 1.0
 */
public class TaskConsumer implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskConsumer.class);
	
	private String nodeId;
	
	private Config config;
	
	//activemq
	private Connection activemqConnection;
	
	private Session activemqSession;
	
	private MessageConsumer taskConsumer;
	
	private String distributeTaskQueue;
	
	private TaskWaitingQueue taskQueue;
	/**
	 * @param distributeTaskQueue
	 * @param jmsTemplate
	 */
	public TaskConsumer(Config config, TaskWaitingQueue taskQueue)
	{
		super();
		this.config = config;
		this.taskQueue = taskQueue;
		this.nodeId = config.getLocal_listener_domain() + ":" + config.getTask_call_node_port();
		this.distributeTaskQueue = nodeId + ":" + "distributeTaskQueue";
		this.activemqConnection = QueueConnectionFactory.createActiveMQConnection(config.getActivemq_url());
		this.activemqSession = QueueConnectionFactory.createSession(activemqConnection);
		this.taskConsumer = QueueConnectionFactory.createConsumer(activemqSession, distributeTaskQueue);
	}
	
	@Override
	public void run()
	{
		String threadName = "TaskConsumer" + Thread.currentThread().getName().substring(Thread.currentThread().getName().length()-2);
		LOGGER.info("开启线程 " + threadName + "..........");
		while (!Thread.currentThread().isInterrupted()) {
			
			Task task = null;
			try {
				// 从队列distributeTaskQueue取出task
//				task = ActiveMQUtils.receiveTask(jmsTemplate, distributeTaskQueue);
				task = ActiveMQUtils_nospring.receiveTask(taskConsumer, distributeTaskQueue);
			} catch (JMSException e) {
				task.setTaskStatus(TaskStatus.ACCEPT_FAILED);
				LOGGER.error("Task [ " + task.getTaskId() + " ] --> " + distributeTaskQueue + " fail!" + e.getMessage());
			}
			
			if (task != null) {
				task.setTaskStatus(TaskStatus.ACCEPTED);
				
				//将task发到taskQueue中
				boolean isAdd = taskQueue.addToTaskQueue(task);
				if(isAdd)
				{
					LOGGER.info("Task [ " + task.getTaskId() + " ] --> taskWaitingQueue size: " + taskQueue.size());
					task.setTaskStatus(TaskStatus.WAITING);
				}
				// 通知taskmanager已接受task
				reportTaskStatus(task);
			}
		}
	}
	
	
	
	private void reportTaskStatus(Task task)
	{
		try {
			NodeCallTaskService.Client nodeCallTaskService = null;
			TProtocol tProtocol = RpcUtils.getTProtocol(config.getTaskmanager_listener_domain(), config.getNode_call_task_port(), config.getRpc_connect_timeout(), config.getRpc_connect_retry_times());
			nodeCallTaskService = new NodeCallTaskService.Client(tProtocol);
			nodeCallTaskService.reportTaskExecutionStatus(nodeId, task.getTaskId(), task.getTaskPhase(), task.getTaskStatus());
		} catch (Exception e) {
			LOGGER.error("无法将task [ " + task.getTaskId() + " ]更新为" + task.getTaskStatus() + ": " + e.getMessage(), e);
		}
	}
	


	
}
