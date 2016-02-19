package bg.uni.sofia.fmi.corejava.log.server.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;

import org.junit.Test;

import bg.uni.sofia.fmi.corejava.log.server.LogFileWriterThread;
import bg.uni.sofia.fmi.corejava.log.server.LogServer;

public class LogWriterCreateFile {

	@Test
	public void createFile() throws IOException {
		Path logFile = Paths.get("logs", "logFile.log");

		Files.deleteIfExists(logFile);

		LogFileWriterThread writer = new LogFileWriterThread();
		Thread t = new Thread(writer);
		t.start();

		String id = ManagementFactory.getRuntimeMXBean().getName();

		Selector selector = Selector.open();
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(LogServer.HOST,LogServer.PORT);
		socketChannel.connect(address);
		socketChannel.register(selector, SelectionKey.OP_CONNECT);

		Random r = new Random();
		int bytes = r.nextInt(10) + 1;
		byte[] data = new byte[bytes];
		HashMap<SocketChannel, String> connectedClient = new HashMap<>();
		connectedClient.put(socketChannel, id);

		for (int i = 0; i < writer.getNumberOfWritingMsgs(); i++) {
			r.nextBytes(data);
			writer.processData( socketChannel, data, bytes, connectedClient);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertTrue(Files.exists(logFile));
	}

}
