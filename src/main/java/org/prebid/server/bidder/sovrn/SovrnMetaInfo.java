package org.prebid.server.bidder.sovrn;

import org.prebid.server.bidder.MetaInfo;
import org.prebid.server.proto.response.BidderInfo;

import java.util.Collections;

public class SovrnMetaInfo implements MetaInfo {

    private BidderInfo bidderInfo;

    public SovrnMetaInfo(boolean enabled) {
        bidderInfo = BidderInfo.create(enabled, "sovrnoss@sovrn.com",
                Collections.singletonList("banner"), Collections.singletonList("banner"), null, 13, false);
    }

    @Override
    public BidderInfo info() {
        return bidderInfo;
    }
}
