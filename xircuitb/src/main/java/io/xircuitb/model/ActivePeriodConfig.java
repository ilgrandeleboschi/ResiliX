package io.xircuitb.model;

import io.resilix.model.ActivePeriod;
import lombok.Getter;
import lombok.Setter;

import static io.resilix.model.ActivePeriod.buildActivePeriod;
import static io.resilix.validator.ResiliXValidator.validateAndConvertDays;

@Getter
@Setter
public class ActivePeriodConfig {

    private String from;
    private String to;
    private String[] days;

    public ActivePeriod toActivePeriod() {
        return buildActivePeriod(from, to, validateAndConvertDays(days));
    }

}
