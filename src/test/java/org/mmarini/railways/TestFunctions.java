/*
 * MIT License
 *
 * Copyright (c) 2022 Marco Marini
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.mmarini.railways;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public interface TestFunctions {

    /*
    @Deprecated
    static Matcher<Object> section(RouteDirection terminal0, RouteDirection terminal1, Edge... edges) {
        return allOf(
                isA(org.mmarini.railways1.model.routes.Section.class),
                anyOf(
                        allOf(
                                hasProperty("terminal0", equalTo(Optional.ofNullable(terminal0))),
                                hasProperty("terminal1", equalTo(Optional.ofNullable(terminal1)))
                        ),
                        allOf(
                                hasProperty("terminal0", equalTo(Optional.ofNullable(terminal1))),
                                hasProperty("terminal1", equalTo(Optional.ofNullable(terminal0)))
                        )
                ),
                hasProperty("edges", containsInAnyOrder(edges))
        );
    }
*/

    static String text(String... lines) {
        return String.join("\n", lines) + "\n";
    }
}
