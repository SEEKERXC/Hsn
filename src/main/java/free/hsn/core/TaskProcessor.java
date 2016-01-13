package free.hsn.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import free.hsn.adaptor.ChannelAdaptor;
import free.hsn.buffer.ChannelBuffer;
import free.hsn.buffer.pool.BufferPool;
import free.hsn.common.HsnProperties;
import free.hsn.common.HsnThreadFactory;
import free.hsn.logger.Logger;
import free.hsn.task.ChannelTask;

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
			channelTaskQueues[i] = new LinkedBlockingQueue<ChannelTask>();
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

		private boolean stop;
		
		private BlockingQueue<ChannelTask> channelTaskQueue;
	
		public QueueTask(BlockingQueue<ChannelTask> channelTaskQueue) {
			this.channelTaskQueue = channelTaskQueue;
		}
		
		@Override
		public void run() {
			while (!stop) {
				ChannelTask channelTask = null;
				try {
					channelTask = channelTaskQueue.take();
					channelTask.run();
				} catch (InterruptedException e) {
					stop = true;
				} catch (Exception e) {
					Logger.error("An exception occurs on channelTask running.", e);
					
					if (channelTask != null) {
						try {
							channelTask.channelSession().close();
						} catch (IOException e2) {
							Logger.error("ChannelSession close fail.", e2);
						}
					}
					
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e1) {
						// Need do nothing.
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
