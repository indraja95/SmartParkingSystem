package org.shaded.apache.http.conn.routing;

import java.net.InetAddress;
import org.shaded.apache.http.HttpHost;
import org.shaded.apache.http.annotation.Immutable;
import org.shaded.apache.http.conn.routing.RouteInfo.LayerType;
import org.shaded.apache.http.conn.routing.RouteInfo.TunnelType;

@Immutable
public final class HttpRoute implements RouteInfo, Cloneable {
    private static final HttpHost[] EMPTY_HTTP_HOST_ARRAY = new HttpHost[0];
    private final LayerType layered;
    private final InetAddress localAddress;
    private final HttpHost[] proxyChain;
    private final boolean secure;
    private final HttpHost targetHost;
    private final TunnelType tunnelled;

    private HttpRoute(InetAddress local, HttpHost target, HttpHost[] proxies, boolean secure2, TunnelType tunnelled2, LayerType layered2) {
        if (target == null) {
            throw new IllegalArgumentException("Target host may not be null.");
        } else if (proxies == null) {
            throw new IllegalArgumentException("Proxies may not be null.");
        } else if (tunnelled2 == TunnelType.TUNNELLED && proxies.length == 0) {
            throw new IllegalArgumentException("Proxy required if tunnelled.");
        } else {
            if (tunnelled2 == null) {
                tunnelled2 = TunnelType.PLAIN;
            }
            if (layered2 == null) {
                layered2 = LayerType.PLAIN;
            }
            this.targetHost = target;
            this.localAddress = local;
            this.proxyChain = proxies;
            this.secure = secure2;
            this.tunnelled = tunnelled2;
            this.layered = layered2;
        }
    }

    public HttpRoute(HttpHost target, InetAddress local, HttpHost[] proxies, boolean secure2, TunnelType tunnelled2, LayerType layered2) {
        this(local, target, toChain(proxies), secure2, tunnelled2, layered2);
    }

    public HttpRoute(HttpHost target, InetAddress local, HttpHost proxy, boolean secure2, TunnelType tunnelled2, LayerType layered2) {
        this(local, target, toChain(proxy), secure2, tunnelled2, layered2);
    }

    public HttpRoute(HttpHost target, InetAddress local, boolean secure2) {
        this(local, target, EMPTY_HTTP_HOST_ARRAY, secure2, TunnelType.PLAIN, LayerType.PLAIN);
    }

    public HttpRoute(HttpHost target) {
        this((InetAddress) null, target, EMPTY_HTTP_HOST_ARRAY, false, TunnelType.PLAIN, LayerType.PLAIN);
    }

    public HttpRoute(HttpHost target, InetAddress local, HttpHost proxy, boolean secure2) {
        this(local, target, toChain(proxy), secure2, secure2 ? TunnelType.TUNNELLED : TunnelType.PLAIN, secure2 ? LayerType.LAYERED : LayerType.PLAIN);
        if (proxy == null) {
            throw new IllegalArgumentException("Proxy host may not be null.");
        }
    }

    private static HttpHost[] toChain(HttpHost proxy) {
        if (proxy == null) {
            return EMPTY_HTTP_HOST_ARRAY;
        }
        return new HttpHost[]{proxy};
    }

    private static HttpHost[] toChain(HttpHost[] proxies) {
        if (proxies == null || proxies.length < 1) {
            return EMPTY_HTTP_HOST_ARRAY;
        }
        for (HttpHost proxy : proxies) {
            if (proxy == null) {
                throw new IllegalArgumentException("Proxy chain may not contain null elements.");
            }
        }
        HttpHost[] result = new HttpHost[proxies.length];
        System.arraycopy(proxies, 0, result, 0, proxies.length);
        return result;
    }

    public final HttpHost getTargetHost() {
        return this.targetHost;
    }

    public final InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public final int getHopCount() {
        return this.proxyChain.length + 1;
    }

    public final HttpHost getHopTarget(int hop) {
        if (hop < 0) {
            throw new IllegalArgumentException("Hop index must not be negative: " + hop);
        }
        int hopcount = getHopCount();
        if (hop >= hopcount) {
            throw new IllegalArgumentException("Hop index " + hop + " exceeds route length " + hopcount);
        } else if (hop < hopcount - 1) {
            return this.proxyChain[hop];
        } else {
            return this.targetHost;
        }
    }

    public final HttpHost getProxyHost() {
        if (this.proxyChain.length == 0) {
            return null;
        }
        return this.proxyChain[0];
    }

    public final TunnelType getTunnelType() {
        return this.tunnelled;
    }

    public final boolean isTunnelled() {
        return this.tunnelled == TunnelType.TUNNELLED;
    }

    public final LayerType getLayerType() {
        return this.layered;
    }

    public final boolean isLayered() {
        return this.layered == LayerType.LAYERED;
    }

    public final boolean isSecure() {
        return this.secure;
    }

    public final boolean equals(Object o) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof HttpRoute)) {
            return false;
        }
        HttpRoute that = (HttpRoute) o;
        boolean equal = this.targetHost.equals(that.targetHost);
        if (this.localAddress == that.localAddress || (this.localAddress != null && this.localAddress.equals(that.localAddress))) {
            z = true;
        } else {
            z = false;
        }
        boolean equal2 = equal & z;
        if (this.proxyChain == that.proxyChain || this.proxyChain.length == that.proxyChain.length) {
            z2 = true;
        } else {
            z2 = false;
        }
        boolean equal3 = equal2 & z2;
        if (!(this.secure == that.secure && this.tunnelled == that.tunnelled && this.layered == that.layered)) {
            z3 = false;
        }
        boolean equal4 = equal3 & z3;
        if (equal4 && this.proxyChain != null) {
            int i = 0;
            while (equal4 && i < this.proxyChain.length) {
                equal4 = this.proxyChain[i].equals(that.proxyChain[i]);
                i++;
            }
        }
        return equal4;
    }

    public final int hashCode() {
        int hc = this.targetHost.hashCode();
        if (this.localAddress != null) {
            hc ^= this.localAddress.hashCode();
        }
        int hc2 = hc ^ this.proxyChain.length;
        for (HttpHost aProxyChain : this.proxyChain) {
            hc2 ^= aProxyChain.hashCode();
        }
        if (this.secure) {
            hc2 ^= 286331153;
        }
        return (hc2 ^ this.tunnelled.hashCode()) ^ this.layered.hashCode();
    }

    public final String toString() {
        StringBuilder cab = new StringBuilder((getHopCount() * 30) + 50);
        cab.append("HttpRoute[");
        if (this.localAddress != null) {
            cab.append(this.localAddress);
            cab.append("->");
        }
        cab.append('{');
        if (this.tunnelled == TunnelType.TUNNELLED) {
            cab.append('t');
        }
        if (this.layered == LayerType.LAYERED) {
            cab.append('l');
        }
        if (this.secure) {
            cab.append('s');
        }
        cab.append("}->");
        for (HttpHost aProxyChain : this.proxyChain) {
            cab.append(aProxyChain);
            cab.append("->");
        }
        cab.append(this.targetHost);
        cab.append(']');
        return cab.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
