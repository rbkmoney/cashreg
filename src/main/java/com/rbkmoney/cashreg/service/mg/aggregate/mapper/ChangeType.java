package com.rbkmoney.cashreg.service.mg.aggregate.mapper;

import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;

public enum ChangeType {

    CREATED("created"),
    STATUS_CHANGED("status_changed"),
    SESSION_PAYLOAD_STARTED("session.payload.started"),
    SESSION_PAYLOAD_FINISHED("session.payload.finished"),
    SESSION_ADAPTER_STATE_CHANGED("session.payload.session_adapter_state_changed"),
    SESSION_PAYLOAD_FINISHED_RESULT_FAILED("session.payload.finished.result.failed"),
    SESSION_PAYLOAD_FINISHED_RESULT_SUCCEEDED("session.payload.finished.result.succeeded"),
    STATUS_CHANGED_STATUS_FAILED("status_changed.status.failed"),
    STATUS_CHANGED_STATUS_DELIVERED("status_changed.status.delivered"),
    STATUS_CHANGED_STATUS_PENDING("status_changed.status.pending");

    Filter filter;

    ChangeType(String path) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, new IsNullCondition().not()));
    }

    public Filter getFilter() {
        return filter;
    }

}
