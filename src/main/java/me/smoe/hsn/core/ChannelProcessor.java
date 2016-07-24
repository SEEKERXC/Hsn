/**
 * Copyright (c) 2015, adar.w (adar.w@outlook.com) 
 * 
 * http://www.smoe.me
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
package me.smoe.hsn.core;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.smoe.hsn.common.HsnThreadFactory;
import me.smoe.hsn.component.ChannelSelector;
import me.smoe.hsn.component.ChannelSession;

public class ChannelProcessor implements Closeable {
	
	private HsnServer server;

	private int channelSelectorCount;
	
	private ChannelSelector[] channelSelectors;
	
	private ExecutorService channelExecutor;
	
	ChannelProcessor(HsnServer server) {
		this.server = server;
	}
	
	private void init() throws IOException {
		this.channelSelectorCount = server.channelSelectorCount();

		this.channelSelectors = new ChannelSelector[channelSelectorCount];
		for (int i = 0; i < channelSelectors.length; i++) {
			ChannelSelector channelSelector = new ChannelSelector(server);
			channelSelectors[i] = channelSelector;
		}
		
		channelExecutor = buildChannelExecutor();
	}
	
	void start() throws IOException {
		init();
		
		for (int i = 0; i < channelSelectors.length; i++) {
			channelExecutor.submit(channelSelectors[i]);
		}
	}
	
	public void registerChannel(SelectableChannel channel, int interestOps, ChannelSession channelSession) {
		takeChannelSelector().registerChannel(channel, interestOps, channelSession);
	}
	
	private ChannelSelector takeChannelSelector() {
		return channelSelectors[new Random().nextInt(channelSelectorCount)];
	}
	
	private ExecutorService buildChannelExecutor() {
		return Executors.newFixedThreadPool(channelSelectorCount, HsnThreadFactory.buildChannelSelectorFactory());
	}

	@Override
	public void close() throws IOException {
		channelExecutor.shutdown();
	}
}
