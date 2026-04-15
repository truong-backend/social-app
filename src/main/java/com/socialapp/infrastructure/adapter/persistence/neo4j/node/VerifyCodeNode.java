package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;

@Node("VerifyCode")
public class VerifyCodeNode {

    @Id
    private String code;

    @Property("isVerified")
    private boolean isVerified;

    @Property("expiryTime")
    private LocalDateTime expiryTime;

    public VerifyCodeNode() {}

    public VerifyCodeNode(String code, boolean isVerified, LocalDateTime expiryTime) {
        this.code       = code;
        this.isVerified = isVerified;
        this.expiryTime = expiryTime;
    }

    public String        getCode()       { return code; }
    public void          setCode(String code) { this.code = code; }

    public boolean       isVerified()    { return isVerified; }
    public void          setVerified(boolean v) { this.isVerified = v; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void          setExpiryTime(LocalDateTime v) { this.expiryTime = v; }
}