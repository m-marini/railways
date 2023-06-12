/*
 * Copyright (c) 2023  Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.railways2.swing;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Decodes the label for the station elements
 */
public interface StationLabels {
    ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(StationLabels.class.getPackageName() + ".messages");

    /**
     * Returns the label for the given id
     *
     * @param id the identifier
     */
    static String getLabel(String stationId, String id) {
        String id1 = id;
        String prefix = "station." + stationId + ".";
        for (; ; ) {
            try {
                return RESOURCE_BUNDLE.getString(prefix + id1);
            } catch (MissingResourceException ignored) {
            }
            int idx = id1.lastIndexOf(".");
            if (idx < 0) {
                return id;
            }
            id1 = id1.substring(0, idx);
        }
    }
}
