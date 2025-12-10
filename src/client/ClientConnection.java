package client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ClientConnection {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Queue for replies to sendRequest()
    private final BlockingQueue<String> replyQueue = new ArrayBlockingQueue<>(10);

    // Handler for EVENT messages
    private volatile Consumer<String> eventHandler;

    private Thread readerThread;

    public ClientConnection() {
        // no-op, call connect() to open
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        startReaderThread();
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("EVENT|")) {
                        Consumer<String> h = eventHandler;
                        if (h != null) {
                            try { h.accept(line); } catch (Exception ex) { ex.printStackTrace(); }
                        }
                    } else {
                        // deliver to waiting request(s)
                        // if queue is full, remove oldest to make room
                        while (!replyQueue.offer(line)) {
                            replyQueue.poll();
                        }
                    }
                }
            } catch (IOException e) {
                // socket closed
            }
        }, "ClientConnection-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Send a request and wait for a reply.
     * Returns null on timeout or if connection closed.
     */
    public String sendRequest(String req) throws IOException {
        if (socket == null || socket.isClosed()) throw new IOException("Not connected");
        // send
        out.println(req);
        out.flush();
        try {
            // wait up to 5 seconds for a reply
            String r = replyQueue.poll(5, TimeUnit.SECONDS);
            return r;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Install handler for server-pushed events (lines starting with "EVENT|").
     * Can be called at any time.
     */
    public void startListener(Consumer<String> handler) {
        this.eventHandler = handler;
    }

    public void close() {
        try { if (socket!=null) socket.close(); } catch (Exception ignored) {}
    }
}
