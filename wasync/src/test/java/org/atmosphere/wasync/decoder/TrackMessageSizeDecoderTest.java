/*
 * Copyright 2017 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.wasync.decoder;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.atmosphere.wasync.Decoder.Decoded;
import org.atmosphere.wasync.Event;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Sebastian Lövdahl <slovdahl@hibox.fi>
 */
public class TrackMessageSizeDecoderTest {

    private final String DELIMITER = "|";
    private TrackMessageSizeDecoder decoder;

    @AfterMethod
    public void tearDownMethod() throws Exception {
        decoder = null;
    }

    @Test
    public void testWithProtocol() {
        decoder = new TrackMessageSizeDecoder(DELIMITER, true);
        String message = "37|{\"message\":\"ab\",\"time\":1373900488808}";

        Decoded<List<String>> result = decoder.decode(Event.MESSAGE, message);
        assertEquals(result.decoded(), Collections.<String>emptyList());

        List<String> expected = new ArrayList<String>() {
            {
                add("{\"message\":\"ab\",\"time\":1373900488808}");
            }
        };
        Decoded<List<String>> result2 = decoder.decode(Event.MESSAGE, message);
        assertEquals(result2.decoded(), expected);
    }

    @Test
    public void testWithOneMessage() {
        decoder = new TrackMessageSizeDecoder(DELIMITER, false);
        String message = "37|{\"message\":\"ab\",\"time\":1373900488808}";
        List<String> expected = new ArrayList<String>() {
            {
                add("{\"message\":\"ab\",\"time\":1373900488808}");
            }
        };
        Decoded<List<String>> result = decoder.decode(Event.MESSAGE, message);
        assertEquals(result.decoded(), expected);
    }

    @Test
    public void testWithMultipleMessages() {
        decoder = new TrackMessageSizeDecoder(DELIMITER, false);
        String messages = "37|{\"message\":\"ab\",\"time\":1373900488807}37|{\"message\":\"ab\",\"time\":1373900488808}37|{\"message\":\"ab\",\"time\":1373900488810}37|{\"message\":\"ab\",\"time\":1373900488812}37|{\"message\":\"ab\",\"time\":1373900488825}37|{\"message\":\"ab\",\"time\":1373900488827}37|{\"message\":\"ab\",\"time\":1373900488829}37|{\"message\":\"ab\",\"time\":1373900488830}37|{\"message\":\"ab\",\"time\":1373900488831}";
        List<String> expected = new ArrayList<String>() {
            {
                add("{\"message\":\"ab\",\"time\":1373900488807}");
                add("{\"message\":\"ab\",\"time\":1373900488808}");
                add("{\"message\":\"ab\",\"time\":1373900488810}");
                add("{\"message\":\"ab\",\"time\":1373900488812}");
                add("{\"message\":\"ab\",\"time\":1373900488825}");
                add("{\"message\":\"ab\",\"time\":1373900488827}");
                add("{\"message\":\"ab\",\"time\":1373900488829}");
                add("{\"message\":\"ab\",\"time\":1373900488830}");
                add("{\"message\":\"ab\",\"time\":1373900488831}");
            }
        };
        Decoded<List<String>> result = decoder.decode(Event.MESSAGE, messages);
        assertEquals(result.decoded(), expected);
    }

    @Test
    public void testIncompleteMessages() {
        decoder = new TrackMessageSizeDecoder(DELIMITER, false);
        String messages = "37|{\"message\":\"ab\",\"time\":1373900488807}37|{\"message\":\"ab\",\"time\":1373900488808}37|{\"message\":\"ab\",\"time\":1373900488810}37|{\"message\":\"ab\",\"time\":1373900488812}37|{\"message\":\"ab\",\"time\":1373900488825}37|{\"message\":\"ab\",\"time\":1373900488827}37|{\"message\":\"ab\",\"time\":1373900488829}37|{\"message\":\"ab\",\"time\":1373900488830}37|{";
        List<String> expected = new ArrayList<String>() {
            {
                add("{\"message\":\"ab\",\"time\":1373900488807}");
                add("{\"message\":\"ab\",\"time\":1373900488808}");
                add("{\"message\":\"ab\",\"time\":1373900488810}");
                add("{\"message\":\"ab\",\"time\":1373900488812}");
                add("{\"message\":\"ab\",\"time\":1373900488825}");
                add("{\"message\":\"ab\",\"time\":1373900488827}");
                add("{\"message\":\"ab\",\"time\":1373900488829}");
                add("{\"message\":\"ab\",\"time\":1373900488830}");
                add("{\"message\":\"ab\",\"time\":1373900488831}");
            }
        };
        Decoded<List<String>> result = decoder.decode(Event.MESSAGE, messages);
        assertEquals(result.decoded().size(), expected.size() -1);

        result.decoded().addAll(decoder.decode(Event.MESSAGE, "\"message\":\"ab\",\"time\":1373900488831}").decoded());
        assertEquals(result.decoded(), expected);
    }
    
    @Test
    public void testSingleIncompleteMessageSplitOverSeveralWebsocketFrames() {
        decoder = new TrackMessageSizeDecoder(DELIMITER, false);
        String firstMessagePart = "37|{\"message\":\"ab\",";
        String seconMessagedPart ="\"time\":1373900";
        String thirdMessagePart ="488807}";
        List<String> expected = new ArrayList<String>() {
            {
                add("{\"message\":\"ab\",\"time\":1373900488807}");
            }
        };
        Decoded<List<String>> firstDecodeCall = decoder.decode(Event.MESSAGE, firstMessagePart);
        assertEquals(firstDecodeCall.decoded().size(), 0);
        Decoded<List<String>> secondDecodeCall = decoder.decode(Event.MESSAGE, seconMessagedPart);
        assertEquals(secondDecodeCall.decoded().size(), 0);
        Decoded<List<String>> result = decoder.decode(Event.MESSAGE, thirdMessagePart);
        
        result.decoded().addAll(decoder.decode(Event.MESSAGE, "{\"message\":\"ab\",\"time\":1373900488807}").decoded());
        assertEquals(result.decoded(), expected);
    }

    @Test
    public void testCustomDelimiter() {
        decoder = new TrackMessageSizeDecoder("^", false);
        String messages = "37^{\"message\":\"ab\",\"time\":1373900488807}37^{\"message\":\"ab\",\"time\":1373900488808}37^{\"message\":\"ab\",\"time\":1373900488810}";
        List<String> expected = new ArrayList<String>() {
            {
                add("{\"message\":\"ab\",\"time\":1373900488807}");
                add("{\"message\":\"ab\",\"time\":1373900488808}");
                add("{\"message\":\"ab\",\"time\":1373900488810}");
            }
        };
        Decoded<List<String>> result = decoder.decode(Event.MESSAGE, messages);
        assertEquals(result.decoded(), expected);
    }
}