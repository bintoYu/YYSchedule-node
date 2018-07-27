/**
 * 
 */
package com.YYSchedule.node.start;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.YYSchedule.common.rpc.domain.node.NodePayload;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.node.applicationContext.ApplicationContextHandler;
import com.YYSchedule.node.config.Config;
import com.YYSchedule.node.detector.HeartBeatDetector;
import com.YYSchedule.node.utils.RpcUtils;

/**
 * @author ybt
 *
 * @date 2018年7月11日  
 * @version 1.0  
 */
public class StartUp
{
	private static final Logger LOGGER = LoggerFactory.getLogger(StartUp.class);
	
	private AbstractApplicationContext applicationContext;
	
	private Config config;
	
	public StartUp(AbstractApplicationContext applicationContext)
	{
		super();
		this.applicationContext = applicationContext;
		this.config = applicationContext.getBean(Config.class);
	}
	
	/**
	 * register node to taskmanager
	 */
	public void registerNode() {	
		
		NodePayload nodePayload = applicationContext.getBean(HeartBeatDetector.class).generateHeartBeat();
		
		NodeCallTaskService.Client nodeCallTaskService = null;
		try {
			LOGGER.info("register node to : [ " + config.getTaskmanager_listener_domain() + ":" + config.getNode_call_task_port() + " ] timeout: " + config.getRpc_connect_timeout());
			TProtocol tProtocol = RpcUtils.getTProtocol(config.getTaskmanager_listener_domain(), config.getNode_call_task_port(), config.getRpc_connect_timeout(), config.getRpc_connect_retry_times());
			nodeCallTaskService = new NodeCallTaskService.Client(tProtocol);
			if (nodeCallTaskService.registerNode(nodePayload) != 0) {
				LOGGER.error("Failed to register node [ " + nodePayload.getNodeId() + " ] to master [ " + config.getTaskmanager_listener_domain() + ":" + config.getNode_call_task_port()
						+ " ]");
				throw new UnavailableException("Failed to register node [ " + nodePayload.getNodeId() + " ] to master [ " + config.getTaskmanager_listener_domain() + ":"
						+ config.getNode_call_task_port() + " ]");
			}
		} catch (InvalidRequestException e) {
			LOGGER.error("Failed to register node [ " + nodePayload.getNodeId() + " ] : " + e.getMessage());
		} catch (UnavailableException e) {
			LOGGER.error("Failed to register node [ " + nodePayload.getNodeId() + " ] : " + e.getMessage());
		} catch (TimeoutException e) {
			LOGGER.error("Failed to register node [ " + nodePayload.getNodeId() + " ] : " + e.getMessage());
		} catch (TException e) {
			LOGGER.error("Failed to register node [ " + nodePayload.getNodeId() + " ] : " + e.getMessage());
		} finally {
			RpcUtils.close(nodeCallTaskService);
		}
	}
	
	
	public static void main(String[] args)
	{
		AbstractApplicationContext applicationContext = ApplicationContextHandler.getInstance().getApplicationContext();
		StartUp startUp = new StartUp(applicationContext);
		startUp.registerNode();
		
	}
}
