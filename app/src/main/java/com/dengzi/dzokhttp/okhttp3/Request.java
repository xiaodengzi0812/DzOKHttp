package com.dengzi.dzokhttp.okhttp3;

/**
 * @author Djk
 * @Title:网络请求
 * @Time: 2017/11/24.
 * @Version:1.0.0
 */
public class Request {
    private final HttpUrl url;
    private final Method method;
    private final Headers headers;
    private final Object tag;
    private final RequestBody requestBody;

    private Request(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers.build();
        this.tag = builder.tag != null ? builder.tag : this;
        this.requestBody = builder.requestBody;
    }

    public boolean isHttps() {
        return url.isHttps();
    }

    public HttpUrl url() {
        return url;
    }

    public Method getMethod() {
        return method;
    }

    public Headers getHeaders() {
        return headers;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public static class Builder {
        private HttpUrl url;
        private Method method;
        private Headers.Builder headers;
        private Object tag;
        private RequestBody requestBody;

        public Builder() {
            this.method = Method.GET;
            this.headers = new Headers.Builder();
        }

        private Builder(Request request) {
            this.url = request.url;
            this.method = request.method;
            this.tag = request.tag;
            this.headers = request.headers.newBuilder();
        }

        public Builder url(HttpUrl url) {
            if (url == null) throw new NullPointerException("url == null");
            this.url = url;
            return this;
        }

        /**
         * Sets the header named {@code name} to {@code value}. If this request already has any headers
         * with that name, they are all replaced.
         */
        public Builder header(String name, String value) {
            headers.set(name, value);
            return this;
        }

        /**
         * Adds a header with {@code name} and {@code value}. Prefer this method for multiply-valued
         * headers like "Cookie".
         *
         * <p>Note that for some headers including {@code Content-Length} and {@code Content-Encoding},
         * OkHttp may replace {@code value} with a header derived from the request body.
         */
        public Builder addHeader(String name, String value) {
            headers.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            headers.removeAll(name);
            return this;
        }

        /**
         * Sets the URL target of this request.
         *
         * @throws IllegalArgumentException if {@code url} is not a valid HTTP or HTTPS URL. Avoid this
         *                                  exception by calling {@link HttpUrl#parse}; it returns null for invalid URLs.
         */
        public Builder url(String url) {
            if (url == null) throw new NullPointerException("url == null");

            // Silently replace websocket URLs with HTTP URLs.
            if (url.regionMatches(true, 0, "ws:", 0, 3)) {
                url = "http:" + url.substring(3);
            } else if (url.regionMatches(true, 0, "wss:", 0, 4)) {
                url = "https:" + url.substring(4);
            }

            HttpUrl parsed = HttpUrl.parse(url);
            if (parsed == null) throw new IllegalArgumentException("unexpected url: " + url);
            return url(parsed);
        }

        public Builder tag(Object tag) {
            this.tag = tag;
            return this;
        }

        public Request build() {
            return new Request(this);
        }

        public Builder post(RequestBody requestBody) {
            this.method = Method.POST;
            this.requestBody = requestBody;
            return this;
        }
    }
}
