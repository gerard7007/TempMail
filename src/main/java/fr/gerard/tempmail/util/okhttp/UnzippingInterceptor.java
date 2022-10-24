package fr.gerard.tempmail.util.okhttp;

import okhttp3.*;
import okhttp3.internal.http.RealResponseBody;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import org.brotli.dec.BrotliInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class UnzippingInterceptor implements Interceptor {

    private final boolean useCompression;

    public UnzippingInterceptor(boolean useCompression) {
        this.useCompression = useCompression;
    }

    public UnzippingInterceptor() {
        this(true);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        if (useCompression) requestBuilder.header("accept-encoding", "gzip, deflate, br");
        Response response = chain.proceed(requestBuilder.build());
        return unzip(response);
    }

    private Response unzip(final Response response) throws IOException {
        ResponseBody body;

        if ((body = response.body()) == null) {
            return response;
        }

        long contentLength = body.contentLength();
        String contentEncoding = response.headers().get("Content-Encoding");
        Headers strippedHeaders = response.headers().newBuilder().build();

        if (contentEncoding != null) {

            BufferedSource source;

            switch (contentEncoding) {
                case "gzip":
                    GzipSource responseBody = new GzipSource(response.body().source());
                    source = Okio.buffer(responseBody);
                    break;
                case "br":
                    BrotliInputStream bis = new BrotliInputStream(body.byteStream());
                    source = Okio.buffer(Okio.source(bis));
                    break;
                default:
                    return response;
            }

            return response.newBuilder().headers(strippedHeaders)
                    .body(new RealResponseBody(Objects.requireNonNull(body.contentType(), "\"content-type\" is null").toString(), contentLength, Okio.buffer(source)))
                    .build();
        } else {
            return response;
        }


    }
}