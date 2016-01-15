

# Hsn
> A simple nio server framework.


####设计概要
	1. IO模型采用JDK NIO

	2. 具备多线程单机扩容能力
			通过配置可尽可能的提高资源使用率.

	3. 基于多Selector Reactor模式
			Accept  Selector 1
			Channel Selector *	
			
	4. 基于池化本地缓冲区
			所有缓冲区均为高效的本地缓冲区,并实现基于内存池的缓冲区重用机制.
			
	5. 基于无锁化的串行设计理念
			尽可能的避免锁竞争带来的性能损耗.
			通过串行化设计,同一连接的业务处理始终仅在同一个线程内完成,避免了多线程竞争和同步锁.


####相较于Netty Mina
* [x] 代码更简洁, 仅40KB左右.
	
* [x] 更大的自定义扩展空间.

* [x] 对学习JDK NIO更具有学习意义.
	
* [x] 更快捷联系作者探讨相关问题(可忽略).


####Demo
	参见: free.hsn.HelloHsn
	
	Example: Echo server
	
	Server code:
		public class HelloHsn {
	
			public static void main(String[] args) throws Exception {
				// 监听端口10080, 设置Backlog值为200
				HsnServer server = new HsnServer(10080, 200);
				
				// 设置用于处理连接相关操作的Selector数量(默认同CPU核心数量)
				server.setChannelSelectorCount(2);
				
				// 设置用于处理连接相关操作的线程数量(默认为ChannelSelectorCount的两倍)
				server.setChannelThreadCount(4);
		
				// 设置缓冲区对象池大小(默认1024)
				server.setBufferPoolSize(1024);
				// 设置缓冲区默认容量(默认1024)
				server.setBufferSize(1024);
		
				// 设置Channel适配器
				server.setChannelAdaptor(EchoChannelAdaptor.class);
				
				// 启动Hsn
				server.start();
			}
		}
		
	ChannelAdaptor code:
		public class EchoChannelAdaptor extends StandardChannelAdaptor {

			@Override
			public void onMessage(ChannelContext channelContext) {
				channelContext.write(Charset.forName("UTF-8").decode(channelContext.read()).toString().getBytes());
				
				channelContext.close();
			}
		}
	
	
####Jmeter测试数据 
	测试环境:
		Dell E5400、Win7、I5-4210U
	软件环境:
		短连接、并发200、循环50次
![image](https://raw.githubusercontent.com/Adar-w/Hsn/dev/file/Jmeter-%E5%B9%B6%E5%8F%91200%E5%BE%AA%E7%8E%AF50%E6%AC%A1%E7%9F%AD%E8%BF%9E%E6%8E%A5%E6%B5%8B%E8%AF%95.png)

