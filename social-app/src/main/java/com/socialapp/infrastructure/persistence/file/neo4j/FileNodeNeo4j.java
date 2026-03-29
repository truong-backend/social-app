package com.socialapp.infrastructure.persistence.file.neo4j;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("FileNode")
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