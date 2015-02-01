/*
 * Copyright 2015 GoDataDriven B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.divolte.uaparser;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParserTest {

    @Test
    public void shouldProperlyParseUserAgent() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/minimal-regexes.yaml"));

        String input = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/40.0.2214.93 Safari/537.36";

        UserAgent agent = parser.parseUserAgent(input);
        Device device = parser.parseDevice(input);
        OperatingSystem os = parser.parseOperatingSystem(input);

        assertEquals(new UserAgent("Chrome", "40", "0", "2214"), agent);
        assertEquals(Device.OTHER, device);
        assertEquals(new OperatingSystem("Mac OS X", "10", "10", "2", null), os);
    }

    @Test
    public void shouldParseDevice() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/minimal-regexes.yaml"));
        String input = "iTunes-AppleTV/4.1";
        Device device = parser.parseDevice(input);
        assertEquals(new Device("AppleTV", "Apple", "AppleTV"), device);
    }

    @Test
    public void shouldProvideDefaultReplacements() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/test-regexes.yaml"), true);
        String input = "no_replacements 10.20.30";
        UserAgent agent = parser.parseUserAgent(input);
        assertEquals(new UserAgent("no_replacements", "10", "20", "30"), agent);
    }

    @Test
    public void shouldProvideGivenReplacements() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/test-regexes.yaml"), true);
        String input = "all_replacements 10.20.30";
        UserAgent agent = parser.parseUserAgent(input);
        assertEquals(new UserAgent("xx all_replacements xx", "xx 10 xx", "xx 20 xx 20", "xx 30 xx $$ "), agent);
    }

    @Test
    public void shouldProvideDefaultReplacementsForMissingOnesWhenEnoughGroupsPresent() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/test-regexes.yaml"), true);
        String input = "some_replacements 10.20.30";
        UserAgent agent = parser.parseUserAgent(input);
        assertEquals(new UserAgent("xx some_replacements xx", "xx 10 xx", "20", "30"), agent);
    }

    @Test
    public void shouldNotEnforceDefaultReplacementsIfNotEnoughGroupsAvailable() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/test-regexes.yaml"), true);
        String input = "less_groups 10.20.30";
        UserAgent agent = parser.parseUserAgent(input);
        assertEquals(new UserAgent("xx less_groups xx", "xx 10 xx", null, null), agent);
    }

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void shouldBreakOnNonExistingGroupReference() {
        expected.expect(Parser.InvalidParserDataException.class);
        expected.expectMessage("Replacement 'xx $4 xx' uses a group not captured in regex '(bad_group_ref) (\\d+)\\.(\\d+)'.");
        new Parser(ParserTest.class.getResourceAsStream("/test-regexes.yaml"), false);
    }

    public void shouldGatherBrokenConfigsWhenLenient() {
        Parser parser = new Parser(ParserTest.class.getResourceAsStream("/test-regexes.yaml"), true);
        assertEquals(1, parser.getUserAgentParser().getInvalidConfigurations().size());
    }
}
