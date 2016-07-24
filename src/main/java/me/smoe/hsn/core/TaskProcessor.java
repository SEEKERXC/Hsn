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
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.smoe.hsn.adaptor.ChannelAdaptor;
import me.smoe.hsn.buffer.ChannelBuffer;
import me.smoe.hsn.buffer.pool.BufferPool;
import me.smoe.hsn.common.HsnProperties;
import me.smoe.hsn.common.HsnThreadFactory;
import me.smoe.hsn.logger.Logger;
import me.smoe.hsn.task.ChannelTask;

public class TaskProcessor implements Closeable {
	
	private HsnServer server;
	
	private Class<? extends ChannelAdaptor> adaptorClass;
	
	private ChannelAdaptor channelAdaptor;
	
	private BufferPool bufferPool;
	
	private ThreadPoolExecutor taskExecutor;
	
	private BlockingQueue<ChannelTask>[] channelTaskQueues;
	
	TaskProcessor(HsnServer server) {
		this.server = server;
	}
	
	void init() throws Exception {
		this.bufferPool = buildBufferPool();
		this.taskExecutor = buildChannelExecutor();
		this.channelTaskQueues = buildChannelTaskQueues();
		this.channelAdaptor = adaptorClass.newInstance();

		bufferPool.prestartCorePool();

		for (int i = 0; i < channelTaskQueues.length; i++) {
			taskExecutor.submit(new QueueTask(channelTaskQueues[i]));
		}
	}
	
	public void start() throws Exception {
		init();
	}

	void setChannelAdaptor(Class<? extends ChannelAdaptor> adaptorClass) {
		this.adaptorClass = adaptorClass;
	}
	
	public ChannelAdaptor channelAdaptor() {
		return channelAdaptor;
	}
	
	public ChannelBuffer newBuffer() throws Exception {
		return bufferPool.borrowObject();
	}
	
	public void recoverBuffer(ChannelBuffer channelBuffer) {
		bufferPool.returnObject(channelBuffer);
	}
	
	private BufferPool buildBufferPool() {
		int corePoolSize = server.getBufferPoolSize();
		int maxPoolSize = Integer.MAX_VALUE;
		int keepAliveTime = HsnProperties.DEFAULT_BUFFER_POOL_KEEPALIVE;
		int bufferSize = server.getBufferPoolSize();
		
		return new BufferPool(corePoolSize, maxPoolSize, keepAliveTime, bufferSize);
	}
	
	private ThreadPoolExecutor buildChannelExecutor() {
		int channelThreadCount = server.channelThreadCount();
		ThreadFactory channelHandlerFactory = HsnThreadFactory.buildChannelHandlerFactory();
		
		return (ThreadPoolExecutor) Executors.newFixedThreadPool(channelThreadCount, channelHandlerFactory);
	}

	private BlockingQueue<ChannelTask>[] buildChannelTaskQueues() {
		@SuppressWarnings("unchecked")
		BlockingQueue<ChannelTask>[] channelTaskQueues = new LinkedBlockingQueue[server.channelThreadCount()];
		for (int i = 0; i < channelTaskQueues.length; i++) {
			channelTaskQueues[i] = new LinkedBlockingQueue<>();
		}
		
		return channelTaskQueues;
	}

	public int calcTaskQueueIndex() {
		return new Random().nextInt(server.channelThreadCount());
	}

	public void processor(ChannelTask channelTask) {
		channelTaskQueues[channelTask.taskQueueIndex()].add(channelTask);
	}
	
	private static class QueueTask implements Runnable {

		private BlockingQueue<ChannelTask> channelTaskQueue;
	
		public QueueTask(BlockingQueue<ChannelTask> channelTaskQueue) {
			this.channelTaskQueue = channelTaskQueue;
		}
		
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				ChannelTask channelTask = null;
				try {
					channelTask = channelTaskQueue.take();
					channelTask.run();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Throwable throwable) {
					Logger.error("An exception occurs on channelTask running.", throwable);
					
					if (channelTask != null) {
						try {
							channelTask.channelSession().close();
						} catch (Throwable closeException) {
							Logger.error("ChannelSession close fail.", closeException);
						}
					}
					
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
		bufferPool.close();
		taskExecutor.shutdown();
	}
}
