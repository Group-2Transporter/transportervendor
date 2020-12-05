package com.e.transportervendor.bean;

public class States {
    String stateName,stateId;

    private boolean check = false;
    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStateId() {
        return stateId;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }
}
