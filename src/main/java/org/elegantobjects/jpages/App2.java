package org.elegantobjects.jpages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class App2 extends IOException {

    private static final long serialVersionUID = 0x7523L;

    interface Resource {
        Resource define(String name, String value);
        void print(Output output) throws IOException;
    }

    interface Output {
        void print(String name, String value) throws IOException;
    }

    private final Session session;

    public App2(Session session) {
        this.session = session;
    }

    public static class Session {

        Resource resource;

        public Session(Resource resource) {
            this.resource = resource;
        }

        String request(String request) {
            Map<String, String> params = new HashMap<>();
            Resource resource = this.resource;

            // Parse request into params map
            String[] lines = request.split("\r\n");
            for(String line: lines) {
                String[] parts = line.split(":");
                if(parts.length == 2) {
                    params.put(parts[0].trim(), parts[1].trim());
                }
            }

            // Extract method, query, protocol
            String[] parts = lines[0].split(" ");
            params.put("X-Method", parts[0]);
            params.put("X-Query", parts[1]);
            params.put("X-Protocol", parts[2]);

            // Populate request into KV pairs
            for(Map.Entry<String, String> entry : params.entrySet()) {
                this.resource.define(entry.getKey(), entry.getValue());
            }

            // Make the request & return the response
            try {
                final StringBuilder builder = new StringBuilder();
                final StringBuilderOutput output = new StringBuilderOutput(builder);

                // Make the request
                resource.print(output);

                return builder.toString();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static class StringBuilderOutput implements Output {
        private StringBuilder builder = new StringBuilder();

        public StringBuilderOutput(StringBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void print(final String name, final String value) {
            // For first line, add the status
            if(builder.length() == 0) {
                builder.append("HTTP/1.1 200 OK\r\n");
            }

            // If body, add blank line
            if(name.equals("X-Body")) {
                builder.append("\r\n")
                        .append(value);
            } else {
                // add a header
                builder.append(name)
                        .append(": ")
                        .append(value)
                        .append("\r\n");
            }
        }
    }


    // Start the server
    void start(int port) throws IOException {
        try(ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(1000);

            // Handle a single request
            while(true) {
                try(Socket socket = server.accept()) {
                    if (Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    InputStream input = socket.getInputStream();
                    OutputStream output = socket.getOutputStream();
                    final byte[] buffer = new byte[1024];

                    int length = input.read(buffer);
                    String request = new String(Arrays.copyOfRange(buffer, 0, length));

                    String response = this.session.request(request);
                    output.write(response.getBytes());

                } catch (final SocketTimeoutException ex) {
                    break;
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                } catch (Exception e) {
                    throw e;
                }

            }
        }
    }

//    public static void main() {
//        final App2 app = new App2(
//            new Resource() {
//                private String name;
//                private String value;
//
//                @Override
//                public Resource define(String name, String value) {
//                    this.name = name;
//                    this.value = value;
//                    return this;
//                }
//
//                @Override
//                public void print() throws IOException {
//                    final Output2 output = new Output2() {
//                        @Override
//                        public void print(String name, String value) throws IOException {
//                            output.write(name);
//                            output.write(": ");
//                            output.write(value);
//                            output.write("\r\n");
//                        }
//                    };
//                    output.print(this.name, this.value);
//                }
//            }
//        );
//
//        try {
//            app.start(8080);
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }
//    }

}