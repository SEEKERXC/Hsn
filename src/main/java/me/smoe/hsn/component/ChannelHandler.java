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
package me.smoe.hsn.component;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import me.smoe.hsn.core.HsnServer;
import me.smoe.hsn.logger.Logger;
import me.smoe.hsn.task.impl.ChannelReadTask;
import me.smoe.hsn.task.impl.ChannelWriteTask;

public final class ChannelHandler {

	private ChannelHandler() {}

	public static void handlerAccpet(HsnServer server, SelectionKey selectionKey) throws Exception {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);
		
		Logger.debug(String.format("Accept socketChannel: %s", socketChannel.getRemoteAddress()));
		
		server.channelProcessor().registerChannel(socketChannel, SelectionKey.OP_READ, openSession(server, socketChannel));
	}
	
	public static void handlerRead(HsnServer server, SelectionKey selectionKey) {
		selectionKey.interestOps(selectionKey.interestOps() & (~SelectionKey.OP_READ));
		
		ChannelSession channelSession = (ChannelSession) selectionKey.attachment();
		channelSession.selectionKey(selectionKey);
		
		server.taskProcessor().processor(new ChannelReadTask(channelSession));
	}
	
	public static void handlerWrite(HsnServer server, SelectionKey selectionKey) {
		selectionKey.interestOps(selectionKey.interestOps() & (~SelectionKey.OP_WRITE));
		
		server.taskProcessor().processor(new ChannelWriteTask((ChannelSession) selectionKey.attachment()));
	}
	
	private static ChannelSession openSession(HsnServer server, SocketChannel socketChannel) throws Exception {
		ChannelSession channelSession = createSession(server, socketChannel);
		channelSession.onConnected();
		
		return channelSession;
	}
	
	private static ChannelSession createSession(HsnServer server, SocketChannel socketChannel) throws Exception {
		ChannelSession channelSession = new ChannelSession(server, socketChannel);
		channelSession.allocateReadBuffer(server.taskProcessor().newBuffer());
		channelSession.allocateWriteBuffer(server.taskProcessor().newBuffer());
		channelSession.taskQueueIndex(server.taskProcessor().calcTaskQueueIndex());
		
		return channelSession;
	}
}
