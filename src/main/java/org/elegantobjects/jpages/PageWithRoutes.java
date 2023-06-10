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
package org.elegantobjects.jpages;

/**
 * The page with a few routes.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @since 0.1
 */
public final class PageWithRoutes implements Page {

    private final String path;

    private final Page success;
    private final Page error;

    PageWithRoutes(final String path, final Page success, final Page error) {
        this.path = path;
        this.success = success;
        this.error = error;
    }

    @Override
    public Page with(final String key, final String value) {
        if (key.equals("X-Path")) {
            if (value.equals(this.path)) {
                return this.success.with(key, value);
            }

            return this.error.with(key, value);
        }

        return this;
    }

    @Override
    public Output printTo(final Output output) {
        return output;
    }
}
