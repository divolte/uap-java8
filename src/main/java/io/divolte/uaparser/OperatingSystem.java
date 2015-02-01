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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OperatingSystem {
    public static final OperatingSystem OTHER = new OperatingSystem("Other", null, null, null, null);
    private final String operatingSystem;

    @Nullable
    private final String majorVersion;
    @Nullable
    private final String minorVersion;
    @Nullable
    private final String patchVersion;
    @Nullable
    private final String patchMinorVersion;

    public OperatingSystem(String operatingSystem,
                           @Nullable String majorVersion,
                           @Nullable String minorVersion,
                           @Nullable String patchVersion,
                           @Nullable String patchMinorVersion) {
        this.operatingSystem = operatingSystem;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        this.patchMinorVersion = patchMinorVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
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

    public String getPatchMinorVersion() {
        return patchMinorVersion;
    }

    public String makeVersionString() {
        return Stream.of(majorVersion, minorVersion, patchVersion, patchMinorVersion)
                .filter((s) -> s != null)
                .collect(Collectors.joining("."));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operatingSystem == null) ? 0 : operatingSystem.hashCode());
        result = prime * result + ((majorVersion == null) ? 0 : majorVersion.hashCode());
        result = prime * result + ((minorVersion == null) ? 0 : minorVersion.hashCode());
        result = prime * result + ((patchMinorVersion == null) ? 0 : patchMinorVersion.hashCode());
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

        OperatingSystem other = (OperatingSystem) obj;
        return Objects.equals(operatingSystem, other.operatingSystem) &&
               Objects.equals(majorVersion, other.majorVersion) &&
               Objects.equals(minorVersion, other.minorVersion) &&
               Objects.equals(patchVersion, other.patchVersion) &&
               Objects.equals(patchMinorVersion, other.patchMinorVersion);
    }

    @Override
    public String toString() {
        return "OperatingSystem [operatingSystem=" + operatingSystem + ", majorVersion=" + majorVersion + ", minorVersion=" + minorVersion + ", patchVersion=" + patchVersion + ", patchMinorVersion="
                + patchMinorVersion + "]";
    }
}
