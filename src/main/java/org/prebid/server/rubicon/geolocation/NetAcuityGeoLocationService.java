package org.prebid.server.rubicon.geolocation;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.digitalenvoy.netacuity.api.DbAccessor;
import net.digitalenvoy.netacuity.api.DbAccessorFactory;
import net.digitalenvoy.netacuity.api.PulseQuery;
import org.prebid.server.exception.PreBidException;
import org.prebid.server.execution.Timeout;
import org.prebid.server.geolocation.GeoLocationService;
import org.prebid.server.geolocation.model.GeoInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class NetAcuityGeoLocationService implements GeoLocationService {

    private static final Logger logger = LoggerFactory.getLogger(NetAcuityGeoLocationService.class);

    private static final int API_ID = 1;
    private static final String VENDOR = "netacuity";

    private final Vertx vertx;
    private final Supplier<InetAddress> serverAddress;

    public NetAcuityGeoLocationService(Vertx vertx, Supplier<InetAddress> serverAddress) {
        this.vertx = Objects.requireNonNull(vertx);
        this.serverAddress = Objects.requireNonNull(serverAddress);
    }

    @Override
    public Future<GeoInfo> lookup(String ip, Timeout timeout) {
        final InetAddress server;
        try {
            server = serverAddress.get();
        } catch (PreBidException e) {
            return Future.failedFuture(e);
        }
        return lookup(ip, timeout, server);
    }

    /**
     * A work-around overloaded method for {@link CircuitBreakerSecuredNetAcuityGeoLocationService}
     * The idea is to use the same serverAddress variable while getting these addresses randomly.
     */
    public Future<GeoInfo> lookup(String ip, Timeout timeout, InetAddress serverAddress) {
        final Promise<GeoInfo> promise = Promise.promise();
        vertx.executeBlocking(executeFuture -> doLookup(executeFuture, serverAddress, ip, timeout), false, promise);
        return promise.future();
    }

    private void doLookup(Promise<GeoInfo> promise, InetAddress serverAddress, String ip, Timeout timeout) {
        final long remainingTimeout = timeout.remaining();
        if (remainingTimeout <= 0) {
            failWith(new TimeoutException("Timeout has been exceeded")).setHandler(promise);
            return;
        }

        final InetAddress lookupAddress;
        try {
            lookupAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            failWith(new PreBidException(String.format("Invalid IP address to lookup: %s", ip), e))
                    .setHandler(promise);
            return;
        }

        final DbAccessor dbAccessor = DbAccessorFactory.getAccessor(serverAddress, API_ID,
                Math.toIntExact(remainingTimeout));
        try {
            final PulseQuery query = dbAccessor.query(PulseQuery.class, lookupAddress);
            promise.complete(GeoInfo.builder().vendor(VENDOR).country(query.getTwoLetterCountry()).build());
        } catch (IllegalArgumentException | IOException e) {
            failWith(new PreBidException("Geo location lookup failed", e)).setHandler(promise);
        }
    }

    private static Future<GeoInfo> failWith(Throwable exception) {
        logger.warn("NetAcuity geo location service error", exception);
        return Future.failedFuture(exception);
    }
}