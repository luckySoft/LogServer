package bg.uni.sofia.fmi.corejava.log.server.test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import bg.uni.sofia.fmi.corejava.log.server.LogServer;

public class BarrierConAndSend implements Runnable {

	private CyclicBarrier barrier;
	private Selector selector;
	private SocketChannel socketChannel;
	private String id;

	private void init() throws IOException {
		this.selector = Selector.open();
		this.socketChannel = SocketChannel.open();
		this.socketChannel.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(LogServer.HOST,LogServer.PORT);
		this.socketChannel.connect(address);
		this.socketChannel.register(selector, SelectionKey.OP_CONNECT);
		this.socketChannel.finishConnect();
		this.id = ManagementFactory.getRuntimeMXBean().getName();
		ByteBuffer writeBuffer = ByteBuffer.allocate(256);
		writeBuffer.put(id.getBytes());
		writeBuffer.flip();
		try {
			socketChannel.write(writeBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BarrierConAndSend(CyclicBarrier barrier) throws IOException {
		this.barrier = barrier;
		this.selector = Selector.open();
	}

	@Override
	public void run() {

		try {
			this.barrier.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			this.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(20);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ByteBuffer writeBuffer = ByteBuffer.allocate(256);
		Random r = new Random();
		byte[] bytes = new byte[10];
		r.nextBytes(bytes);
		writeBuffer.put(bytes);
		writeBuffer.flip();
		try {
			this.socketChannel.write(writeBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
