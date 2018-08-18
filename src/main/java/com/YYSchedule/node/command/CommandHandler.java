/**
 * 
 */
package com.YYSchedule.node.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.YYSchedule.common.pojo.Task;
import com.YYSchedule.common.rpc.domain.task.TaskPhase;
import com.YYSchedule.common.rpc.domain.task.TaskStatus;
import com.YYSchedule.common.rpc.exception.InvalidRequestException;
import com.YYSchedule.common.rpc.exception.TimeoutException;
import com.YYSchedule.common.rpc.exception.UnavailableException;
import com.YYSchedule.common.rpc.service.task.NodeCallTaskService;
import com.YYSchedule.common.utils.RpcUtils;
import com.YYSchedule.node.config.Config;
import com.YYSchedule.node.process.ProcessWatcher;
import com.YYSchedule.node.process.ProcessResult;


/**
 * @author ybt
 *
 * @date 2018年7月30日  
 * @version 1.0  
 */
public class CommandHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);
	
	private Config config;
	
	public CommandHandler(Config config)
	{
		super();
		this.config = config;
	}

	/**
	 * 执行task
	 * 
	 * @param task
	 * @param localFilePath：下载下来的文件位置
	 * @return
	 * @throws ExecutionException
	 */
	
	public ProcessResult launch(Task task,String localFilePath) throws ExecutionException {
		File file = new File(localFilePath);
		LOGGER.info("Start executing program....");
		//获取命令行
		List<String> commandList = CommandGenerator.getCommand(task,localFilePath,config.getExecution_dir(),config.getEngine_call_node_port());
		LOGGER.info(commandList.toString());
		
		ProcessResult result = null;
		try {
			//将task更新为RUNNING
			task.setTaskStatus(TaskStatus.RUNNING);
			NodeCallTaskService.Client nodeCallTaskService = null;
			TProtocol tProtocol = RpcUtils.getTProtocol(config.getTaskmanager_listener_domain(), config.getNode_call_task_port(), config.getRpc_connect_timeout(), config.getRpc_connect_retry_times());
			nodeCallTaskService = new NodeCallTaskService.Client(tProtocol);
			nodeCallTaskService.reportTaskExecutionStatus(task.getExecutorId(), task.getTaskId(), task.getTaskPhase(), task.getTaskStatus());
			
			result = runCommand(task, commandList,new File(config.getExecution_dir()).getParentFile());
			// LOGGER.info(result.toString());
			LOGGER.info("Task [ " + task.getTaskId() + " ] process exit with result : " + result.toString());
			
			//执行完毕后，将文件删除
			FileUtils.deleteDirectory(file.getParentFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 执行命令行
	 * 
	 * @param task
	 * @param commandList
	 * @param workingDir:执行引擎所在的目录
	 * @return
	 * @throws ExecutionException
	 */
	private ProcessResult runCommand(Task task, List<String> commandList,File workingDir) throws ExecutionException {
		// run command
		Process process = null;
		try {
			ProcessBuilder pbuilder = new ProcessBuilder(commandList);
			if (workingDir != null) {
				pbuilder.directory(workingDir);
			}
			process = pbuilder.start();
			process.getOutputStream().close();
			process.getInputStream().close();
			process.getErrorStream().close();
		} catch (IOException ioe) {
			LOGGER.error("Failed to get task [ " + task.getTaskId() + " ] process : " + ioe.getMessage(), ioe);
			throw new ExecutionException("Failed to get task [ " + task.getTaskId() + " ] process : " + ioe.getMessage(), ioe);
		}
		// monitor process running status
		ProcessResult result = new ProcessWatcher().watchProcess(process, commandList.toString(), 0, task.getTimeout(), "UTF-8");
		return result;
	}


	
}
