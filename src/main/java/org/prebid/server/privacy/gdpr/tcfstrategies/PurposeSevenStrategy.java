package org.prebid.server.privacy.gdpr.tcfstrategies;

import org.prebid.server.privacy.gdpr.model.PrivacyEnforcementAction;
import org.prebid.server.privacy.gdpr.tcfstrategies.typestrategies.BasicEnforcePurposeStrategy;
import org.prebid.server.privacy.gdpr.tcfstrategies.typestrategies.FullEnforcePurposeStrategy;
import org.prebid.server.privacy.gdpr.tcfstrategies.typestrategies.NoEnforcePurposeStrategy;

public class PurposeSevenStrategy extends PurposeStrategy {

    private static final int PURPOSE_ID = 7;

    public PurposeSevenStrategy(FullEnforcePurposeStrategy fullEnforcePurposeStrategy,
                                BasicEnforcePurposeStrategy basicEnforcePurposeStrategy,
                                NoEnforcePurposeStrategy noEnforcePurposeStrategy) {
        super(fullEnforcePurposeStrategy, basicEnforcePurposeStrategy, noEnforcePurposeStrategy);
    }

    @Override
    public void allow(PrivacyEnforcementAction privacyEnforcementAction) {
        privacyEnforcementAction.setBlockAnalyticsReport(false);
    }

    @Override
    public int getPurposeId() {
        return PURPOSE_ID;
    }

}
