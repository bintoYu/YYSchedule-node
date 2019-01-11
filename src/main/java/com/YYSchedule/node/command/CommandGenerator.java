/**
 * 
 */
package com.YYSchedule.node.command;

import java.util.ArrayList;
import java.util.List;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.parameter.ContextParameter;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.utils.StringUtils;

/**
 * @author ybt
 * 
 * @date 2018年7月31日
 * @version 1.0
 */
public class CommandGenerator
{
	/**
	 * generate command list
	 * 
	 * @param command
	 * @return commandList
	 */
	public static List<String> getCommandList(TaskPhase taskPhase, String command)
	{
		
		List<String> commandList = new ArrayList<String>();
		
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows"))
		{
			commandList.add("cmd.exe");
			commandList.add("/C");
		}
		else
		{
			commandList.add("/bin/sh");
			commandList.add("-c");
		}
		
		commandList.add(command);
		
		return commandList;
	}
	
	/**
	 * 获取命令行
	 * 
	 * @param context
	 * @return commandList
	 */
	public static List<String> getCommand(Task task, String fileName, String executionDir, int engine_call_node_port)
	{
		
		// prepare execute commands
		StringBuilder commandBuilder = new StringBuilder();
		List<ContextParameter> contextParameterList = task.getJobParameter().getContextParameterList();
		
		if (contextParameterList != null && contextParameterList.size() != 0)
		{
			// if parameter sequence matters
			String[] parameterArray = new String[contextParameterList.size()];
			for (ContextParameter parameter : contextParameterList)
			{
				// 将执行参数“例如java -jar”区分开
				if (parameter.sequenceNum == 0)
				{
					parameterArray[0] = parameter.getContent();
					parameterArray[0] += " " + executionDir;
					continue;
				}
				
				parameterArray[parameter.sequenceNum] = (StringUtils.isEmpty(parameter.getOpt()) ? "" : parameter.getOpt()) + " ";
				if (!StringUtils.isEmpty(parameter.getContent()))
				{
					if (parameter.getContent().indexOf('"') != -1)
					{
						parameterArray[parameter.sequenceNum] = parameterArray[parameter.sequenceNum] + parameter.getContent();
					}
					else
					{
						parameterArray[parameter.sequenceNum] = parameterArray[parameter.sequenceNum] + '"' + parameter.getContent() + '"';
					}
				}
			}
			
			for (String parameterContent : parameterArray)
			{
				commandBuilder.append(parameterContent + " ");
			}
			
		}
		
		// 设置nodeId，下发任务时，是task调用node的端口
		// 而引擎执行完后，应该变成engine调用node的端口
		// 也就是说 192.168.2.91:7000 需要改成 192.168.2.91:7010
		String nodeId = task.getExecutorId().substring(0, task.getExecutorId().indexOf(":") + 1) + engine_call_node_port;
		commandBuilder.append("-nodeId " + nodeId + " ");
		
		commandBuilder.append("-file " + fileName + " ");
		commandBuilder.append("-taskId " + task.getTaskId() + " ");
		
		return getCommandList(task.getTaskPhase(), commandBuilder.toString().trim());
	}
}
