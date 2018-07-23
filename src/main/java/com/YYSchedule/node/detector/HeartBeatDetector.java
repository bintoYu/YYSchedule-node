
package com.YYSchedule.node.detector;

import java.util.Date;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.rpc.domain.node.NodePayload;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.node.config.Config;
import com.YYSchedule.node.queue.Queue;
import com.YYSchedule.node.service.NodeService;
import com.YYSchedule.node.utils.RpcUtils;
import com.YYSchedule.store.util.ActiveMQUtils;
/**
 * 
 * @author ybt
 *
 * @date 2018年7月12日  
 * @version 1.0
 */
@Component("HeartBeatDetector")
public class HeartBeatDetector implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatDetector.class);
	
	@Autowired
	private Config config;
	
	@Autowired
	private JmsTemplate jmsTemplate;

	/**
	 * 使用quartz定时执行execute方法
	 * @throws JobExecutionException
	 */
	public void execute() throws JobExecutionException {
		NodePayload nodePayload = generateHeartBeat();
		try {
			reportHeartBeat(nodePayload);
		} catch (InvalidRequestException ire) {
			throw new JobExecutionException("Failed to report heart beat : Invalid heart beat information : " + nodePayload + " : "
					+ ire.getMessage(), ire);
		} catch (UnavailableException ue) {
			throw new JobExecutionException("Failed to report heart beat : Master Error : " + ue.getMessage(), ue);
		} catch (TimeoutException toe) {
			throw new JobExecutionException("Failed to report heart beat : Timeout : " + toe.getMessage(), toe);
		} catch (TException te) {
			throw new JobExecutionException("Failed to report heart beat : RPC Error : " + te.getMessage(), te);
		} finally {
			nodePayload.setNodeRuntime(null);
			nodePayload = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// LOGGER.info("Start collecting heart beat thread ...");
		NodePayload nodePayload = generateHeartBeat();
		try {
			if (nodePayload != null) {
				reportHeartBeat(nodePayload);
			}
		} catch (InvalidRequestException ire) {
		} catch (UnavailableException ue) {
		} catch (TimeoutException toe) {
		} catch (TException te) {
		} finally {
			nodePayload.setNodeRuntime(null);
			nodePayload = null;
		}
	}

	/**
	 * generate heart beat information
	 * 
	 * @return nodePayload
	 */
	public NodePayload generateHeartBeat() {
		NodePayload nodePayload = new NodePayload();
		String nodeId = config.getLocal_listener_domain() + ":" + config.getTask_call_node_port();
		Queue queue = new Queue(nodeId);
		
		// init payload id info
		nodePayload.setNodeId(nodeId);
		
		nodePayload.setNodeRuntime(NodeService.getNodeRuntime());
		nodePayload.setQueueLimit(config.getMax_queue_size());
		nodePayload.setQueueLength(ActiveMQUtils.getQueueSize(jmsTemplate, queue.getEquippedContextQueueName()));
		nodePayload.setExpectedDelay(queue.getContextQueueExpectedDelay(jmsTemplate));
		nodePayload.setTaskPhase(TaskPhase.valueOf(config.getTask_phase()));
		
		return nodePayload;
	}

	/**
	 * report heart beat information to task manager using thrift rpc
	 * 
	 * @param nodePayload
	 * @throws InvalidRequestException
	 * @throws UnavailableException
	 * @throws TimeoutException
	 * @throws TException
	 */
	public void reportHeartBeat(NodePayload nodePayload) throws InvalidRequestException, UnavailableException, TimeoutException, TTransportException,
			TException {
		TProtocol tProtocol = RpcUtils.getTProtocol(config.getTaskmanager_listener_domain(), config.getNode_call_task_port(), config.getRpc_connect_timeout(), config.getRpc_connect_retry_times());
		NodeCallTaskService.Client nodeCallTaskService = new NodeCallTaskService.Client(tProtocol);
		if (nodeCallTaskService.reportHeartbeat(nodePayload) != 0) {
			LOGGER.error("Failed to report node [ " + nodePayload.getNodeId() + " ] to master [ " + config.getTaskmanager_listener_domain() + ":" + config.getNode_call_task_port()
					+ " ]");
			throw new UnavailableException("Failed to report node [ " + nodePayload.getNodeId() + " ] to master [ " + config.getTaskmanager_listener_domain() + ":"
					+ config.getNode_call_task_port() + " ]");
		}
		RpcUtils.close(nodeCallTaskService);
	}
}
