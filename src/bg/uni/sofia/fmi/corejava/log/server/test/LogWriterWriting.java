package bg.uni.sofia.fmi.corejava.log.server.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.junit.Test;

import bg.uni.sofia.fmi.corejava.log.server.LogFileWriterThread;
import bg.uni.sofia.fmi.corejava.log.server.LogServer;

public class LogWriterWriting {

	@Test
	public void containsInFile() throws IOException, InterruptedException {

		Path logFile = Paths.get("logs", "logFile.log");

		Files.deleteIfExists(logFile);

		LogFileWriterThread writer = new LogFileWriterThread();

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
		r.nextBytes(data);
		HashMap<SocketChannel, String> connectedClient = new HashMap<>();
		connectedClient.put(socketChannel, id);

		Thread t = new Thread(writer);
		t.start();

		for (int i = 0; i < writer.getNumberOfWritingMsgs(); i++) {
			r.nextBytes(data);
			writer.processData(socketChannel, data, bytes, connectedClient);
		}
		//Thread.sleep(100);

		try (BufferedReader reader = new BufferedReader(new FileReader(
				logFile.toString()))) {
			String line = null;
			String msg = new String(data, Charset.defaultCharset());
			System.out.println(msg);
			Iterator<String> it = reader.lines().iterator();
			while(it.hasNext()){
				line = it.next();
			}
			assertTrue(line.contains(msg));
		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

}
