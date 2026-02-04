package reports;

import java.time.LocalDate;

public class Report {

    private String reportId;
    private String title;
    private LocalDate generatedDate;
    private String content;

    public Report(String reportId, String title, String content) {
        this.reportId = reportId;
        this.title = title;
        this.content = content;
        this.generatedDate = LocalDate.now();
    }

    public String getReportId() {
        return reportId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public String getContent() {
        return content;
    }
}