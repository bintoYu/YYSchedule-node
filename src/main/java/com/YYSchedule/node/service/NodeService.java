/**
 * 
 */
package com.YYSchedule.node.service;

import java.text.DecimalFormat;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.YYSchedule.common.rpc.domain.node.NodeRuntime;

/**
 * @author ybt
 *
 * @date 2018年7月10日  
 * @version 1.0  
 */
public class NodeService
{
	/**
	 * 通过sigar获得node的runtime信息
	 * 
	 * @return NodeRuntime
	 */
	public static NodeRuntime getNodeRuntime() {

		Sigar sigar = new Sigar();
		NodeRuntime nodeRuntime = new NodeRuntime();

		try {
			//获取cpu信息
			int cpuCount = sigar.getCpuInfoList().length;
			CpuInfo cpuInfo = sigar.getCpuInfoList()[0];
			nodeRuntime.setCpuCount(cpuCount);
			nodeRuntime.setCpuCores(cpuInfo.getTotalCores());
			nodeRuntime.setCpuMhz(cpuInfo.getMhz());

			CpuPerc cpuPerc = sigar.getCpuPerc();
			double cpuUsedPerc = 0;
			if (cpuPerc.getCombined() == 0) {
				cpuUsedPerc = 0;
			} else {
				cpuUsedPerc = Double.parseDouble(new DecimalFormat("#.00").format(cpuPerc.getCombined() * 100));
			}
			nodeRuntime.setCpuUsedPerc(cpuUsedPerc);
			
			//获取剩余空间
			nodeRuntime.setFreeMem(sigar.getMem().getFree() / 1024 / 1024);
			nodeRuntime.setJvmFreeMem(Runtime.getRuntime().freeMemory() / 1024 / 1024);

		} catch (SigarException se) {
			throw new RuntimeException("Failed to get node heart beat [ " + nodeRuntime + " ] : " + se.getMessage(), se);
		} finally {
			sigar.close();
			sigar = null;
		}

		return nodeRuntime;
	}
	
	
	private static String getEmptyStringIfNull(Object o) {
		if (o == null) {
			return " ";
		} else {
			return o.toString();
		}
	}
}
