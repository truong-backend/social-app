package com.socialapp.infrastructure.config.neo4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jAuditing
@EnableTransactionManagement
@EnableNeo4jRepositories(
        basePackages = "com.socialapp.infrastructure.adapter.persistence.neo4j.repository"
)
public class Neo4jConfig {
    // Driver, URI, credentials đặt trong application.yml:
    // spring.neo4j.uri=bolt://localhost:7687
    // spring.neo4j.authentication.username=neo4j
    // spring.neo4j.authentication.password=secret
}