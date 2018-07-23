/**
 * 
 */
package com.YYSchedule.node.test;

import java.io.File;
import java.util.Set;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.YYSchedule.common.rpc.domain.node.NodeInfo;

/**
 * @author ybt
 *
 * @date 2018年7月10日  
 * @version 1.0  
 */
public class Test
{
	public static void main(String[] args)
	{
		Sigar sigar = new Sigar();
		NodeInfo nodeInfo = new NodeInfo();
		String classPath = Test.class.getProtectionDomain().getCodeSource().getLocation().getFile().toString();

		String devName = classPath.substring(classPath.indexOf("/") + 1, classPath.indexOf("/", classPath.indexOf("/") + 1));
		if (devName.contains(":")) {
			devName = devName + File.separatorChar;
		} else {
			devName = File.separatorChar + devName + File.separatorChar;
		}
		
		try {
			CpuInfo cpuInfo = sigar.getCpuInfoList()[0];
//			System.out.println(cpuInfo.getVendor());
//			System.out.println(cpuInfo.getModel());
			System.out.println(cpuInfo.getTotalCores());
			System.out.println(cpuInfo.getMhz());
//			System.out.println(Long.valueOf(sigar.getMem().getTotal() / 1024 / 1024).intValue());
//			System.out.println(System.getProperties().getProperty("os.arch"));
//			System.out.println(System.getProperties().getProperty("os.name"));
//			System.out.println(System.getProperties().getProperty("os.version"));
//			System.out.println(System.getProperties().getProperty("java.vm.name"));
//			System.out.println(System.getProperties().getProperty("java.vm.version"));
//			System.out.println(Long.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024).intValue());
//			System.out.println(sigar.getNetInfo().getDomainName());
//			System.out.println(sigar.getNetInfo().getHostName());
//			System.out.println(sigar.getNetInterfaceConfig(sigar.getNetInterfaceList()[0]).getHwaddr());
//			System.out.println(sigar.getNetStat().getTcpStates()[0]);
//			System.out.println();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		

	}
}
