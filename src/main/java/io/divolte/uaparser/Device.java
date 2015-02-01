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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class Device {
    public static final Device OTHER = new Device("Other", null, "Other");

    private final String family;

    @Nullable
    private final String brand;
    @Nullable
    private final String model;

    public Device(final String family, @Nullable final String brand, @Nullable final String model) {
        this.family = family;
        this.brand = brand;
        this.model = model;
    }

    public String getFamily() {
        return family;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brand == null) ? 0 : brand.hashCode());
        result = prime * result + ((family == null) ? 0 : family.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
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
        Device other = (Device) obj;

        return Objects.equals(family, other.family) &&
               Objects.equals(brand, other.brand) &&
               Objects.equals(model, other.model);
    }

    @Override
    public String toString() {
        return "Device [family=" + family + ", brand=" + brand + ", model=" + model + "]";
    }
}
