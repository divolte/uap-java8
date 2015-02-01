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

import static io.divolte.uaparser.Parser.*;
import io.divolte.uaparser.Parser.InvalidParserDataException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

@ParametersAreNonnullByDefault
@ThreadSafe
public class UserAgentParser {
    private final boolean lenient;
    private final List<UserAgentPattern> patterns;
    private final List<Map<String,String>> invalidConfigs;


    public UserAgentParser(final List<Map<String, String>> configs) {
        this(configs, false);
    }

    public UserAgentParser(final List<Map<String,String>> configs, boolean lenient) {
        this.lenient = lenient;
        patterns = new ArrayList<UserAgentParser.UserAgentPattern>(configs.size());
        invalidConfigs = lenient ? new ArrayList<Map<String,String>>() : Collections.emptyList();

        configs.forEach((config) -> {
            try {
                patterns.add(prepareParser(config));
            } catch(InvalidParserDataException ipde) {
                if (lenient) {
                    invalidConfigs.add(config);
                } else {
                    throw ipde;
                }
            }
        });
    }

    public UserAgent parse(String input) {
        return patterns.stream()
                       .filter((p) -> p.pattern.asPredicate().test(input))
                       .findFirst().map((p) -> {
                           final Matcher matcher = p.pattern.matcher(input);
                           matcher.find();
                           return new UserAgent(
                                   p.familyReplacer.apply(matcher),
                                   p.majorVersionReplacer.apply(matcher),
                                   p.minorVersionReplacer.apply(matcher),
                                   p.patchVersionReplacer.apply(matcher)
                                   );
                       }).orElse(UserAgent.OTHER);
    }

    public List<Map<String,String>> getInvalidConfigurations() {
        if (!lenient) {
            throw new Parser.InvalidParserDataException("Parser needs to be lenient in order to keep track of invalid configurations.");
        }
        return invalidConfigs;
    }

    public boolean isLenient() {
        return lenient;
    }

    private static UserAgentPattern prepareParser(Map<String,String> config) {
        final String regex = Optional.ofNullable(config.get("regex"))
                                     .orElseThrow(() -> new Parser.InvalidParserDataException("User agent parser data contains entry without regex key."));
        final Pattern pattern = Pattern.compile(regex);
        // Pattern does not expose the group count, so we must create a matcher
        final int groupCount = pattern.matcher("").groupCount();

        Optional.ofNullable(config.get("v1_replacement"))
                .map((r) -> replacer(regex, groupCount, r))
                .orElse(groupCount >= 2 ? replacer(regex, groupCount, "$2") : (m) -> null);

        return new UserAgentPattern(
                pattern,
                replacer(regex, groupCount, config.getOrDefault("family_replacement", "$1")),
                Optional.ofNullable(config.get("v1_replacement"))
                        .map((r) -> replacer(regex, groupCount, r))
                        .orElse(groupCount >= 2 ? (m) -> m.group(2) : (m) -> null),
                Optional.ofNullable(config.get("v2_replacement"))
                        .map((r) -> replacer(regex, groupCount, r))
                        .orElse(groupCount >= 3 ? (m) -> m.group(3) : (m) -> null),
                Optional.ofNullable(config.get("v3_replacement"))
                        .map((r) -> replacer(regex, groupCount, r))
                        .orElse(groupCount >= 4 ? (m) -> m.group(4) : (m) -> null));
    }

    private static final class UserAgentPattern {
        private final Pattern pattern;
        private final Function<Matcher,String> familyReplacer;
        private final Function<Matcher,String> majorVersionReplacer;
        private final Function<Matcher,String> minorVersionReplacer;
        private final Function<Matcher,String> patchVersionReplacer;

        private UserAgentPattern(Pattern pattern,
                                 Function<Matcher, String> familyReplacer,
                                 Function<Matcher, String> majorVersionReplacer,
                                 Function<Matcher, String> minorVersionReplacer,
                                 Function<Matcher, String> patchVersionReplacer) {
            this.pattern = pattern;
            this.familyReplacer = familyReplacer;
            this.majorVersionReplacer = majorVersionReplacer;
            this.minorVersionReplacer = minorVersionReplacer;
            this.patchVersionReplacer = patchVersionReplacer;
        }
    }
}
