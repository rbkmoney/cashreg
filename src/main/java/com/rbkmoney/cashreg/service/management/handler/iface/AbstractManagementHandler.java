package com.rbkmoney.cashreg.service.management.handler.iface;

import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;


public abstract class AbstractManagementHandler implements ManagementHandler{

    private final Filter filter;

    public AbstractManagementHandler(String path) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, new IsNullCondition().not()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean filter(Change change) {
        return filter.match(change);
    }

}
