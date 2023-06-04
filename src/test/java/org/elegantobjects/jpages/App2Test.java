package org.elegantobjects.jpages;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class App2Test extends TestCase {

    @Test
    @Ignore
    public void testWorks() throws Exception {
        final int port = 12345;

        // Create the session with the resource to be served
        final App2.Session session = new App2.Session(
            new App2.Resource() {
                @Override
                public App2.Resource define(String name, String value) {
                    return this;
                }

                @Override
                public void print(final App2.Output output) throws IOException {
                    output.print("Content-Type", "text/plain");
                    output.print("Content-Length", "13");
                    output.print("X-Body", "Hello, world!");
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
            final String response = new JdkRequest("http://localhost:" + port)
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