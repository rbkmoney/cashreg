package com.rbkmoney.cashreg.service.management;

import com.rbkmoney.cashreg.domain.SourceData;
import com.rbkmoney.damsel.cashreg_processing.Change;

import java.util.List;

public interface ManagementService {

    SourceData signalInit();

    SourceData signalTimeout(List<Change> changes);
}
