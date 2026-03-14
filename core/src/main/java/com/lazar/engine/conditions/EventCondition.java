package com.lazar.engine.conditions;

public class EventCondition {
    public ConditionType type;

    // pentru STAT
    public StatKey stat;
    public ComparisonType op;
    public Integer value;

    // pentru FLAG
    public String flag;
    public Boolean expected;

    // pentru LAST_DECISION
    public String eventId;
    public String option;

    // pentru ARC_STAGE
    public String arcId;
    public String arcStage;

    public EventCondition() {
    }
}
