package com.socialapp.infrastructure.adapter.persistence.neo4j.node;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Keyword")
public class KeywordNode {

    @Id
    private String text;

    public KeywordNode() {}

    public KeywordNode(String text) { this.text = text; }

    public String getText()           { return text; }
    public void   setText(String v)   { this.text = v; }
}