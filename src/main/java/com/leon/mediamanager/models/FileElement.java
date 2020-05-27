package com.leon.mediamanager.models;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class FileElement {

    @Getter
    private UUID uuid = UUID.randomUUID();

    @Getter
    @Setter
    private int level;

    @Getter
    @Setter
    private Boolean isFolder;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private UUID parent;

    @Getter
    @Setter
    private String url = null;

    public FileElement() {}

    public FileElement(Boolean isFolder, String name, UUID parent, int level) {
        this.isFolder = isFolder;
        this.name = name;
        this.parent = parent;
        this.level = level;
    }

}
