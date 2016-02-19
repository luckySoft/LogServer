package bg.uni.sofia.fmi.corejava.log.server.test;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;


public class TestClient {

	public static void connectClients() {
		int barriersCount = 200;
		CyclicBarrier barrier = new CyclicBarrier(barriersCount);
		for (int i = 0; i < barrier.getParties(); i++) {
			new Thread(new BarrierConnection(barrier)).start();
		}
	}

	public static void writeMessages() {
		int barriersCount = 1000;
		CyclicBarrier barrier = new CyclicBarrier(barriersCount);
		for (int i = 0; i < barrier.getParties(); i++) {
			new Thread(new BarrierWriter(barrier)).start();
		}
	}
	
	
	private static void connectAndSend() throws IOException {
		int barriersCount = 2;
		CyclicBarrier barrier = new CyclicBarrier(barriersCount);
		for (int i = 0; i < barrier.getParties(); i++) {
			new Thread(new BarrierConAndSend(barrier)).start();
		}
	}
	
	//@Test
	public static void main(String args[]) {
		System.out.println("START SERVER FIRST!!!");
		System.out.println("case 1: connecting clients");
		System.out.println("case 2: writing messages");
		System.out.println("case 3: connect and write at the same time");
		Scanner scan = new Scanner(System.in);
		int caseTest = scan.nextInt();
		switch (caseTest) {
		case 1:
			connectClients();
			break;
		case 2:
			writeMessages();
			break;
		case 3:
			try {
				connectAndSend();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("wrong operation");
			break;
		}
		scan.close();
	}

}
