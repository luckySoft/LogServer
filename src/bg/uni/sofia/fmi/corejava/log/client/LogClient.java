package bg.uni.sofia.fmi.corejava.log.client;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class LogClient implements Runnable {

	private static final int REMOTE_PORT = 10514;
	private static final String REMOTE_HOST = "localhost";

	private Selector selector;

	private ByteBuffer writeBuffer;

	private String id;

	private Scanner scan;

	private boolean exit = false;

	private final int allowedConnectingAttempts = 3;

	private void sendId(SocketChannel socketChannel) {
		
		ByteBuffer writeBuffer = ByteBuffer.allocate(256);
		writeBuffer.put(id.getBytes());
		writeBuffer.flip();
		try {
			socketChannel.write(writeBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private boolean tryReconnect(SocketChannel socketChannel) {
		try {
			socketChannel.finishConnect();
			this.sendId(socketChannel);
		} catch (IOException e) {
			System.out.println("Could not succeeded to finish the connection! ");
			return false;
		}
		return true;
	}

	

	private void initClient() throws IOException {

		this.selector = Selector.open();

		SocketChannel socketChannel = SocketChannel.open();

		socketChannel.configureBlocking(false);

		InetSocketAddress address = new InetSocketAddress(REMOTE_HOST,REMOTE_PORT);

		socketChannel.connect(address);

		socketChannel.register(selector, SelectionKey.OP_CONNECT);

		System.out.println("Client " + socketChannel + " connected to server");

	}
	
	private boolean write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		if (scan.hasNextLine()) {
			writeBuffer.clear();
			String message = scan.nextLine();
			if ("quit".equalsIgnoreCase(message.trim())) {
				return true;
			}
			writeBuffer.put(message.getBytes());
			writeBuffer.flip();
			socketChannel.write(writeBuffer);
		}
		return false;
	}

	private void connect(SelectionKey key) {

		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			System.out.println("Could not succeeded to finish connection! "
					+ e.getMessage());
			key.cancel();
			System.out.println("Starting attempts to reconnect...");
			boolean success = false;
			for (int i = 0; i < allowedConnectingAttempts; i++) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
				if (this.tryReconnect(socketChannel)) {
					success = true;
					break;
				}
			}
			if (!success) {
				exit = true;
				return;
			}
		}
		key.interestOps(SelectionKey.OP_WRITE);
		this.sendId(socketChannel);
	}

	public LogClient() throws IOException {
		this.id = ManagementFactory.getRuntimeMXBean().getName();
		this.writeBuffer = ByteBuffer.allocate(1024);
		this.initClient();
		this.scan = new Scanner(System.in);

	}

	@Override
	public void run() {
		while (true) {
			try {

				int num = this.selector.select();

				if (num == 0) {
					continue;
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();

				while (it.hasNext()) {
					SelectionKey key = it.next();
					if (!key.isValid()) {
						continue;
					}
					if (key.isConnectable()) {
						this.connect(key);

					} else if (key.isWritable()) {
						this.exit = this.write(key);
					}
					it.remove();
				}
			} catch (Exception e) {
				System.out.println("Something went wrong! ");
				boolean success = false;
				for (int i = 0; i < allowedConnectingAttempts; i++) {
					try {
						this.initClient();
					} catch (IOException e1) {
						System.out.println("System error occurs!");
						exit = true;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					try {
						selector.select();
					} catch (IOException e1) {
					}
					
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> it = selectedKeys.iterator();
					SelectionKey key = it.next();
					if (this.tryReconnect((SocketChannel)key.channel())) {
						success = true;
						break;
					}
				}
				if(!success){
					this.exit = true;
				}
			}
			if (this.exit) {
				break;
			}
		}
		System.out.println("Client is turned off! ");
	}

	public static void main(String[] args) {
		try {
			LogClient client = new LogClient();
			Thread t = new Thread(client);
			t.start();
		} catch (IOException e) {
			System.out.println("Could not manage to start client application! "
					+ e.getMessage());
		}

	}

}
