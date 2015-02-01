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

import static java.lang.Integer.*;
import static java.util.Collections.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import org.yaml.snakeyaml.Yaml;

/**
 * Main parser implementation. Parser instances are stateless; after construction it is safe
 * to use instances from multiple threads concurrently.
 */
@ParametersAreNonnullByDefault
@ThreadSafe
public class Parser {
    private final UserAgentParser userAgentParser;
    private final DeviceParser deviceParser;
    private final OperatingSystemParser operatingSystemParser;

    /**
     * Creates a Parser instance based on the the given data file as InputStream. This parser
     * will not be lenient in presence of invalid configurations in the given data file.
     * @param data InputStream that reads a parser data file in Yaml format.
     */
    public Parser(final InputStream data) {
        this(data, false);
    }

    /**
     * Creates a Parser instance based on the the given data file as InputStream. This parser
     * can be configured to be lenient in presence of invalid configurations in the given data file.
     * In such cases, the invalid configurations are ignored. A lenient parser will not throw any
     * exceptions in case of configuration errors.
     * @param data InputStream that reads a parser data file in Yaml format.
     * @param lenient When true, the parser instance will be lenient in presence of invalid configuration.
     */
    public Parser(final InputStream data, final boolean lenient) {
        Yaml yaml = new Yaml();
        @SuppressWarnings("unchecked")
        Map<String,List<Map<String,String>>> regexes = yaml.loadAs(data, Map.class);

        userAgentParser = new UserAgentParser(regexes.getOrDefault("user_agent_parsers", emptyList()), lenient);
        deviceParser = new DeviceParser(regexes.getOrDefault("device_parsers", emptyList()), lenient);
        operatingSystemParser = new OperatingSystemParser(regexes.getOrDefault("os_parsers", emptyList()), lenient);
    }

    public UserAgent parseUserAgent(String input) {
        return userAgentParser.parse(input);
    }

    public Device parseDevice(String input) {
        return deviceParser.parse(input);
    }

    public OperatingSystem parseOperatingSystem(String input) {
        return operatingSystemParser.parse(input);
    }

    public UserAgentParser getUserAgentParser() {
        return userAgentParser;
    }

    public DeviceParser getDeviceParser() {
        return deviceParser;
    }

    public OperatingSystemParser getOperatingSystemParser() {
        return operatingSystemParser;
    }

    public static final class InvalidParserDataException extends RuntimeException {
        private static final long serialVersionUID = -8450027246917077146L;

        public InvalidParserDataException(String message) {
            super(message);
        }
    }

    static Function<Matcher,String> replacer(final String regex, final int groupCount, final String replacement) {
        final Pattern placeholderPattern = Pattern.compile("^\\$(\\d)$");

        /*
         * Split replacement string on $1, $2, $3, etc. Yes, we are using a
         * clever lookaraound trick to make split also return the delimiters.
         */
        final String[] parts = replacement.split("(?:(?=\\$\\d)|(?<=\\$\\d))");

        @SuppressWarnings("unchecked") //Generic types and arrays never really go together
        final Function<Matcher,String>[] partReplacers = Stream.of(parts).<Function<Matcher,String>>map((part) -> {
            final Matcher phMatcher = placeholderPattern.matcher(part);
            if (phMatcher.matches()) {
                final int group = parseInt(phMatcher.group(1));
                if (group > groupCount) {
                    throw new InvalidParserDataException(String.format("Replacement '%s' uses a group not captured in regex '%s'.", replacement, regex));
                }
                return (m) -> m.group(group);
            } else {
                return (m) -> part;
            }
        }).toArray((s) -> new Function[s]);

        return (m) -> Stream.of(partReplacers).map((r) -> r.apply(m)).collect(Collectors.joining());
    }
}

