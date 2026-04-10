package com.cjx.decision.projection.frorcl;

import java.math.BigDecimal;

public interface CollectionPlan {
    BigDecimal getPlanValue();
    String getCompanyName();
    String yesterday();


}
