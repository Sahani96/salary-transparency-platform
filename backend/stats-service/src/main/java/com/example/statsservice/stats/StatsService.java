package com.example.statsservice.stats;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final SalarySubmissionRepository repository;

    public StatsService(SalarySubmissionRepository repository) {
        this.repository = repository;
    }

    public StatsDtos.SummaryResponse summary() {
        List<SalarySubmission> submissions = approvedSubmissions();
        List<BigDecimal> salaries = sortedSalaries(submissions);
        return new StatsDtos.SummaryResponse(
                submissions.size(),
                average(salaries),
                median(salaries),
                salaries.isEmpty() ? BigDecimal.ZERO : salaries.get(0),
                salaries.isEmpty() ? BigDecimal.ZERO : salaries.get(salaries.size() - 1),
                percentile(salaries, 0.90)
        );
    }

    public StatsDtos.GroupStatsResponse byRole() {
        return grouped("jobTitle", approvedSubmissions(), SalarySubmission::getJobTitle);
    }

    public StatsDtos.GroupStatsResponse byCompany() {
        return grouped("company", approvedSubmissions(), SalarySubmission::getCompany);
    }

    public StatsDtos.GroupStatsResponse byCountry() {
        return grouped("country", approvedSubmissions(), SalarySubmission::getCountry);
    }

    public StatsDtos.GroupStatsResponse byLevel() {
        return grouped("experienceLevel", approvedSubmissions(), submission -> submission.getExperienceLevel().name());
    }

    public StatsDtos.GroupStatsResponse compare(String groupBy, List<String> values) {
        Function<SalarySubmission, String> extractor = switch (groupBy) {
            case "jobTitle" -> SalarySubmission::getJobTitle;
            case "company" -> SalarySubmission::getCompany;
            case "country" -> SalarySubmission::getCountry;
            case "experienceLevel" -> submission -> submission.getExperienceLevel().name();
            default -> throw new IllegalArgumentException("Unsupported groupBy value");
        };

        List<String> normalizedValues = values.stream().map(String::trim).toList();
        List<SalarySubmission> filtered = approvedSubmissions().stream()
                .filter(submission -> normalizedValues.contains(extractor.apply(submission)))
                .toList();
        return grouped(groupBy, filtered, extractor);
    }

    private StatsDtos.GroupStatsResponse grouped(
            String groupBy,
            List<SalarySubmission> submissions,
            Function<SalarySubmission, String> groupingFunction
    ) {
        Map<String, List<SalarySubmission>> groups = submissions.stream()
                .collect(Collectors.groupingBy(groupingFunction));

        List<StatsDtos.GroupStat> items = groups.entrySet().stream()
                .map(entry -> {
                    List<BigDecimal> salaries = sortedSalaries(entry.getValue());
                    return new StatsDtos.GroupStat(
                            entry.getKey(),
                            entry.getValue().size(),
                            average(salaries),
                            median(salaries),
                            salaries.isEmpty() ? BigDecimal.ZERO : salaries.get(0),
                            salaries.isEmpty() ? BigDecimal.ZERO : salaries.get(salaries.size() - 1)
                    );
                })
                .sorted(Comparator.comparing(StatsDtos.GroupStat::group))
                .toList();

        return new StatsDtos.GroupStatsResponse(groupBy, items);
    }

    private List<SalarySubmission> approvedSubmissions() {
        return repository.findByStatus(SubmissionStatus.APPROVED);
    }

    private List<BigDecimal> sortedSalaries(List<SalarySubmission> submissions) {
        return submissions.stream()
                .map(SalarySubmission::getBaseSalary)
                .sorted()
                .toList();
    }

    private BigDecimal average(List<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = salaries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(salaries.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal median(List<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int middle = salaries.size() / 2;
        if (salaries.size() % 2 == 1) {
            return salaries.get(middle);
        }
        return salaries.get(middle - 1)
                .add(salaries.get(middle))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentile(List<BigDecimal> salaries, double percentile) {
        if (salaries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int index = (int) Math.ceil(percentile * salaries.size()) - 1;
        index = Math.max(index, 0);
        return salaries.get(index);
    }
}
