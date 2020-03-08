package com.demo.api.model;

import java.util.Objects;

public abstract class Id {
    private final String value;

    protected Id(String value) {
        this.value = Objects.requireNonNull(value, "Id value must be provided");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return Objects.equals(value, id.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
