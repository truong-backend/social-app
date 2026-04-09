package com.socialapp.infrastructure.config.bean;

import com.socialapp.application.report.usecase.*;
import com.socialapp.domain.account.repository.AccountRepository;
import com.socialapp.domain.report.repository.ReportRepository;
import com.socialapp.infrastructure.persistence.report.neo4j.ReportRepositoryAdapter;
import com.socialapp.infrastructure.persistence.report.neo4j.repository.ReportNeo4jRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportConfig {

    @Bean
    public ReportRepository reportRepository(ReportNeo4jRepository neo4jRepository) {
        return new ReportRepositoryAdapter(neo4jRepository);
    }

    @Bean
    public CreateReportUseCase createReportUseCase(ReportRepository reportRepository) {
        return new CreateReportUseCase(reportRepository);
    }

    @Bean
    public GetReportsUseCase getReportsUseCase(ReportRepository reportRepository) {
        return new GetReportsUseCase(reportRepository);
    }

    @Bean
    public ResolveReportUseCase resolveReportUseCase(ReportRepository reportRepository) {
        return new ResolveReportUseCase(reportRepository);
    }

    @Bean
    public BanUserUseCase banUserUseCase(AccountRepository accountRepository) {
        return new BanUserUseCase(accountRepository);
    }

    @Bean
    public UnbanUserUseCase unbanUserUseCase(AccountRepository accountRepository) {
        return new UnbanUserUseCase(accountRepository);
    }
}