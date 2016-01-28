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
package pers.hsn.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import pers.hsn.buffer.ChannelBuffer;
import pers.hsn.core.HsnServer;
import pers.hsn.exception.ClosedSessionException;
import pers.hsn.logger.Logger;

public class ChannelSession {
	
	private HsnServer server;
	
	private SocketChannel socketChannel;
	
	private SelectionKey selectionKey;
	
	private Map<String, Object> attributes;
	
	private ChannelContext channelContext;
	
	private ChannelBuffer channelReadBuffer;

	private ChannelBuffer channelWriteBuffer;
	
	private ByteBuffer readBuffer;

	private ByteBuffer writeBuffer;
	
	private boolean isRecoverReadBuffer;
	
	private boolean isRecoverWriteBuffer; 
	
	private boolean needFlush;
	
	private boolean needClose;

	private int taskQueueIndex;

	ChannelSession(HsnServer server, SocketChannel socketChannel) {
		this.server = server;
		this.socketChannel = socketChannel;
		this.channelContext = new ChannelContext(this);
	}
	
	public HsnServer server() {
		return server;
	}
	
	public void allocateReadBuffer(ChannelBuffer channelReadBuffer) {
		this.channelReadBuffer = channelReadBuffer;
		this.readBuffer = channelReadBuffer.byteBuffer();
	}

	public void allocateWriteBuffer(ChannelBuffer channelWriteBuffer) {
		this.channelWriteBuffer = channelWriteBuffer;
		this.writeBuffer = channelWriteBuffer.byteBuffer();
	}
	
	public void taskQueueIndex(int taskQueueIndex) {
		this.taskQueueIndex = taskQueueIndex;
	}
	
	public SocketChannel socketChannel() {
		return socketChannel;
	}

	public int taskQueueIndex() {
		return taskQueueIndex;
	}

	public SelectionKey selectionKey() {
		return selectionKey;
	}

	public void selectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}
	
	public void setAttribute(String name, Object value) {
		if (attributes == null) {
			attributes = new HashMap<String, Object>();
		}
		
		attributes.put(name, value);
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	private void needFlush(boolean needFlush) {
		this.needFlush = needFlush;
	}

	private boolean needFlush() {
		return needFlush;
	}
	
	public void flush() {
		if (needFlush()) {
			writeable();
		}
		
		needFlush(false);
	}
	
	public void needClose(boolean needClose) {
		this.needClose = needClose;
	}
	
	private boolean needClose() {
		return needClose;
	}
	
	public void close() throws IOException {
		onClosed();
		
		recoverReadBuffer();
		recoverWriteBuffer();
		
		selectionKey.cancel();
		socketChannel.close();
	}
	
	public boolean checkClose() {
		if (needClose() && hasWriteFinished()) {
			try {
				close();
			} catch (IOException e) {
				Logger.error("ChannelSession close fail.", e);
			}
			
			return true;
		}
		return false;
	}
	
	private boolean hasWriteFinished() {
		return writeBuffer.position() == 0;
	}
	
	public int readChannel() throws IOException {
		int length = socketChannel.read(readBuffer);
		if (length == -1) {
			return length;
		} else {
			while (!readBuffer.hasRemaining()) {
				ByteBuffer newReadBuffer = ByteBuffer.allocate(readBuffer.limit() * 2);
				
				readBuffer.flip();
				newReadBuffer.put(readBuffer);
				
				newReadBuffer(newReadBuffer);
				
				length += socketChannel.read(readBuffer);
			}
		}
		
		return length;
	}
	
	public int writeChannel() throws IOException {
		return socketChannel.write(writeBuffer);
	}
	
	public ByteBuffer read() {
		return readBuffer;
	}
	
	public void write(byte[] bytes) {
		if (needFlush()) {
			throw new ClosedSessionException();
		}
		
		if (writeBuffer.remaining() >= bytes.length) {
			writeBuffer.put(bytes);
		} else {
			ByteBuffer newWriteBuffer = ByteBuffer.allocate(writeBuffer.capacity() + bytes.length * 2);
			newWriteBuffer.put(bytes);
			
			newWriteBuffer(newWriteBuffer);
		}
		
		needFlush(true);
	}
	
	private void newReadBuffer(ByteBuffer newReadBuffer) {
		recoverReadBuffer();
		
		readBuffer = newReadBuffer;
	}

	private void newWriteBuffer(ByteBuffer newWriteBuffer) {
		recoverWriteBuffer();
		
		writeBuffer = newWriteBuffer;
	}
	
	public void filpReadBuffer() {
		readBuffer.flip();
	}

	public void filpWriteBuffer() {
		writeBuffer.flip();
	}
	
	public void clearReadBuffer() {
		readBuffer.clear();
	}

	public void clearWriteBuffer() {
		writeBuffer.clear();
	}
	
	private void recoverReadBuffer() {
		if (!isRecoverReadBuffer) {
			server.taskProcessor().recoverBuffer(channelReadBuffer);
		}
		
		isRecoverReadBuffer = true;
	}

	private void recoverWriteBuffer() {
		if (!isRecoverWriteBuffer) {
			server.taskProcessor().recoverBuffer(channelWriteBuffer);
		}
		
		isRecoverWriteBuffer = true;
	}
	
	public void readable() {
		selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);

		wakeupSelector();
	}

	public void writeable() {
		selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
		
		wakeupSelector();
	}
	
	private void wakeupSelector() {
		selectionKey.selector().wakeup();
	}
	
	public void onConnected() {
		server.taskProcessor().channelAdaptor().onConnected(channelContext);
	}
	
	public void onMessage() {
		server.taskProcessor().channelAdaptor().onMessage(channelContext);
	}
	
	public void onClosed() {
		server.taskProcessor().channelAdaptor().onClosed(channelContext);
	}
	
	public void onExeception(Throwable throwable) {
		server.taskProcessor().channelAdaptor().onExeception(channelContext, throwable);
	}
	
	public class ChannelContext {

		private ChannelSession channelSession;
		
		ChannelContext(ChannelSession channelSession) {
			this.channelSession = channelSession;
		}
		
		public SocketChannel socketChannel() {
			return channelSession.socketChannel;
		}
		
		public void setAttribute(String name, Object value) {
			channelSession.setAttribute(name, value);
		}
		
		public Object getAttribute(String name) {
			return channelSession.getAttribute(name);
		}
		
		public ByteBuffer read() {
			return channelSession.read();
		}
		
		public void write(byte[] bytes) {
			channelSession.write(bytes);
		}
		
		public void flush() {
			channelSession.flush();
		}
		
		public void close() {
			channelSession.needClose(true);
		}
	}
}
