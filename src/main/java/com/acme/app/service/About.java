package com.acme.app.service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class About {

    @JsonProperty("version")
    private String applicationVersion;

    public String getApplicationVersion() {
        return this.applicationVersion;
    }

    public void setApplicationVersion(String pApplicationVersion) {
        this.applicationVersion = pApplicationVersion;
    }

}
