package com.holybell.rpcfx.registry;

import java.util.UUID;

public class ServiceInfo {

    private String uniqueId;
    private String serviceName;
    private String host;
    private int port;
    private String group;
    private String version;

    public ServiceInfo() {

    }

    public ServiceInfo(String serviceName, String host, int port, String group, String version) {
        this.uniqueId = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.group = group;
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
