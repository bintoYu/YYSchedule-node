/**
 * 
 */
package com.YYSchedule.node.process;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.YYSchedule.common.rpc.domain.task.TaskStatus;

/**
 * ProcessResult.java
 * @author yanwei
 * @date 2013-4-1 下午5:47:15
 * @description
 */
public class ProcessResult {
	
	public static final int INTERRUPT_CODE_BIT = 1;
	
	public static final int TIMEOUT_CODE_BIT = 2;
	
	// process regular info

	private String command;
	
	private int successCode;
	
	private long timeout;

	// process error info

	/**
	 * exception notifier <br />
	 * | EXIT_CODE | INTERRUPT_CODE | TIMEOUT_CODE |
	 * {}finished | {1}interrupt | {2}timeout
	 */
	private BitSet processFlagSet;
	
	private int retCode;
	
	private List<Throwable> throwableList;
	
	public ProcessResult(String command, int successCode, long timeout) {
		this.command = command;
		this.successCode = successCode;
		this.timeout = timeout;
		this.processFlagSet = new BitSet(3);
		this.throwableList = new ArrayList<Throwable>();
	}
	
	public synchronized void setProcessFlagCode(int index) {
		processFlagSet.set(index);
	}
	
	public synchronized boolean getProcessFlagCode(int index) {
		return processFlagSet.get(index);
	}
	
	public synchronized void addThrowable(Throwable cause) {
		throwableList.add(cause);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int getSuccessCode() {
		return successCode;
	}

	public void setSuccessCode(int successCode) {
		this.successCode = successCode;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public BitSet getProcessFlagSet() {
		return processFlagSet;
	}

	public void setProcessFlagSet(BitSet processFlagSet) {
		this.processFlagSet = processFlagSet;
	}

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

	public List<Throwable> getThrowableList() {
		return throwableList;
	}

	public void setThrowableList(List<Throwable> throwableList) {
		this.throwableList = throwableList;
	}

	private String getResult()
	{
		if(processFlagSet.get(1))
		{
			return "interrupt";
		}
		else if(processFlagSet.get(2))
		{
			return "timeout";
		}
		else
		{
			return "finished";
		}
	}
	
	/**
	 * get task executing status from process result
	 * 
	 * @param processResult
	 * @return taskStatus TIMEOUT|INTERRPTED
	 */
	public TaskStatus getTaskStatus() {
		TaskStatus status = null;
		if (processFlagSet.get(2)) {
			status = TaskStatus.TIMEOUT;
		} else if (processFlagSet.get(1)) {
			status = TaskStatus.INTERRUPTED;
		}  else {
			status = TaskStatus.FINISHED;
		}
		return status;
	}
	
	@Override
	public String toString() {
		String result = getResult();
		
		return "ProcessResult [command=" + command 
				+ ", successCode=" + successCode + ", timeout=" + timeout
				+ ", result=" + result + "]";
	}
}
