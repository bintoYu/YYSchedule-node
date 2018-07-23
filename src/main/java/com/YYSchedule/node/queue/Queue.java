package com.YYSchedule.node.queue;

import org.springframework.jms.core.JmsTemplate;

import com.YYSchedule.store.util.ActiveMQUtils;


public class Queue {

	private String receivedContextQueueName;
	
	private String equippedContextQueueName;
	
	private String resultQueue;

	/**
	 * @param nodeId
	 */
	public Queue(String nodeId)
	{
		receivedContextQueueName = nodeId + ":" + "receivedContextQueueName";
		equippedContextQueueName = nodeId + ":" + "equippedContextQueueName";
		resultQueue = nodeId + ":" + "resultQueue";
	}

	public String getReceivedContextQueueName()
	{
		return receivedContextQueueName;
	}

	public String getEquippedContextQueueName()
	{
		return equippedContextQueueName;
	}

	public String getResultQueue()
	{
		return resultQueue;
	}
	
	public synchronized long getContextQueueExpectedDelay(JmsTemplate jmsTemplate) {
		long expectedDelay = 0L;
		for (Long delay : ActiveMQUtils.getQueueTimeoutList(jmsTemplate,receivedContextQueueName)) {
			expectedDelay += delay;
		}
		for (Long delay : ActiveMQUtils.getQueueTimeoutList(jmsTemplate,equippedContextQueueName)) {
			expectedDelay += delay;
		}
		return expectedDelay;
	}
}
