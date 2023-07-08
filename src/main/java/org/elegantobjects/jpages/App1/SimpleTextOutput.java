/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018-2019 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.elegantobjects.jpages.App1;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The output.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @since 0.1
 */
public final class SimpleTextOutput implements Output {

    private final String before;

    public SimpleTextOutput(final String txt) {
        this.before = txt;
    }

    @Override
    public String toString() {
        return this.before;
    }

    @Override
    public Output with(final String name, final String value) {
        final StringBuilder after = new StringBuilder(this.before);
        if (after.length() == 0) {
            after.append("HTTP/1.1 200 OK\r\n");
        }
        if ("X-Body".equals(name)) {
            after.append("\r\n").append(value);
        } else {
            after.append(name).append(": ").append(value).append("\r\n");
        }
        return new SimpleTextOutput(after.toString());
    }

    @Override
    public void writeTo(final OutputStream output) throws IOException {
        output.write(this.before.getBytes());
    }
}
