/**
 * 
 */
package com.YYSchedule.node.service.impl;

import java.io.File;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.YYSchedule.common.pojo.Result;
import com.YYSchedule.common.rpc.domain.engine.EngineLogger;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.node.EngineCallNodeService;
import com.YYSchedule.node.applicationContext.ApplicationContextHandler;
import com.YYSchedule.node.config.Config;
import com.YYSchedule.node.mapper.EngineLoggerMapper;
import com.YYSchedule.store.util.ActiveMQUtils_nospring;
import com.YYSchedule.store.util.QueueConnectionFactory;

/**
 * @author ybt
 *
 * @date 2018年8月4日  
 * @version 1.0  
 */
public class EngineCallNodeServiceImpl implements EngineCallNodeService.Iface
{
	private static final Logger LOGGER = LoggerFactory.getLogger(EngineCallNodeServiceImpl.class);

	@Override
	public int transferResult(long taskId, String file, String result, String logger,boolean isSuccess) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		ApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		Config config = applicationContext.getBean(Config.class);
		EngineLoggerMapper engineLoggerMapper = applicationContext.getBean(EngineLoggerMapper.class);
		engineLoggerMapper.removeEngineLogger(taskId);
		
		//将result封装成结果类发送到resultQueue中
		String resultQueue = config.getTaskmanager_listener_domain() + ":" + "resultQueue";
		//activemq
		Connection activemqConnection = QueueConnectionFactory.createActiveMQConnection(config.getActivemq_url());
		Session activemqSession = QueueConnectionFactory.createSession(activemqConnection);
		MessageProducer resultProducer = QueueConnectionFactory.createProducer(activemqSession, resultQueue);;
		Result resultPojo = new Result();
		resultPojo.setTaskId(taskId);
		resultPojo.setFileName(file.substring(file.lastIndexOf(File.separator)+1));
		resultPojo.setResult(result);
		resultPojo.setTaskPhase(TaskPhase.valueOf(config.getTask_phase()));
		resultPojo.setLogger(logger);
		resultPojo.setTaskStatus(isSuccess? TaskStatus.FINISHED : TaskStatus.FAILURE);
		resultPojo.setNodeId(config.getLocal_listener_domain() + ":" + config.getTask_call_node_port());
		resultPojo.setFinishedTime(System.currentTimeMillis());
		
		try
		{
//			ActiveMQUtils.sendResult(jmsTemplate, resultQueue, resultPojo);
			ActiveMQUtils_nospring.sendResult(resultPojo, activemqSession, resultProducer, resultQueue);
		}
		catch(JMSException jmsException)
		{
			LOGGER.error("result [ " + taskId + " ] 放入队列resultQueue失败！" + jmsException.getMessage());
			throw new TException("result [ " + taskId + " ] 放入队列resultQueue失败！" + jmsException.getMessage());
		}
		LOGGER.info("result [ " + taskId + " ] 已放入队列resultQueue中.");
		return 1;
		
	}


	@Override
	public int submitExecutionLogger(long taskId,String file, String content) throws InvalidRequestException, UnavailableException, TimeoutException, TException
	{
		EngineLogger engineLogger = new EngineLogger();
		engineLogger.setTaskId(taskId);
		engineLogger.setFileName(file);
		engineLogger.setContent(content);
		
		ApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		EngineLoggerMapper engineLoggerMapper = applicationContext.getBean(EngineLoggerMapper.class);
		engineLoggerMapper.updateEngineLogger(engineLogger);
		
		return 1;
	}

	
}
