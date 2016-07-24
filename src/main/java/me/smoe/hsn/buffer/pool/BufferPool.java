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
package me.smoe.hsn.buffer.pool;

import java.io.Closeable;
import java.nio.ByteBuffer;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import me.smoe.hsn.buffer.ChannelBuffer;

public class BufferPool implements Closeable {
	
	private GenericObjectPool<ChannelBuffer> objectPool;

	public BufferPool(int corePoolSize, int maxPoolSize, int keepAliveTime, int bufferSize) {
		super();
		
		objectPool = buildPool(corePoolSize, maxPoolSize, keepAliveTime, bufferSize);
	}
	
	public ChannelBuffer borrowObject() throws Exception {
		return objectPool.borrowObject();
	}
	
	public void returnObject(ChannelBuffer byteBuffer) {
		objectPool.returnObject(byteBuffer);
	}
	
	public void close() {
		objectPool.close();
	}
	
	private GenericObjectPool<ChannelBuffer> buildPool(int corePoolSize, int maxPoolSize, int keepAliveTime, int bufferSize) {
		GenericObjectPoolConfig poolConfig = buildPoolConfig(corePoolSize, maxPoolSize, keepAliveTime);
		
		return new GenericObjectPool<>(new BufferFactory(bufferSize), poolConfig);
	}
	
	private static GenericObjectPoolConfig buildPoolConfig(int corePoolSize, int maxPoolSize, int keepAliveTime) {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxIdle(corePoolSize);
		config.setMaxTotal(maxPoolSize);
		config.setMaxWaitMillis(keepAliveTime);
		
		return config;
	}
	
	public void prestartCorePool() throws Exception {
		for (int i = 0; i < objectPool.getMaxIdle(); i++) {
			objectPool.addObject();
		}
	}

	private class BufferFactory implements PooledObjectFactory<ChannelBuffer> {
	
		private int bufferSize;
		
		public BufferFactory(int bufferSize) {
			super();
			this.bufferSize = bufferSize;
		}
	
		@Override
		public PooledObject<ChannelBuffer> makeObject() throws Exception {
			return new DefaultPooledObject<>(new ChannelBuffer(ByteBuffer.allocateDirect(bufferSize)));
		}
	
		@Override
		public void destroyObject(PooledObject<ChannelBuffer> pooledObject) throws Exception {
			// Need do nothing.
		}
	
		@Override
		public boolean validateObject(PooledObject<ChannelBuffer> pooledObject) {
			return true;
		}
	
		@Override
		public void activateObject(PooledObject<ChannelBuffer> pooledObject) throws Exception {
			// See passivateObject.
		}
	
		@Override
		public void passivateObject(PooledObject<ChannelBuffer> pooledObject) throws Exception {
			pooledObject.getObject().byteBuffer().clear();
		}
	}
}
