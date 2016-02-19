package bg.uni.sofia.fmi.corejava.log.server.test;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import bg.uni.sofia.fmi.corejava.log.client.LogClient;

public class BarrierConnection implements Runnable {

	CyclicBarrier barrier;

	public BarrierConnection(CyclicBarrier barrier) {
		this.barrier = barrier;
	}

	@Override
	public void run() {

		try {
			Thread t = null;
			try {
				LogClient client = new LogClient();
				t = new Thread(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.barrier.await();
			t.start();
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
