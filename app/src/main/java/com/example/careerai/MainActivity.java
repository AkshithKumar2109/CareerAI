
package com.example.careerai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> resumePickerLauncher;

    private String selectedCareerRole = "Java Developer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnAnalyzeResume = findViewById(R.id.btnAnalyzeResume);
        Button btnCareerGuidance = findViewById(R.id.btnCareerGuidance);
        Button btnHistory = findViewById(R.id.btnHistory);

        Spinner spinnerCareerRole = findViewById(R.id.spinnerCareerRole);

        resumePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {

                        Uri resumeUri = result.getData().getData();

                        if (resumeUri != null) {
                            analyzeResume(resumeUri);
                        }
                    }
                }
        );

        btnAnalyzeResume.setOnClickListener(view -> {
            selectedCareerRole =
                    spinnerCareerRole.getSelectedItem().toString();

            openResumePicker();
        });

        btnCareerGuidance.setOnClickListener(view -> {
            selectedCareerRole =
                    spinnerCareerRole.getSelectedItem().toString();

            showCareerGuidance();
        });

        btnHistory.setOnClickListener(view -> showHistory());
    }

    private void openResumePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        resumePickerLauncher.launch(intent);
    }

    private void analyzeResume(Uri resumeUri) {
        try {
            ResumeAnalyzer.AnalysisResult result =
                    ResumeAnalyzer.analyze(
                            this,
                            resumeUri,
                            selectedCareerRole
                    );

            saveAnalysis(
                    selectedCareerRole,
                    result.jobMatchScore,
                    result.detectedSkills
            );

            String message =
                    "Target Career: "
                            + selectedCareerRole
                            + "\n\nSkills Found:\n"
                            + formatList(result.detectedSkills)
                            + "\nJob Match Score: "
                            + result.jobMatchScore
                            + "%"
                            + "\n\nMissing Skills:\n"
                            + formatList(result.missingSkills);

            new AlertDialog.Builder(this)
                    .setTitle("Resume Analysis Results")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Analysis failed: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String formatList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "None detected";
        }

        StringBuilder result = new StringBuilder();

        for (String item : items) {
            result.append("\u2022 ")
                    .append(item)
                    .append("\n");
        }

        return result.toString();
    }

    private void saveAnalysis(
            String careerRole,
            int score,
            List<String> skills) {

        SharedPreferences preferences =
                getSharedPreferences(
                        "CareerAIHistory",
                        MODE_PRIVATE
                );

        String oldHistory =
                preferences.getString("history", "");

        String newEntry =
                "Career: " + careerRole
                        + "\nMatch Score: " + score + "%"
                        + "\nSkills: " + skills;

        String updatedHistory = newEntry;

        if (!oldHistory.isEmpty()) {
            updatedHistory += "\n\n--------------------\n\n"
                    + oldHistory;
        }

        preferences.edit()
                .putString("history", updatedHistory)
                .apply();
    }

    private void showHistory() {
        SharedPreferences preferences =
                getSharedPreferences(
                        "CareerAIHistory",
                        MODE_PRIVATE
                );

        String history = preferences.getString("history", "");

        if (history.isEmpty()) {
            history =
                    "No previous resume analyses found.\n\n"
                            + "Analyze a resume to see your history here.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Analysis History")
                .setMessage(history)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showCareerGuidance() {
        String guidance;

        switch (selectedCareerRole) {

            case "Python Developer":
                guidance =
                        "PYTHON DEVELOPER ROADMAP\n\n"
                                + "1. Learn Python fundamentals.\n\n"
                                + "2. Practice functions, classes and OOP.\n\n"
                                + "3. Learn data structures and algorithms.\n\n"
                                + "4. Learn SQL and databases.\n\n"
                                + "5. Understand REST APIs.\n\n"
                                + "6. Explore Django or Flask.\n\n"
                                + "7. Learn Git and GitHub.\n\n"
                                + "8. Build Python projects.\n\n"
                                + "9. Practice coding interviews.";
                break;

            case "Data Analyst":
                guidance =
                        "DATA ANALYST ROADMAP\n\n"
                                + "1. Learn Excel.\n\n"
                                + "2. Learn SQL and database queries.\n\n"
                                + "3. Study basic statistics.\n\n"
                                + "4. Learn Python fundamentals.\n\n"
                                + "5. Practice Pandas and NumPy.\n\n"
                                + "6. Learn data visualization.\n\n"
                                + "7. Explore Power BI or Tableau.\n\n"
                                + "8. Build data analysis projects.\n\n"
                                + "9. Practice interview questions.";
                break;

            default:
                guidance =
                        "JAVA DEVELOPER ROADMAP\n\n"
                                + "1. Learn Core Java.\n\n"
                                + "2. Understand object-oriented programming.\n\n"
                                + "3. Practice data structures and algorithms.\n\n"
                                + "4. Learn SQL and MySQL.\n\n"
                                + "5. Learn JDBC and Spring Boot.\n\n"
                                + "6. Understand REST APIs.\n\n"
                                + "7. Learn Git and GitHub.\n\n"
                                + "8. Build Java projects.\n\n"
                                + "9. Practice coding and technical interviews.";
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(selectedCareerRole + " Guidance")
                .setMessage(guidance)
                .setPositiveButton("Close", null)
                .show();
    }
}
