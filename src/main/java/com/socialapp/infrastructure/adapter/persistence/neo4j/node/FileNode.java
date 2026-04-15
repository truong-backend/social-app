package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("File")
public class FileNode {

    @Id
    private String path;

    @Property("name")
    private String name;

    @Property("contentType")
    private String contentType;

    @Property("sizeBytes")
    private long sizeBytes;

    // ===== Constructors =====

    public FileNode() {
    }

    public FileNode(String path, String name, String contentType, long sizeBytes) {
        this.path = path;
        this.name = name;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    // ===== Getters / Setters =====

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}