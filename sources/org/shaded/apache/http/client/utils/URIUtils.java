package org.shaded.apache.http.client.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Stack;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.annotation.Immutable;

@Immutable
public class URIUtils {
    public static URI createURI(String scheme, String host, int port, String path, String query, String fragment) throws URISyntaxException {
        StringBuilder buffer = new StringBuilder();
        if (host != null) {
            if (scheme != null) {
                buffer.append(scheme);
                buffer.append("://");
            }
            buffer.append(host);
            if (port > 0) {
                buffer.append(':');
                buffer.append(port);
            }
        }
        if (path == null || !path.startsWith("/")) {
            buffer.append('/');
        }
        if (path != null) {
            buffer.append(path);
        }
        if (query != null) {
            buffer.append('?');
            buffer.append(query);
        }
        if (fragment != null) {
            buffer.append('#');
            buffer.append(fragment);
        }
        return new URI(buffer.toString());
    }

    public static URI rewriteURI(URI uri, HttpHost target, boolean dropFragment) throws URISyntaxException {
        if (uri == null) {
            throw new IllegalArgumentException("URI may nor be null");
        } else if (target != null) {
            return createURI(target.getSchemeName(), target.getHostName(), target.getPort(), uri.getRawPath(), uri.getRawQuery(), dropFragment ? null : uri.getRawFragment());
        } else {
            return createURI(null, null, -1, uri.getRawPath(), uri.getRawQuery(), dropFragment ? null : uri.getRawFragment());
        }
    }

    public static URI rewriteURI(URI uri, HttpHost target) throws URISyntaxException {
        return rewriteURI(uri, target, false);
    }

    public static URI resolve(URI baseURI, String reference) {
        return resolve(baseURI, URI.create(reference));
    }

    public static URI resolve(URI baseURI, URI reference) {
        boolean emptyReference;
        if (baseURI == null) {
            throw new IllegalArgumentException("Base URI may nor be null");
        } else if (reference == null) {
            throw new IllegalArgumentException("Reference URI may nor be null");
        } else {
            String s = reference.toString();
            if (s.startsWith("?")) {
                return resolveReferenceStartingWithQueryString(baseURI, reference);
            }
            if (s.length() == 0) {
                emptyReference = true;
            } else {
                emptyReference = false;
            }
            if (emptyReference) {
                reference = URI.create("#");
            }
            URI resolved = baseURI.resolve(reference);
            if (emptyReference) {
                String resolvedString = resolved.toString();
                resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf(35)));
            }
            return removeDotSegments(resolved);
        }
    }

    private static URI resolveReferenceStartingWithQueryString(URI baseURI, URI reference) {
        String baseUri = baseURI.toString();
        if (baseUri.indexOf(63) > -1) {
            baseUri = baseUri.substring(0, baseUri.indexOf(63));
        }
        return URI.create(baseUri + reference.toString());
    }

    private static URI removeDotSegments(URI uri) {
        String path = uri.getPath();
        if (path == null || path.indexOf("/.") == -1) {
            return uri;
        }
        String[] inputSegments = path.split("/");
        Stack<String> outputSegments = new Stack<>();
        for (int i = 0; i < inputSegments.length; i++) {
            if (inputSegments[i].length() != 0 && !".".equals(inputSegments[i])) {
                if (!"..".equals(inputSegments[i])) {
                    outputSegments.push(inputSegments[i]);
                } else if (!outputSegments.isEmpty()) {
                    outputSegments.pop();
                }
            }
        }
        StringBuilder outputBuffer = new StringBuilder();
        Iterator i$ = outputSegments.iterator();
        while (i$.hasNext()) {
            outputBuffer.append('/').append((String) i$.next());
        }
        try {
            return new URI(uri.getScheme(), uri.getAuthority(), outputBuffer.toString(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private URIUtils() {
    }
}
