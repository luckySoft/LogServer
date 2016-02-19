package bg.uni.sofia.fmi.corejava.log.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class LogServer implements Runnable, AutoCloseable {

	public static final int PORT = 10514;
	public static final String HOST = "localhost";
	private Selector selector;
	private ByteBuffer readBuffer;
	private LogFileWriterThread fileWorker;
	private HashMap<SocketChannel, String> connectedClients = new HashMap<>();

	private void initializeServer() throws IOException {

		selector = Selector.open();

		ServerSocketChannel serverChannel = ServerSocketChannel.open();

		serverChannel.configureBlocking(false);

		ServerSocket serverSocket = serverChannel.socket();

		InetSocketAddress address = new InetSocketAddress(HOST, PORT);

		serverSocket.bind(address);

		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		readBuffer = ByteBuffer.allocate(1024);

		System.out.println("LogServer is listening on port " + PORT);

	}

	private void accept(SelectionKey key) throws IOException {

		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
				.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		socketChannel.register(this.selector, SelectionKey.OP_READ);

		System.out.println("Client " + socketChannel + " connected");

	}

	private void read(SelectionKey key) throws IOException {

		SocketChannel socketChannel = (SocketChannel) key.channel();
		if (!connectedClients.containsKey(socketChannel)) {
			readBuffer.clear();

			int num = socketChannel.read(readBuffer);
			if (num == -1) {
				throw new IOException();
			}
			readBuffer.flip();

			StringBuilder id = new StringBuilder();
			while (readBuffer.hasRemaining()) {
				id.append((char) readBuffer.get());
			}
			String identificator = id.toString();
			connectedClients.put(socketChannel, identificator);
			System.out.println("ID: " + identificator);
			return;
		}
		try {
			this.readBuffer.clear();

			int numRead;
			try {
				numRead = socketChannel.read(this.readBuffer);
			} catch (IOException e) {
				System.out.println("Client " + socketChannel + " quit");
				connectedClients.remove(socketChannel);
				key.cancel();
				socketChannel.close();
				return;
			}

			if (numRead == -1) {
				System.out
						.println("Broken Channel! It did not receive any messages");
				key.channel().close();
				key.cancel();
				return;
			}
			this.fileWorker.processData(socketChannel, this.readBuffer.array(),
					numRead, connectedClients);

			System.out.println("Client " + socketChannel + " wrote " + numRead);

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			System.out
					.println("The channel is broken! Server is attempting to close");
			try {
				socketChannel.close();
			} catch (IOException e) {
				// Nothing that we can do
			}
			key.cancel();
			return;
		}
	}

	public LogServer() throws IOException {
		this.initializeServer();
		this.fileWorker = new LogFileWriterThread();
		new Thread(fileWorker).start();
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
					if (key.isAcceptable()) {
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					}
					it.remove();
				}
			} catch (Exception e) {
				System.out.println("Something went wrong with the connection!"
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws Exception {
		if (selector != null) {
			try {
				selector.close();
				System.out.println("Server stopped");
			} catch (IOException e) {
				// Nothing that we can do
			}
		}
	}

	public static void main(String[] args) {
		try {
			new Thread(new LogServer()).start();
		} catch (IOException e) {
			System.out.println("Failed in starting server! " + e.getMessage());
		}
	}

}
