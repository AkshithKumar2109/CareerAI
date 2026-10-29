package com.example.careerai;

import android.content.Context;
import android.net.Uri;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ResumeAnalyzer {

    public static class AnalysisResult {
        public String extractedText;
        public List<String> detectedSkills;
        public List<String> missingSkills;
        public int jobMatchScore;

        public AnalysisResult(
                String extractedText,
                List<String> detectedSkills,
                List<String> missingSkills,
                int jobMatchScore) {
            this.extractedText = extractedText;
            this.detectedSkills = detectedSkills;
            this.missingSkills = missingSkills;
            this.jobMatchScore = jobMatchScore;
        }
    }

    public static AnalysisResult analyze(
            Context context,
            Uri resumeUri,
            String targetRole) throws Exception {

        PDFBoxResourceLoader.init(context);

        StringBuilder textBuilder = new StringBuilder();

        InputStream inputStream =
                context.getContentResolver().openInputStream(resumeUri);

        if (inputStream == null) {
            throw new Exception("Unable to open the selected PDF.");
        }

        try (InputStream input = inputStream;
             PDDocument document = PDDocument.load(input)) {

            PDFTextStripper stripper = new PDFTextStripper();
            textBuilder.append(stripper.getText(document));
        }

        String resumeText = textBuilder.toString();
        String normalizedText = resumeText.toLowerCase(Locale.ROOT);

        List<String> detectedSkills = new ArrayList<>();

        List<String> allSkills = Arrays.asList(
                "Java", "Python", "C++", "SQL",
                "MySQL", "HTML", "CSS", "JavaScript",
                "Spring Boot", "REST APIs", "Git",
                "Data Structures", "Machine Learning"
        );

        for (String skill : allSkills) {
            if (normalizedText.contains(skill.toLowerCase(Locale.ROOT))) {
                detectedSkills.add(skill);
            }
        }

        List<String> requiredSkills;

        switch (targetRole.toLowerCase(Locale.ROOT)) {
            case "java developer":
                requiredSkills = Arrays.asList(
                        "Java", "OOP", "SQL",
                        "Spring Boot", "REST APIs", "Git"
                );
                break;

            case "python developer":
                requiredSkills = Arrays.asList(
                        "Python", "SQL", "Git",
                        "REST APIs", "Data Structures"
                );
                break;

            case "data analyst":
                requiredSkills = Arrays.asList(
                        "Python", "SQL", "Excel",
                        "Statistics", "Data Visualization"
                );
                break;

            default:
                requiredSkills = Arrays.asList(
                        "Java", "SQL", "Git"
                );
        }

        List<String> missingSkills = new ArrayList<>();
        int matchedCount = 0;

        for (String skill : requiredSkills) {
            if (normalizedText.contains(skill.toLowerCase(Locale.ROOT))) {
                matchedCount++;
            } else {
                missingSkills.add(skill);
            }
        }

        int score = Math.round(
                matchedCount * 100.0f / requiredSkills.size()
        );

        return new AnalysisResult(
                resumeText,
                detectedSkills,
                missingSkills,
                score
        );
    }
}