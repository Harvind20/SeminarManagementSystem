package reports;

public class ReportGenerator {

    public Report generateSampleReport() {

        String reportId = "RPT-001";
        String title = "Seminar Evaluation Summary";

        String content =
                "Total Presenters: 10\n" +
                "Total Evaluators: 5\n" +
                "Average Score: 82%\n\n" +
                "This report summarizes the overall seminar evaluation results.";

        return new Report(reportId, title, content);
    }
}