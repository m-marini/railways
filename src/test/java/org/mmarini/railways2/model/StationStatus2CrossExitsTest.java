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

package org.mmarini.railways2.model;

import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.routes.Section;
import org.mmarini.railways2.swing.StationExamples;
import org.mmarini.railways2.swing.WithTrain;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class StationStatus2CrossExitsTest extends WithStationStatusTest {
    void createStatus(boolean through) {
        status = new WithTrain(StationExamples.create2CrossExitStation(through))
                .addTrain(10, "a", "e", "ab", "b", 0)
                .addTrain(3, "a", "j", "gh", "h", 0)
                .build();
    }

    @Test
    void isSectionClear() {
        // Given ...
        createStatus(false);

        // When ... Then ...
        assertFalse(status.isSectionClear(edge("gh")));
        assertFalse(status.isSectionClear(edge("bc")));
    }
}
