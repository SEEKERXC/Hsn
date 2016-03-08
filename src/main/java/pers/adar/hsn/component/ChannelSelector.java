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
package pers.adar.hsn.component;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pers.adar.hsn.core.HsnServer;
import pers.adar.hsn.logger.Logger;

public class ChannelSelector implements Runnable {
	
	private HsnServer server;

	private Selector selector;
	
	private final Queue<RegisterChannel> registerChannels = new LinkedBlockingQueue<>();
	
	public ChannelSelector(HsnServer server) throws IOException {
		this.server = server;
		this.selector = Selector.open();
	}
	
	public void registerChannel(SelectableChannel channel, int interestOps, ChannelSession channelSession) {
		registerChannels.offer(new RegisterChannel(channel, interestOps, channelSession));
		selector.wakeup();
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				selector.select();
			} catch (IOException e) {
				continue;
			}
			
			processRegisterChannels();
			
			Set<SelectionKey> keys = selector.selectedKeys();
			for (SelectionKey key : keys) {
				try{
					handle(key);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch(Throwable throwable){
					Logger.error("ChannelKey handle fail.", throwable);
					
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
			keys.clear();
		}
	}

	private void handle(SelectionKey key) throws Exception {
		if (key.isValid() && key.isAcceptable()) {
			ChannelHandler.handlerAccpet(server, key);
		} else if (key.isReadable()) {
			ChannelHandler.handlerRead(server, key);
		} else if (key.isWritable()) {
			ChannelHandler.handlerWrite(server, key);
		}
	}

	private void processRegisterChannels() {
		RegisterChannel registerChannel;
		while ((registerChannel = registerChannels.poll()) != null) {
			try {
				registerChannel.channel.register(selector, registerChannel.interestOps, registerChannel.channelSession);
			} catch (ClosedChannelException e) {
				continue;
			}
		}
	}

	private static class RegisterChannel {
		
		private SelectableChannel channel;
		
		private int interestOps;
		
		private ChannelSession channelSession;
		
		public RegisterChannel(SelectableChannel channel, int interestOps, ChannelSession channelSession) {
			this.channel = channel;
			this.interestOps = interestOps;
			this.channelSession = channelSession;
		}
	}
}
