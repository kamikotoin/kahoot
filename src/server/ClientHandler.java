package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket client;
    private final ClientContext ctx;

    public ClientHandler(Socket client) throws IOException {
        this.client = client;
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        this.ctx = new ClientContext(out, client);
        // register writer so NotificationService can broadcast
        NotificationService.getInstance().register(out);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("REQ: " + line);
                String reply = CommandParser.handle(line, ctx);
                // send reply
                ctx.getOut().println(reply);
                ctx.getOut().flush();
            }
        } catch (IOException e) {
            // client disconnected
        } finally {
            NotificationService.getInstance().unregister(ctx.getOut());
            try { client.close(); } catch (IOException ignored) {}
            System.out.println("Client disconnected");
        }
    }
}
