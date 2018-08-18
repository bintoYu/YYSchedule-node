/**
 * 
 */
package com.YYSchedule.node.process;

import java.util.BitSet;

/**
 * 
 * 
 * @author ybt
 *
 * @date 2018年7月31日  
 * @version 1.0
 */
public class ThreadTimer implements Runnable {

	private Process process;

	private BitSet processFlagSet;

	private long timeout;

	/**
	 * 实现超时中断机制
	 * 
	 * @param process
	 * @param processFlagSet
	 *            : {}success | {0}failure | {0,1}interrupt | {0,2}timeout
	 * @param timeout
	 */
	public ThreadTimer(Process process, BitSet processFlagSet, long timeout) {
		this.process = process;
		this.processFlagSet = processFlagSet;
		this.timeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException ie) {
		}
		interrupt();
	}

	public void interrupt() {
		try {
			process.exitValue();
		} catch (IllegalThreadStateException ie) { 
			// only timeout when the process is not yet finished
			processFlagSet.set(ProcessResult.TIMEOUT_CODE_BIT);
			ProcessWatcher.killProcess(process);
		}

	}
}