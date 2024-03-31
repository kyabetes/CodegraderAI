import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class CodeComparisonApp extends JFrame implements ActionListener {
    private JTextArea correctCodeArea;
    private JTextArea submittedCodeArea;
    private JTextArea feedbackArea;
    private JButton compareButton;
    private JButton saveButton;

    public CodeComparisonApp() {
        setTitle("Code Comparison App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create text areas for code input
        correctCodeArea = new JTextArea(10, 30);
        submittedCodeArea = new JTextArea(10, 30);
        feedbackArea = new JTextArea(10, 30);
        feedbackArea.setEditable(false);

        // Create buttons
        compareButton = new JButton("Compare");
        compareButton.addActionListener(this);
        saveButton = new JButton("Save Feedback");
        saveButton.addActionListener(this);

        // Create panels for layout
        JPanel codePanel = new JPanel(new GridLayout(1, 2));
        codePanel.add(new JScrollPane(correctCodeArea));
        codePanel.add(new JScrollPane(submittedCodeArea));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(compareButton);
        buttonPanel.add(saveButton);

        // Add components to the frame
        add(codePanel, BorderLayout.NORTH);
        add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == compareButton) {
            String correctCode = correctCodeArea.getText();
            String submittedCode = submittedCodeArea.getText();

            System.out.println("Correct Code: " + correctCode); // Debugging statement
            System.out.println("Submitted Code: " + submittedCode); // Debugging statement

            String feedback = compareCode(correctCode, submittedCode);
            System.out.println("Generated Feedback: " + feedback); // Debugging statement

            feedbackArea.setText(feedback);
            System.out.println("Feedback Area Text: " + feedbackArea.getText()); // Debugging statement
        } else if (e.getSource() == saveButton) {
            String feedback = feedbackArea.getText();
            saveFeedbackToFile(feedback);
        }
    }

    private String compareCode(String correctCode, String submittedCode) {
        String apiKey = "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";
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        String prompt = "Compare the following code files and provide suggestions for improvement:\n\n";
        prompt += "Correct Code:\n" + correctCode + "\n\n";
        prompt += "Submitted Code:\n" + submittedCode + "\n\n";
        prompt += "Provide the suggestions in a clear and concise manner.";

        String requestBody = "{\"model\":\"gpt-3.5-turbo\"," +
                "\"messages\":[{\"role\":\"user\",\"content\":\"" + URLEncoder.encode(prompt, StandardCharsets.UTF_8) + "\"}]," +
                "\"temperature\":0.5," +
                "\"max_tokens\":150}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            System.out.println("API Response: " + responseBody); // Debugging statement

            if (response.statusCode() == 200) {
                String content = extractContent(responseBody);
                return content;
            } else {
                System.out.println("Error Status Code: " + response.statusCode()); // Debugging statement
                return "Error occurred while generating feedback. Status code: " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error occurred while generating feedback.";
        }
    }

    private String extractContent(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
            return "Unable to extract content from the API response.";
        }
    }

    private void saveFeedbackToFile(String feedback) {
        JFileChooser fileChooser = new JFileChooser();
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                FileWriter writer = new FileWriter(filePath);
                writer.write(feedback);
                writer.close();
                JOptionPane.showMessageDialog(this, "Feedback saved successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error occurred while saving feedback.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CodeComparisonApp();
            }
        });
    }
}