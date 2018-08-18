/**
 * 
 */
package com.YYSchedule.node.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.YYSchedule.common.pojo.NodeItem;
import com.YYSchedule.common.rpc.domain.engine.EngineLogger;

/**
 * @author ybt
 *
 * @date 2018年8月7日  
 * @version 1.0  
 */
@Component("EngineLoggerMapper")
@Scope("singleton")
public class EngineLoggerMapper
{
	private Map<Long, EngineLogger> engineLoggerMap = new ConcurrentHashMap<>();

	public synchronized Map<Long, EngineLogger> getEngineLoggerMap()
	{
		return engineLoggerMap;
	}

	public synchronized List<EngineLogger> getAllEngineLogger()
	{
		return new ArrayList<EngineLogger>(engineLoggerMap.values());
	}
	
	public synchronized void updateEngineLogger(EngineLogger engineLogger) {
		engineLoggerMap.put(engineLogger.getTaskId(), engineLogger);
	}
	
	public synchronized void removeEngineLogger(Long taskId) {
		engineLoggerMap.remove(taskId);
	}
}
