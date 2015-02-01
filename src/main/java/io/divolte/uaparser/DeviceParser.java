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
public class DeviceParser {
    private final List<DevicePattern> patterns;
    private final boolean lenient;
    private final List<Map<String,String>> invalidConfigs;

    public DeviceParser(List<Map<String,String>> configs) {
        this(configs, false);
    }

    public DeviceParser(List<Map<String,String>> configs, boolean lenient) {
        this.lenient = lenient;

        patterns = new ArrayList<DeviceParser.DevicePattern>(configs.size());
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

    public Device parse(String input) {
        return patterns.stream()
                .filter((p) -> p.pattern.asPredicate().test(input))
                .findFirst().map((p) -> {
                    final Matcher matcher = p.pattern.matcher(input);
                    matcher.find();
                    return new Device(
                            p.familyReplacer.apply(matcher),
                            p.brandReplacer.apply(matcher),
                            p.modelReplacer.apply(matcher)
                            );
                }).orElse(Device.OTHER);
    }

    public List<Map<String,String>> getInvalidConfigurations() {
        if (!lenient) {
            throw new IllegalStateException("Parser needs to be lenient in order to keep track of invalid configurations.");
        }
        return invalidConfigs;
    }

    public boolean isLenient() {
        return lenient;
    }

    private static DevicePattern prepareParser(Map<String,String> config) {
        final String regex = Optional.ofNullable(config.get("regex"))
                                     .orElseThrow(() -> new Parser.InvalidParserDataException("Device parser data contains entry without regex key."));
        final Pattern pattern = Pattern.compile(regex);
        // Pattern does not expose the group count, so we must create a matcher
        final int groupCount = pattern.matcher("").groupCount();

        return new DevicePattern(
                pattern,
                replacer(regex, groupCount, config.getOrDefault("device_replacement", "$1")),
                Optional.ofNullable(config.get("brand_replacement"))
                        .map((r) -> replacer(regex, groupCount, r))
                        .orElse((m) -> null),
                replacer(regex, groupCount, config.getOrDefault("model_replacement", "$1"))
                );
    }

    private static final class DevicePattern {
        private final Pattern pattern;
        private final Function<Matcher,String> familyReplacer;
        private final Function<Matcher,String> brandReplacer;
        private final Function<Matcher,String> modelReplacer;
        private DevicePattern(Pattern pattern,
                              Function<Matcher, String> familyReplacer,
                              Function<Matcher, String> brandReplacer,
                              Function<Matcher, String> modelReplacer) {
            this.pattern = pattern;
            this.familyReplacer = familyReplacer;
            this.brandReplacer = brandReplacer;
            this.modelReplacer = modelReplacer;
        }
    }
}
