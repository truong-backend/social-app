package com.socialapp.infrastructure.persistence.file.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("File")   // FIX: phải là "File" để khớp với tất cả Cypher queries ATTACH_FILE
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileNodeNeo4j {

    @Id
    private String path;
    private String name;
    private String contentType;
}