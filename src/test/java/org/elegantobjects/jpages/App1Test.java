package org.elegantobjects.jpages;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class App1Test extends TestCase {

    @Test
    @Ignore
    public void testWorks() throws Exception {
        final int port = 12345;

        // Create the session with the resource to be served
        final App1.Session session = new App1.Session(
                new App1.Resource() {
                    Map<String, String> params = new HashMap<>();

                    @Override
                    public App1.Resource define(String name, String value) {
                        params.put(name, value);
                        return this;
                    }

                    @Override
                    public void printTo(final App1.Output output) throws IOException {

                        // Simulate a http request that takes 100ms to complete
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Create and write the output
                        String outputString = "Hello, world! query=" +
                                params.getOrDefault("X-Query", "no-query");
                        output.print("Content-Type", "text/plain");
                        output.print("Content-Length", String.valueOf(outputString.length()));
                        output.print("X-Body", outputString);

                        // Print to console
                        System.out.printf("%s: %s\n", LocalDateTime.now(), outputString);
                    }
                }
        );


        final Thread thread = new Thread(() -> {

            // Create the app with the session
            final App1 app = new App1(session);

            try {
                app.start(port);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw new IllegalStateException(ex);
            }
        }
        );
        thread.setDaemon(true);
        thread.start();

        // Send 10 requests to the server
        for (int attempt = 0; attempt < 10; ++attempt) {
            final String response = new JdkRequest("http://localhost:" + port + "/hello")
                    .fetch()
                    .as(RestResponse.class)
                    .body();
            MatcherAssert.assertThat(
                    response,
                    Matchers.containsString("Hello, world!")
            );
        }

        thread.interrupt();
        thread.join();
    }

}