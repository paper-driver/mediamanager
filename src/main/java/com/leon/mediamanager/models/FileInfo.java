package com.leon.mediamanager.models;

public class FileInfo {
    private String name;
    private String url;
    private String path;

    public FileInfo() {

    }

    public FileInfo(String name, String url, String path) {
        this.name = name;
        this.url = url;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

