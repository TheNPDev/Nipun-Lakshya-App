package com.samagra.workflowengine.workflow.model;


import java.util.List;

public class WorkflowConfig {

    private List<FlowConfig> flowConfigs;
    private List<Action> actions;
    private List<State> states;

    public List<FlowConfig> getFlowConfigs() {
        return flowConfigs;
    }

    public void setFlowConfigs(List<FlowConfig> flowConfigs) {
        this.flowConfigs = flowConfigs;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }
}
