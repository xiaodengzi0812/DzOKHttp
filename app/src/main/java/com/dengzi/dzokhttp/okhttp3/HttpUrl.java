package com.dengzi.dzokhttp.okhttp3;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * description: 网络请求路径
 * author: Darren on 2017/10/9 14:49
 * email: 240336124@qq.com
 * version: 1.0
 */
public class HttpUrl {

    /** Either "http" or "https". */
    private final String scheme;

    /** Canonical URL. */
    private final String url;


    private HttpUrl(Builder builder) {
        this.scheme = builder.scheme;
        this.url = builder.url;
    }

    public String getUrl() {
        return url;
    }

    /** Returns this URL as a {@link URL java.net.URL}. */
    public URL url() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e); // Unexpected!
        }
    }

    /**
     * Returns the URL that would be retrieved by following {@code link} from this URL, or null if
     * the resulting URL is not well-formed.
     */
    public HttpUrl resolve(String link) {
        Builder builder = newBuilder(link);
        return builder != null ? builder.build() : null;
    }

    /**
     * Returns a builder for the URL that would be retrieved by following {@code link} from this URL,
     * or null if the resulting URL is not well-formed.
     */
    public Builder newBuilder(String link) {
        Builder builder = new Builder();
        Builder.ParseResult result = builder.parse(this, link);
        return result == Builder.ParseResult.SUCCESS ? builder : null;
    }

    public boolean isHttps() {
        return scheme.equals("https");
    }

    /**
     * Returns a new {@code HttpUrl} representing {@code url} if it is a well-formed HTTP or HTTPS
     * URL, or null if it isn't.
     */
    public static HttpUrl parse(String url) {
        Builder builder = new Builder();
        Builder.ParseResult result = builder.parse(null, url);
        return result == Builder.ParseResult.SUCCESS ? builder.build() : null;
    }

    public static final class Builder {
        String scheme;
        String url;

        public Builder(){

        }

        public HttpUrl build() {
            if (scheme == null) throw new IllegalStateException("scheme == null");
            return new HttpUrl(this);
        }

        public Builder scheme(String scheme) {
            if (scheme == null) {
                throw new NullPointerException("scheme == null");
            } else if (scheme.equalsIgnoreCase("http")) {
                this.scheme = "http";
            } else if (scheme.equalsIgnoreCase("https")) {
                this.scheme = "https";
            } else {
                throw new IllegalArgumentException("unexpected scheme: " + scheme);
            }
            return this;
        }

        /**
         * Returns 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
         * otherwise.
         */
        public static int defaultPort(String scheme) {
            if (scheme.equals("http")) {
                return 80;
            } else if (scheme.equals("https")) {
                return 443;
            } else {
                return -1;
            }
        }

        ParseResult parse(HttpUrl base, String input) {
            this.url = input;
            if (input.startsWith("https:")||input.startsWith("http")) {
                if (input.regionMatches(true, 0, "https:", 0, 6)) {
                    this.scheme = "https";
                } else if (input.regionMatches(true, 0, "http:", 0, 5)) {
                    this.scheme = "http";
                } else {
                    return ParseResult.UNSUPPORTED_SCHEME; // Not an HTTP scheme.
                }
            } else if (base != null) {
                this.scheme = base.scheme;
            } else {
                return ParseResult.MISSING_SCHEME; // No scheme.
            }
            return ParseResult.SUCCESS;
        }

        enum ParseResult {
            SUCCESS,
            MISSING_SCHEME,
            UNSUPPORTED_SCHEME,
        }
    }
}
