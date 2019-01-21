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
public class TaskExecutor implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);
	
	private String nodeId;
	
	private FtpConnFactory ftpConnFactory;
	
	private Config config;
	
	//activemq
	private Connection activemqConnection;
	
	private Session activemqSession;
	
	private MessageProducer resultProducer;
	
	private String resultQueue;
	
	private TaskWaitingQueue taskQueue;
	/**
	 * @param distributeTaskQueue
	 * @param jmsTemplate
	 */
	public TaskExecutor(Config config, FtpConnFactory ftpConnFactory,TaskWaitingQueue taskQueue)
	{
		super();
		this.config = config;
		this.ftpConnFactory = ftpConnFactory;
		this.taskQueue = taskQueue;
		this.nodeId = config.getLocal_listener_domain() + ":" + config.getTask_call_node_port();
		this.resultQueue = config.getTaskmanager_listener_domain() + ":" + "resultQueue"; 
		this.activemqConnection = QueueConnectionFactory.createActiveMQConnection(config.getActivemq_url());
		this.activemqSession = QueueConnectionFactory.createSession(activemqConnection);
		this.resultProducer = QueueConnectionFactory.createProducer(activemqSession, resultQueue);
	}
	
	@Override
	public void run()
	{
		LOGGER.info(taskQueue.toString());
		String threadName = "TaskExecutor" + Thread.currentThread().getName().substring(Thread.currentThread().getName().length()-2);
		LOGGER.info("开启线程" + threadName + "..........");
		while (!Thread.currentThread().isInterrupted()) {
			
			Task task = null;
			
			// 从队列TaskQueue取出task
			task = taskQueue.takeTask();
			
			if (task != null) {

				LOGGER.info("Task [ " + task.getTaskId() + " ]: " + "distributeTaskQueue --> taskWaitingQueue");
				
				task.setTaskStatus(TaskStatus.RUNNING);
				// 通知taskmanager已接受task
				reportTaskStatus(task);
				
				// 下载文件到本地
				String localFilePath = download(task.getFileName(), task.getTaskId());
				
				if(localFilePath != null)
				{
					LOGGER.info("下载文件" + localFilePath + "成功！");
					
					// 执行task
					CommandHandler commandHandler = new CommandHandler(config);
					ProcessResult result = null;
					try {
						result = commandHandler.launch(task, localFilePath);
					} catch (ExecutionException e) {
						LOGGER.error("无法执行task [ " + task.getTaskId() + " ]");
					}
				
					/**
					 * 当程序执行中断或者超时，程序并不会调用接口向任务节点node返结果,控制节点便无法获知任务执行情况
					 * 因此需要在此由任务节点直接返回失败结果给控制节点
					 */
					if(result.getTaskStatus() != TaskStatus.FINISHED)
					{
						//向控制节点taskmanager发送失败任务
						transferFailureResult(task.getTaskId(),result.getTaskStatus());
					}
				}
				else
				{
					LOGGER.info("task[" + task.getTaskId() + "]" + "无文件可下载！");
					task.setTaskStatus(TaskStatus.FAILURE);
					reportTaskStatus(task);
				}
			}
		}
	}
	
	private String download(String remoteFilePath, long taskId)
	{
		FTPClient ftpClient = null;
		String localFilePath = null;
		for (int tryTime = 0; tryTime <= 2 && (localFilePath == null || !new File(localFilePath).exists()); tryTime++) 
		{
			try {
				if (ftpClient == null || !ftpClient.isConnected() || !ftpClient.isAvailable()) {
					ftpClient = ftpConnFactory.connect();
				}
				if (FtpUtils.isFileExist(ftpClient, remoteFilePath)) {
					File file = new File(config.getExecution_dir());
					localFilePath = FtpUtils.download(ftpClient, remoteFilePath, file.getParent() + File.separator + taskId);
					if (!new File(localFilePath).exists()) {
						LOGGER.error("下载文件 [ " + remoteFilePath + " ] 失败！ 文件未存在！");
					}
					file = null;
					break;
				}
			} catch (Exception e) {
				LOGGER.error("下载文件 [ " + remoteFilePath + " ] 失败！尝试重新连接ftp服务器..." + e.getMessage(), e);
				if (ftpClient != null) {
					try {
						ftpClient.disconnect();
					} catch (IOException ioe) {
						LOGGER.error("无法关闭ftp连接 : " + ioe.getMessage(), ioe);
					}
				}
				ftpClient = null;
				try {
					ftpClient = ftpConnFactory.connect();
				} catch (Exception e1) {
					ftpClient = null;
					LOGGER.error("无法连接ftp服务器 [ " + ftpConnFactory.getFtpHost() + " ] : " + e1.getMessage(), e1);
				}
			}
		}
		
		if(ftpClient.isConnected())
		{
			try
			{
				ftpClient.disconnect();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return localFilePath;
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
	

	private void transferFailureResult(long taskId, TaskStatus taskStatus)
	{
		Result resultPojo = new Result();
		resultPojo.setTaskId(taskId);
		resultPojo.setFinishedTime(System.currentTimeMillis());
		resultPojo.setNodeId(nodeId);
		resultPojo.setTaskStatus(taskStatus);
		 
		try
		{
//			ActiveMQUtils.sendResult(jmsTemplate, resultQueue, resultPojo);
			ActiveMQUtils_nospring.sendResult(resultPojo, activemqSession, resultProducer, resultQueue);
		}
		catch(JMSException jmsException)
		{
			LOGGER.error("失败信息 [ " + taskId + " ] 放入队列resultQueue失败！" + jmsException.getMessage());
		}
		LOGGER.info("失败信息 [ " + taskId + " ] 已放入队列resultQueue中.");
	}
	
}
