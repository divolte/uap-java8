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

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import com.sun.istack.internal.Nullable;

@ParametersAreNonnullByDefault
public class UserAgent {
    public static final UserAgent OTHER = new UserAgent("Other", null, null, null);

    private final String family;
    private final String majorVersion;

    @Nullable
    private final String minorVersion;
    @Nullable
    private final String patchVersion;

    public UserAgent(String family, @Nullable String majorVersion, @Nullable String minorVersion, @Nullable String patchVersion) {
        this.family = family;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
    }

    public String getFamily() {
        return family;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public String getPatchVersion() {
        return patchVersion;
    }

    public String makeVersionString() {
        return Stream.of(majorVersion, minorVersion, patchVersion)
                     .filter((s) -> s != null)
                     .collect(Collectors.joining("."));
    }

    @Override
    public String toString() {
        return "UserAgent [family=" + family + ", majorVersion=" + majorVersion + ", minorVersion=" + minorVersion + ", patchVersion=" + patchVersion + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((family == null) ? 0 : family.hashCode());
        result = prime * result + ((majorVersion == null) ? 0 : majorVersion.hashCode());
        result = prime * result + ((minorVersion == null) ? 0 : minorVersion.hashCode());
        result = prime * result + ((patchVersion == null) ? 0 : patchVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserAgent other = (UserAgent) obj;
        return Objects.equals(family, other.family) &&
               Objects.equals(majorVersion, other.majorVersion) &&
               Objects.equals(minorVersion, other.minorVersion) &&
               Objects.equals(patchVersion, other.patchVersion);
    }
}
