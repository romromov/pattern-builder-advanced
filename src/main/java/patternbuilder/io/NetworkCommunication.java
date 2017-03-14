package patternbuilder.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Roman Katerinenko
 */
public class NetworkCommunication extends MinimalCommunication {
    private final String host;
    private final int port;
    private final Socket inputSocket;

    protected NetworkCommunication(Builder builder) {
        super(builder);
        host = builder.host;
        port = builder.port;
        inputSocket = builder.inputSocket;
    }

    public Socket getInputSocket() {
        return inputSocket;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void send(byte[] bytes) throws Exception {
        // do nothing
    }

    @Override
    public void close() throws Exception {
        inputSocket.close();
    }

    public static class Builder extends MinimalCommunication.Builder {
        private String host;
        private int port;
        private Socket inputSocket;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public NetworkCommunication build() {
            try {
                inputSocket = new Socket();
                InetSocketAddress address = new InetSocketAddress(host, port);
                inputSocket.bind(address);
                return new NetworkCommunication(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}