package com.YYSchedule.node.utils;

import org.apache.log4j.Logger;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class RpcUtils {

	private static final Logger LOGGER = Logger.getLogger(RpcUtils.class);

	/**
	 * 获取TProtocol
	 * 适用于所有thrift服务
	 * 同时，如果连接thrift服务失败，会进行多次尝试
	 * 
	 * @param ip   
	 * @param port
	 * @param timeout
	 * @param retryTimes
	 * @return TProtocol
	 * @throws TTransportException
	 */
	public static TProtocol getTProtocol(String ip, int port, int timeout, int retryTimes) throws TTransportException {
		TProtocol protocol = prepare(ip, port, timeout, retryTimes);
		return protocol;
	}

	/**
	 * 实现连接失败多次尝试的功能
	 * @param ip
	 * @param port
	 * @param timeout
	 * @param retryTimes
	 * @return TProtocol
	 * @throws TTransportException
	 */
	private static TProtocol prepare(String ip, int port, int timeout, int retryTimes) throws TTransportException {
		TTransport transport = new TSocket(ip, port, timeout);

		boolean opened = false;
		Exception cause = null;
		for (int i = 1; i <= retryTimes; i++) {
			try {
				transport.open();
				opened = true;
				break;
			} catch (TTransportException e) {
				cause = e;
				LOGGER.debug("Open connect [" + ip + ":" + port + "] fail retry times : " + i);
			}
		}

		if (!opened) {
			throw new TTransportException("Remote host unreachable.", cause);
		}
		TProtocol protocol = new TBinaryProtocol(transport);
		return protocol;
	}

	/**
	 * 关闭服务
	 * @param client
	 */
	public static void close(TServiceClient client) {
		if (client != null) {
			client.getInputProtocol().getTransport().close();
		}
	}

}
