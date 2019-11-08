package com.rbkmoney.cashreg.service.mg.aggregate.mapper.iface;

import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;

public abstract class AbstractChangeMapper implements ChangeMapper {

    private final Filter filter;

    public AbstractChangeMapper(String path) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, new IsNullCondition().not()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean filter(Change change) {
        return filter.match(change);
    }

}
