package com.dengzi.dzokhttp.okhttp3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * description:请求头信息
 * author: Darren on 2017/10/9 14:46
 * email: 240336124@qq.com
 * version: 1.0
 */
public class Headers {
    private final String[] namesAndValues;

    public Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    /** Returns the number of field values. */
    public int size() {
        return namesAndValues.length / 2;
    }

    /** Returns the field at {@code position}. */
    public String name(int index) {
        return namesAndValues[index * 2];
    }

    /** Returns the value at {@code index}. */
    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    /** Returns the last value corresponding to the specified field, or null. */
    public String get(String name) {
        return get(namesAndValues, name);
    }

    /**
     * Returns headers for the alternating header names and values. There must be an even number of
     * arguments, and they must alternate between header names and values.
     */
    public static Headers of(String... namesAndValues) {
        if (namesAndValues == null) throw new NullPointerException("namesAndValues == null");
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }

        // Make a defensive copy and clean it up.
        namesAndValues = namesAndValues.clone();
        for (int i = 0; i < namesAndValues.length; i++) {
            if (namesAndValues[i] == null) throw new IllegalArgumentException("Headers cannot be null");
            namesAndValues[i] = namesAndValues[i].trim();
        }

        // Check for malformed headers.
        for (int i = 0; i < namesAndValues.length; i += 2) {
            String name = namesAndValues[i];
            String value = namesAndValues[i + 1];
            if (name.length() == 0 || name.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
                throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
            }
        }

        return new Headers(namesAndValues);
    }

    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    /** Returns an immutable case-insensitive set of header names. */
    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    public static final class Builder {
        private final List<String> namesAndValues = new ArrayList<>(20);

        /**
         * Add a header line without any validation. Only appropriate for headers from the remote peer
         * or cache.
         */
        Builder addLenient(String line) {
            int index = line.indexOf(":", 1);
            if (index != -1) {
                return addLenient(line.substring(0, index), line.substring(index + 1));
            } else if (line.startsWith(":")) {
                // Work around empty header names and header names that start with a
                // colon (created by old broken SPDY versions of the response cache).
                return addLenient("", line.substring(1)); // Empty header name.
            } else {
                return addLenient("", line); // No header name.
            }
        }

        /** Add an header line containing a field name, a literal colon, and a value. */
        public Builder add(String line) {
            int index = line.indexOf(":");
            if (index == -1) {
                throw new IllegalArgumentException("Unexpected header: " + line);
            }
            return add(line.substring(0, index).trim(), line.substring(index + 1));
        }

        /** Add a field with the specified value. */
        public Builder add(String name, String value) {
            checkNameAndValue(name, value);
            return addLenient(name, value);
        }

        /**
         * Add a field with the specified value without any validation. Only appropriate for headers
         * from the remote peer or cache.
         */
        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i); // name
                    namesAndValues.remove(i); // value
                    i -= 2;
                }
            }
            return this;
        }

        /**
         * Set a field with the specified value. If the field is not found, it is added. If the field is
         * found, the existing values are replaced.
         */
        public Builder set(String name, String value) {
            checkNameAndValue(name, value);
            removeAll(name);
            addLenient(name, value);
            return this;
        }

        private void checkNameAndValue(String name, String value) {
            if (name == null) throw new NullPointerException("name == null");
            if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
            for (int i = 0, length = name.length(); i < length; i++) {
                char c = name.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    throw new IllegalArgumentException(Util.format(
                            "Unexpected char %#04x at %d in header name: %s", (int) c, i, name));
                }
            }
            if (value == null) throw new NullPointerException("value == null");
            for (int i = 0, length = value.length(); i < length; i++) {
                char c = value.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    throw new IllegalArgumentException(Util.format(
                            "Unexpected char %#04x at %d in %s value: %s", (int) c, i, name, value));
                }
            }
        }

        /** Equivalent to {@code build().get(name)}, but potentially faster. */
        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        public Headers build() {
            return new Headers(this);
        }
    }
}
