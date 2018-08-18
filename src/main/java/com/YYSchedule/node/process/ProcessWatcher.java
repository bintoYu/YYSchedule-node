/**
 * 
 */
package com.YYSchedule.node.process;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.YYSchedule.common.utils.StringUtils;
import com.sun.jna.Pointer;

/**
 * 
 * @author ybt
 * 
 * @date 2018年8月2日
 * @version 1.0
 */
public class ProcessWatcher
{
	private static final Logger LOGGER = Logger.getLogger(ProcessWatcher.class);
	
	/**
	 * 开启一个监听器，用于判断程序的运行结果，可能的情况如下：
	 * 1、程序成功执行完毕（本程序不处理成功信息） 
	 * 2、程序执行过程中被强制中断
	 * 3、程序执行超时
	 * 
	 * @param process
	 * @param command
	 *            : command list
	 * @param successCode
	 * @param timeout
	 *            : kill the process when timeout
	 * @param charset
	 *            : encode
	 * @return processResult
	 * @exception : this method record every exception into ProcessResult but do
	 *            not throws exception
	 */
	public ProcessResult watchProcess(Process process, String command, int successCode, long timeout, String charset)
	{
		
		/************** init process result **************/
		ProcessResult result = new ProcessResult(command, successCode, timeout);
		
		/************** launch time watcher **************/
		if (timeout > 0L) {
			ThreadTimer timer = new ThreadTimer(process, result.getProcessFlagSet(), timeout);
			new Thread(timer).start();
		}
		
		try {
			boolean isCompleted = false;
			while (!isCompleted && !result.getProcessFlagCode(ProcessResult.INTERRUPT_CODE_BIT) && !result.getProcessFlagCode(ProcessResult.TIMEOUT_CODE_BIT)) 
			{
				try 
				{
					int retCode = process.waitFor();
					result.setRetCode(retCode);
					// 执行失败
					if (retCode != successCode) 
					{
						result.setProcessFlagCode(ProcessResult.INTERRUPT_CODE_BIT);
					}
					isCompleted = true;
				} catch (IllegalThreadStateException itse) {
					result.addThrowable(itse);
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						// 执行中断
						result.setProcessFlagCode(ProcessResult.INTERRUPT_CODE_BIT);
						ProcessWatcher.killProcess(process);
					}
					
				} catch (InterruptedException ie) {
					result.addThrowable(ie);
				}
			}
			
		} catch (NullPointerException npe) {
			result.addThrowable(npe);
		}
		return result;
	}
	
	/**
	 * manage process 's input and output without encode
	 * 
	 * @param process
	 * @param command
	 * @param input
	 * @param timeout
	 * @return processResult
	 */
	public ProcessResult launchContainer(Process process, String command, String input, long timeout)
	{
		return watchProcess(process, command, 0, timeout, null);
	}
	
	/**
	 * manage process 's input and output only with process and command
	 * 
	 * @param process
	 * @param command
	 * @param input
	 * @param timeout
	 * @return processResult
	 */
	public ProcessResult launchContainer(Process process, String command)
	{
		return watchProcess(process, command, 0, 0, null);
	}
	
	/**
	 * kill process, only support windows and unix
	 * 
	 * @param process
	 * @return kill status : 0 success | -1 kill fail | -2 cannot get pid
	 */
	public static int killProcess(Process process)
	{
		// process.destroy();
		String os = System.getProperty("os.name");
		
		int pid = -1;
		if (os.startsWith("Windows")) {
			pid = getPidForWindows(process);
			String[] commondArray =
			{ "taskkill", "/PID", String.valueOf(pid), "/F", "/T" };
			if (pid != -1) {
				try {
					Process killProcess = Runtime.getRuntime().exec(commondArray);
					killProcess.getOutputStream().close();
					killProcess.getInputStream().close();
					killProcess.getErrorStream().close();
				} catch (IOException e) {
					process.destroy();
					LOGGER.equals("cannot kill process, pid [ " + pid + " ]");
					return -1; // cannot kill process
				}
			}
			else {
				LOGGER.error("failed to get pid for process [ " + process.toString() + " ]");
				process.destroy();
				return -2; // cannot get pid
			}
			
		}
		else {
			pid = getPidForUnix(process);
			String[] commondArray =
			{ "kill", "-9", String.valueOf(pid) };
			if (pid != -1) {
				try {
					Process killProcess = Runtime.getRuntime().exec(commondArray);
					killProcess.getOutputStream().close();
					killProcess.getInputStream().close();
					killProcess.getErrorStream().close();
				} catch (IOException e) {
					process.destroy();
					LOGGER.equals("cannot kill process, pid [ " + pid + " ]");
					return -1; // cannot kill process
				}
			}
			else {
				LOGGER.error("failed to get pid for process [ " + process.toString() + " ]");
				process.destroy();
				return -2; // cannot get pid
			}
		}
		return 0;
	}
	
	/**
	 * get pid for process on unix
	 * 
	 * @param process
	 * @return pid | -1 failure
	 */
	private static int getPidForUnix(Process process)
	{
		int pid = -1;
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */
			try {
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getInt(process);
			} catch (Throwable e) {
				LOGGER.error("failed to get pid for process [ " + process.toString() + " ] : " + e.getMessage(), e);
				return -1;
			}
		}
		return pid;
	}
	
	/**
	 * get pid for process on windows
	 * 
	 * @param process
	 * @return pid | -1 failure
	 */
	private static int getPidForWindows(Process process)
	{
		int pid = -1;
		if (process.getClass().getName().equals("java.lang.Win32Process") || process.getClass().getName().equals("java.lang.ProcessImpl")) {
			/* determine the pid on windows plattforms */
			try {
				Field f = process.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				// f.getInt(process);
				long handl = f.getLong(process);
				Kernel32 kernel = Kernel32.INSTANCE;
				W32API.HANDLE handle = new W32API.HANDLE();
				handle.setPointer(Pointer.createConstant(handl));
				pid = kernel.GetProcessId(handle);
			} catch (Throwable e) {
				LOGGER.error("failed to get pid for process [ " + process.toString() + " ] : " + e.getMessage(), e);
				return -1;
			}
		}
		return pid;
	}
	
}
