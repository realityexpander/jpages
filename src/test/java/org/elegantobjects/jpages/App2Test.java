package org.elegantobjects.jpages;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class App2Test extends TestCase {

    @Test
    @Ignore
    public void testWorks() throws Exception {
        final int port = 12345;

        // Create the session with the resource to be served
        final App2.Session session = new App2.Session(
            new App2.Resource() {
                Map<String, String> params = new HashMap<>();

                @Override
                public App2.Resource define(String name, String value) {
                    params.put(name, value);
                    return this;
                }

                @Override
                public void printTo(final App2.Output output) throws IOException {
                    String outputString = "Hello, world! " +
                            params.getOrDefault("X-Query", "no-query");

                    output.print("Content-Type", "text/plain");
                    output.print("Content-Length", String.valueOf(outputString.length()));
                    output.print("X-Body", outputString);
                }
            }
        );


        final Thread thread = new Thread( () -> {

                // Create the app with the session
                final App2 app = new App2(
                    session
                );

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