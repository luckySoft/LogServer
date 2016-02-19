package bg.uni.sofia.fmi.corejava.log.server.test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import bg.uni.sofia.fmi.corejava.log.server.LogFileWriterThread;

public class BarrierWriter implements Runnable{

	private CyclicBarrier barrier;
	
	
	public BarrierWriter(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	@Override
	public void run() {
		Thread t = null;
		LogFileWriterThread writer = new LogFileWriterThread();
		t = new Thread(writer);
		
		String id = ManagementFactory.getRuntimeMXBean().getName();
		SocketChannel sc = null;
		try {
			sc = SocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int bytes = 10;
		byte[] data = new byte[bytes];
		Random r = new Random();
		r.nextBytes(data);
		HashMap<SocketChannel, String> connectedClient = new HashMap<>();
		connectedClient.put(sc, id);
		
		try {
			this.barrier.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.start();
		writer.processData(sc, data, bytes, connectedClient);
	}

}
