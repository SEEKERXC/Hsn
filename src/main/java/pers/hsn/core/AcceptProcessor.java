/**
 * Copyright (c) 2015, adar.w (adar.w@outlook.com) 
 * 
 * http://www.adar-w.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.hsn.core;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pers.hsn.common.HsnProperties;
import pers.hsn.common.HsnThreadFactory;
import pers.hsn.common.SocketOption;
import pers.hsn.component.ChannelSelector;

public class AcceptProcessor implements Closeable {

	private HsnServer server;

	private ChannelSelector channelSelector;
	
	private ServerSocketChannel serverSocketChannel;
	
	private ExecutorService acceptExecutor;
	
	AcceptProcessor(HsnServer server) {
		this.server = server;
	}
	
	private void init() throws Exception {
		buildServerSocketChannel();
		
		channelSelector = new ChannelSelector(server);
		channelSelector.registerChannel(serverSocketChannel, SelectionKey.OP_ACCEPT, null);
		
		acceptExecutor = buildAcceptExecutor();
	}
	
	void start() throws Exception {
		init();
		
		acceptExecutor.submit(channelSelector);
	}
	
	private void buildServerSocketChannel() throws Exception {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		setSocketOptions();
		serverSocketChannel.socket().bind(new InetSocketAddress(server.port()), server.backlog());
	}
	
	private void setSocketOptions() throws Exception {
		setDefaultOptions();
		setUserOptions();
	}
	
	private void setDefaultOptions() throws SocketException {
		serverSocketChannel.socket().setReuseAddress(true);
		serverSocketChannel.socket().setPerformancePreferences(HsnProperties.PERFORMANCE_CONNECTIONTIME, 
											   				   HsnProperties.PERFORMANCE_LATENCY, 
											   				   HsnProperties.PERFORMANCE_BANDWIDTH);
	}
	
	private void setUserOptions() throws Exception {
		SocketImpl socketImpl = getSocketImpl();
		for (Map.Entry<SocketOption, Object> me : server.getSocketOptions().entrySet()) {
			socketImpl.setOption(me.getKey().getOptId(), me.getValue());
		}
	}
	
	private SocketImpl getSocketImpl() throws Exception {
		Method method = ServerSocket.class.getDeclaredMethod("getImpl", new Class<?>[0]);
		method.setAccessible(true);
		
		return (SocketImpl) method.invoke(serverSocketChannel.socket(), new Object[0]);
	}
	
	private ExecutorService buildAcceptExecutor() {
		return Executors.newSingleThreadExecutor(HsnThreadFactory.buildAcceptSelectorFactory());
	}

	@Override
	public void close() throws IOException {
		acceptExecutor.shutdown();
	}
}
