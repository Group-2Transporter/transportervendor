package com.e.transportervendor.bean;

public class State {
    private boolean check = false;
    private String stateList ;

    public State(String stateList) {
        this.stateList = stateList;
    }

    public String getStateList() {
        return stateList;
    }

    public void setStateList(String stateList) {
        this.stateList = stateList;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
