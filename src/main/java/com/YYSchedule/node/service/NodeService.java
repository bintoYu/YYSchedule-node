/**
 * 
 */
package com.YYSchedule.node.service;

import java.io.File;
import java.text.DecimalFormat;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.YYSchedule.common.rpc.domain.node.NodeInfo;
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
	
	/**
	 * 通过sigar获取node的系统信息
	 * @return
	 */
	public static NodeInfo getNodeInfo() throws Exception 
	{
		NodeInfo nodeInfo = new NodeInfo();
		String classPath = NodeService.class.getProtectionDomain().getCodeSource().getLocation().getFile().toString();
	
		/**
		 * 获得盘符根目录
		 * windows：/D:/ybt/workspace-pc/YYSchedule-nodemanager/target/test-classes/  --> D:/
		 * linux： /root/home/.....    -->  /root/
		 */
		String devName = classPath.substring(classPath.indexOf("/") + 1, classPath.indexOf("/", classPath.indexOf("/") + 1));
		if (devName.contains(":")) {
			devName = devName + File.separatorChar;
		} else {
			devName = File.separatorChar + devName + File.separatorChar;
		}
		
		Sigar sigar = new Sigar();
		CpuInfo cpuInfo = sigar.getCpuInfoList()[0];
		
		/**
		 *  硬件信息 例：
		 *  CpuVendor:Intel
		 *  CpuModel:Core(TM) i3-2130 CPU @ 3.40GHz
		 *  TotalCores:cpu核数
		 *  CpuMhz:cpu的赫兹
		 *  MemSize:5963
		 */
		nodeInfo.setCpuVendor(cpuInfo.getVendor());
		nodeInfo.setCpuModel(cpuInfo.getModel());
		nodeInfo.setCpuCores(cpuInfo.getTotalCores());
		nodeInfo.setCpuMhz(cpuInfo.getMhz());
		nodeInfo.setMemSize(Long.valueOf(sigar.getMem().getTotal() / 1024 / 1024).intValue());
		nodeInfo.setFsName(devName);
		
		/**
		 * 文件系统信息
		 * FileSystemMap key为各个盘符根目录，例如D:\
		 * SysTypeName 该盘符文件系统类别，例如：NTFS或cdrom
		 * FileSystemUsage 该盘符使用量
		 */
		FileSystemMap fileSystemMap = sigar.getFileSystemMap();
		if (fileSystemMap.getFileSystem(devName) != null) {
			nodeInfo.setFsType(getEmptyStringIfNull(fileSystemMap.getFileSystem(devName).getTypeName()));
			nodeInfo.setFsFormat(getEmptyStringIfNull(fileSystemMap.getFileSystem(devName).getSysTypeName()));
			nodeInfo.setFsSize(Long.valueOf(sigar.getFileSystemUsage(devName).getTotal() / 1024 / 1024).intValue());
		} else {
			nodeInfo.setFsType("");
			nodeInfo.setFsFormat("");
			nodeInfo.setFsSize(0);
		}
		
		/**
		 * 网络配置
		 * 
		 */
		nodeInfo.setDomain(getEmptyStringIfNull(sigar.getNetInfo().getDomainName()));
		nodeInfo.setMacAddress(getEmptyStringIfNull(sigar.getNetInterfaceConfig(sigar.getNetInterfaceList()[0]).getHwaddr()));
		
		/**
		 *  软件信息 例：
		 *  os.arch:amd64
		 *  os.name:Windows 7
		 *  os.version:6.1
		 *  JvmName:Java HotSpot(TM) 64-Bit Server VM
		 *  JvmVersion.version:24.45-b08
		 *  JvmMaxMem:1326
		 */
		nodeInfo.setOsArch(getEmptyStringIfNull(System.getProperties().getProperty("os.arch")));
		nodeInfo.setOsName(getEmptyStringIfNull(System.getProperties().getProperty("os.name")));
		nodeInfo.setOsVersion(getEmptyStringIfNull(System.getProperties().getProperty("os.version")));
		nodeInfo.setJvmName(getEmptyStringIfNull(System.getProperties().getProperty("java.vm.name")));
		nodeInfo.setJvmVersion(getEmptyStringIfNull(System.getProperties().getProperty("java.vm.version")));
		nodeInfo.setJvmMaxMem(Long.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024).intValue());
		
		// nodeType is not set here!
		// queueLimit is not set here!
		
		// validation
		nodeInfo.setConfigUpdated(0);
		nodeInfo.setPayloadUpdated(0);
		return nodeInfo;
	}
	
	private static String getEmptyStringIfNull(Object o) {
		if (o == null) {
			return " ";
		} else {
			return o.toString();
		}
	}
}
