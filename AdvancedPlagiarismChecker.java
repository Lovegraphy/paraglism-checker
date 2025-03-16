import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.PDFTextStripper;

public class AdvancedPlagiarismChecker {
    private JTextArea referenceText, userText;
    private JLabel similarityLabel, grammarLabel, wordCountLabel, readabilityLabel;
    private JButton analyzeButton, saveResultButton, uploadPdfRefButton, uploadPdfUserButton;

    public AdvancedPlagiarismChecker() {
        JFrame frame = new JFrame("Advanced Plagiarism Detector");
        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        referenceText = new JTextArea(15, 50);
        referenceText.setBorder(BorderFactory.createTitledBorder("Reference Text"));
        referenceText.setLineWrap(true);
        referenceText.setWrapStyleWord(true);

        userText = new JTextArea(15, 50);
        userText.setBorder(BorderFactory.createTitledBorder("User's Text"));
        userText.setLineWrap(true);
        userText.setWrapStyleWord(true);

        uploadPdfRefButton = new JButton("Upload PDF (Reference)");
        uploadPdfUserButton = new JButton("Upload PDF (User)");
        JPanel pdfPanel = new JPanel();
        pdfPanel.add(uploadPdfRefButton);
        pdfPanel.add(uploadPdfUserButton);

        inputPanel.add(new JScrollPane(referenceText));
        inputPanel.add(new JScrollPane(userText));

        JPanel resultPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        similarityLabel = createStyledLabel("Similarity: 0%");
        grammarLabel = createStyledLabel("Grammar: N/A");
        wordCountLabel = createStyledLabel("Word Count: 0");
        readabilityLabel = createStyledLabel("Readability: N/A");

        resultPanel.add(similarityLabel);
        resultPanel.add(grammarLabel);
        resultPanel.add(wordCountLabel);
        resultPanel.add(readabilityLabel);

        mainPanel.add(inputPanel);
        mainPanel.add(resultPanel);

        JPanel actionPanel = new JPanel(new FlowLayout());
        analyzeButton = new JButton("Analyze");
        analyzeButton.setFont(new Font("Arial", Font.BOLD, 22));
        analyzeButton.setBackground(new Color(0, 150, 0));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setPreferredSize(new Dimension(200, 50));

        saveResultButton = new JButton("Save Result");
        saveResultButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveResultButton.setBackground(new Color(0, 102, 204));
        saveResultButton.setForeground(Color.WHITE);

        actionPanel.add(analyzeButton);
        actionPanel.add(saveResultButton);

        analyzeButton.addActionListener(e -> analyzeText());
        saveResultButton.addActionListener(e -> saveResults());
        uploadPdfRefButton.addActionListener(e -> loadPdf(referenceText));
        uploadPdfUserButton.addActionListener(e -> loadPdf(userText));

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(actionPanel, BorderLayout.SOUTH);
        frame.add(pdfPanel, BorderLayout.NORTH);
        frame.setVisible(true);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        return label;
    }

    private void analyzeText() {
        String refText = referenceText.getText();
        String userTextData = userText.getText();

        if (refText.isEmpty() || userTextData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter or upload both texts.");
            return;
        }

        double similarityScore = evaluateSimilarity(refText, userTextData);
        similarityLabel.setText("Similarity: " + (int) (similarityScore * 100) + "%");
        similarityLabel.setForeground(similarityScore > 0.5 ? Color.RED : Color.GREEN);

        int grammarScore = assessGrammar(userTextData);
        grammarLabel.setText("Grammar Accuracy: " + grammarScore + "%");

        int words = countWords(userTextData);
        wordCountLabel.setText("Word Count: " + words);

        double readability = computeReadability(userTextData);
        readabilityLabel.setText("Readability Score: " + String.format("%.2f", readability));
    }

    private void saveResults() {
        try {
            FileWriter writer = new FileWriter("plagiarism_results.txt");
            writer.write("Plagiarism Report\n");
            writer.write(similarityLabel.getText() + "\n");
            writer.write(grammarLabel.getText() + "\n");
            writer.write(wordCountLabel.getText() + "\n");
            writer.write(readabilityLabel.getText() + "\n");
            writer.close();
            JOptionPane.showMessageDialog(null, "Results saved successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error saving results.");
        }
    }

    private void loadPdf(JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String extractedText = stripper.getText(document);
                textArea.setText(extractedText.isEmpty() ? "No text found in PDF." : extractedText);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error reading PDF: " + ex.getMessage());
            }
        }
    }

    private double evaluateSimilarity(String original, String analyzed) {
        Set<String> refWords = new HashSet<>(Arrays.asList(original.toLowerCase().split("\\s+")));
        Set<String> userWords = new HashSet<>(Arrays.asList(analyzed.toLowerCase().split("\\s+")));
        Set<String> commonWords = new HashSet<>(refWords);
        commonWords.retainAll(userWords);
        return refWords.isEmpty() ? 0 : (double) commonWords.size() / refWords.size();
    }

    private int assessGrammar(String text) {
        int mistakes = 0;
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.length() > 20) mistakes++;
        }
        return Math.max(100 - (mistakes * 5), 0);
    }

    private int countWords(String text) {
        return text.trim().isEmpty() ? 0 : text.split("\\s+").length;
    }

    private double computeReadability(String text) {
        int words = countWords(text);
        return words > 0 ? 206.835 - (1.015 * words) : 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedPlagiarismChecker::new);
    }
}
