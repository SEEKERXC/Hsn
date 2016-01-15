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
package free.hsn.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import free.hsn.adaptor.ChannelAdaptor;
import free.hsn.common.HsnProperties;
import free.hsn.common.SocketOption;
import free.hsn.logger.Logger;

public class HsnServer {
	
	private int port;
	
	private int backlog;
	
	private boolean isStart;
	
	private int channelSelectorCount = HsnProperties.DEFAULT_CHANNEL_SELECTOR_COUNT;

	private int channelThreadCount = HsnProperties.DEFAULT_CHANNEL_THREAD_COUNT;
	
	private int bufferSize = HsnProperties.DEFAULT_BUFFER_SIZE;

	private int bufferPoolSize = HsnProperties.DEFAULT_BUFFER_POOL_SIZE;
	
	private Map<SocketOption, Object> socketOptions = new HashMap<SocketOption, Object>();
	
	private AcceptProcessor acceptProcessor;
	
	private ChannelProcessor channelProcessor;
	
	private TaskProcessor taskProcessor;

	public HsnServer(int port) {
		this(port, HsnProperties.BACKLOG);
	}
	
	public HsnServer(int port, int backlog) {
		this.port = port;
		this.backlog = backlog;
		this.acceptProcessor = new AcceptProcessor(this);
		this.channelProcessor = new ChannelProcessor(this);
		this.taskProcessor = new TaskProcessor(this);
	}
	
	public synchronized void start() throws Exception {
		if (!isStart) {
			acceptProcessor.start();
			channelProcessor.start();
			taskProcessor.start();
			
			Logger.info("Hsn server has start...");
		}
	}
	
	public synchronized void close() throws IOException {
		if (isStart) {
			acceptProcessor.close();
			channelProcessor.close();
			taskProcessor.close();
		}
	}
	
	public int port() {
		return port;
	}
	
	public int backlog() {
		return backlog;
	}
	
	public int channelSelectorCount() {
		return channelSelectorCount;
	}

	public void setChannelSelectorCount(int channelSelectorCount) {
		this.channelSelectorCount = channelSelectorCount;
	}

	public int channelThreadCount() {
		return channelThreadCount;
	}
	
	public void setChannelThreadCount(int channelThreadCount) {
		this.channelThreadCount = channelThreadCount;
	}
	
	public void setChannelAdaptor(Class<? extends ChannelAdaptor> channelAdaptor) {
		taskProcessor.setChannelAdaptor(channelAdaptor);
	}
	
	public void addSocketOption(SocketOption option, Object optionValue) {
		socketOptions.put(option, optionValue);
	}

	public Map<SocketOption, Object> getSocketOptions() {
		return socketOptions;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getBufferPoolSize() {
		return bufferPoolSize;
	}

	public void setBufferPoolSize(int bufferPoolSize) {
		this.bufferPoolSize = bufferPoolSize;
	}
	
	public ChannelProcessor channelProcessor() {
		return channelProcessor;
	}

	public TaskProcessor taskProcessor() {
		return taskProcessor;
	}
}
