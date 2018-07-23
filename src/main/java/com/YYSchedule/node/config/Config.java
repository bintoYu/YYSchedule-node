package com.YYSchedule.node.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("Config")
public class Config
{
	@Value("#{config.local_listener_domain}")
	private String local_listener_domain;
	
	@Value("#{config.task_call_node_port}")
	private int task_call_node_port;

	@Value("#{config.taskmanager_listener_domain}")
	private String taskmanager_listener_domain;	
	
	@Value("#{config.node_call_task_port}")
	private int node_call_task_port;

	@Value("#{config.task_phase}")
	private String task_phase;
	
	@Value("#{config.rpc_connect_timeout}")
	private int rpc_connect_timeout;
	
	@Value("#{config.rpc_connect_retry_times}")
	private int rpc_connect_retry_times;
	
	@Value("#{config.max_queue_size}")
	private int max_queue_size;
	
	public String getLocal_listener_domain()
	{
		return local_listener_domain;
	}

	public int getTask_call_node_port()
	{
		return task_call_node_port;
	}

	public String getTaskmanager_listener_domain()
	{
		return taskmanager_listener_domain;
	}
	
	public int getNode_call_task_port()
	{
		return node_call_task_port;
	}

	public String getTask_phase()
	{
		return task_phase;
	}

	public int getRpc_connect_timeout()
	{
		return rpc_connect_timeout;
	}

	public int getRpc_connect_retry_times()
	{
		return rpc_connect_retry_times;
	}

	public int getMax_queue_size()
	{
		return max_queue_size;
	}
	
	
}
