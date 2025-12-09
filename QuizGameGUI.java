package project_1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class QuizGameGUI {
    private static Map<String, Quiz> quizzes = new HashMap<>();
    private static JFrame frame;
    private static JPanel mainPanel, quizPanel, resultPanel;
    private static JLabel questionLabel;
    private static JCheckBox[] options;
    private static JButton nextButton;
    private static Quiz currentQuiz;
    private static int currentQuestionIndex = 0;
    private static int score = 0;
    private static String playerName; // ชื่อผู้เล่น

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizGameGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Quiz Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1800, 820);

        mainPanel = new JPanel();
        mainPanel.setBackground(new Color(74, 189, 236));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        loadQuizzesFromFile();

        createMainMenuButtons(); // สร้างปุ่มเมนูหลัก

        frame.setContentPane(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private static void createMainMenuButtons() {
        mainPanel.removeAll(); // เคลียร์ปุ่มที่มีอยู่ (สำคัญสำหรับการกลับเมนูหลัก)
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Set layout again

        JButton createQuizButton = new JButton("Create Quiz");
        JButton takeQuizButton = new JButton("Take Quiz");
        JButton viewQuizzesButton = new JButton("View Quizzes");
        JButton viewResultsButton = new JButton("View Results");
        JButton deleteQuizButton = new JButton("Delete Quiz");
        JButton exitAppButton = new JButton("Exit Application");

        styleButton(createQuizButton);
        styleButton(takeQuizButton);
        styleButton(viewQuizzesButton);
        styleButton(viewResultsButton);
        styleButton(deleteQuizButton);
        styleButton(exitAppButton);

        createQuizButton.addActionListener(e -> createQuiz());
        takeQuizButton.addActionListener(e -> takeQuiz());
        viewQuizzesButton.addActionListener(e -> viewQuizzes());
        viewResultsButton.addActionListener(e -> SaveData.viewSavedResults());
        deleteQuizButton.addActionListener(e -> deleteQuiz());
        exitAppButton.addActionListener(e -> System.exit(0));

        createQuizButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        takeQuizButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewQuizzesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewResultsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteQuizButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitAppButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(createQuizButton);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(takeQuizButton);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(viewQuizzesButton);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(viewResultsButton);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(deleteQuizButton);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(exitAppButton);
        mainPanel.add(Box.createVerticalGlue());

        frame.setContentPane(mainPanel); // Make sure mainPanel is set as content pane
        frame.revalidate();
        frame.repaint();
    }


    private static boolean isDataLoaded = false; // ป้องกันการโหลดซ้ำ

    private static void loadQuizzesFromFile() {
        if (isDataLoaded) return; // ถ้าโหลดแล้ว ให้ return ทันที

        try (BufferedReader reader = new BufferedReader(new FileReader("D:/quiz_data.txt"))) {
            String line;
            Quiz currentQuiz = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Quiz: ")) {
                    // เริ่ม Quiz ใหม่
                    String quizName = line.substring(6).trim();
                    currentQuiz = new Quiz(quizName);
                    quizzes.put(quizName, currentQuiz);
                } else if (line.startsWith("Q: ") && currentQuiz != null) {
                    // อ่านคำถาม
                    String questionText = line.substring(3).trim();
                    List<String> choices = new ArrayList<>();
                    int correctChoice = -1;

                    // อ่านตัวเลือก
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("Choices: ")) {
                            continue; // ข้ามบรรทัด "Choices:"
                        } else if (line.startsWith("Correct Choice: ")) {
                            correctChoice = Integer.parseInt(line.substring(15).trim()) - 1;
                            break; // ออกจาก loop เมื่อเจอ "Correct Choice:"
                        }
                        choices.add(line.trim()); // เพิ่มตัวเลือก
                    }

                    // เพิ่มคำถามเข้าไปใน Quiz
                    if (!choices.isEmpty() && correctChoice >= 0) {
                        currentQuiz.addQuestion(new Question(questionText, choices, correctChoice));
                    }
                }
            }

            isDataLoaded = true; // ตั้งค่าว่าโหลดเสร็จแล้ว
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading quizzes: " + e.getMessage());
        }
    }
    private static void createQuiz() {
        frame.getContentPane().removeAll();
        JPanel quizCreationPanel = new JPanel();
        quizCreationPanel.setLayout(new BoxLayout(quizCreationPanel, BoxLayout.Y_AXIS));

        Font inputFont = new Font("Arial", Font.PLAIN, 30);

        JTextField quizNameField = new JTextField(20);
        quizNameField.setFont(inputFont);
        quizCreationPanel.add(new JLabel("Enter Quiz Name:"));
        quizCreationPanel.add(quizNameField);

        JTextField questionField = new JTextField(40);
        questionField.setFont(inputFont);
        quizCreationPanel.add(new JLabel("Enter Question:"));
        quizCreationPanel.add(questionField);

        JTextField[] choiceFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            choiceFields[i] = new JTextField(30);
            choiceFields[i].setFont(inputFont);
            quizCreationPanel.add(new JLabel("Choice " + (i + 1) + ":"));
            quizCreationPanel.add(choiceFields[i]);
        }

        JTextField correctChoiceField = new JTextField(5);
        correctChoiceField.setFont(inputFont);
        quizCreationPanel.add(new JLabel("Enter Correct Choice (1-4):"));
        quizCreationPanel.add(correctChoiceField);

        JButton addQuestionButton = new JButton("Add Question");
        JButton saveQuizButton = new JButton("Save Quiz");
        JButton backButton = new JButton("Back to Menu");
        quizCreationPanel.add(addQuestionButton);
        quizCreationPanel.add(saveQuizButton);
        quizCreationPanel.add(backButton);

        final Quiz[] newQuiz = {null};

        addQuestionButton.addActionListener(e -> {
            String quizName = quizNameField.getText().trim();
            String questionText = questionField.getText().trim();
            List<String> choices = new ArrayList<>();

            for (JTextField field : choiceFields) {
                choices.add(field.getText().trim());
            }

            int correctChoice;
            try {
                correctChoice = Integer.parseInt(correctChoiceField.getText().trim()) - 1;
                if (correctChoice < 0 || correctChoice >= choices.size()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid correct choice input.");
                return;
            }

            if (quizName.isEmpty() || questionText.isEmpty() || choices.contains("")) {
                JOptionPane.showMessageDialog(frame, "All fields must be filled.");
                return;
            }

            if (newQuiz[0] == null) {
                newQuiz[0] = new Quiz(quizName);
            }
            newQuiz[0].addQuestion(new Question(questionText, choices, correctChoice));
            JOptionPane.showMessageDialog(frame, "Question Added!");
            questionField.setText("");
            correctChoiceField.setText("");
            for (JTextField field : choiceFields) {
                field.setText("");
            }
        });

        saveQuizButton.addActionListener(e -> {
            if (newQuiz[0] == null || newQuiz[0].getNumQuestions() == 0) {
                JOptionPane.showMessageDialog(frame, "No questions added.");
                return;
            }
            quizzes.put(newQuiz[0].getName(), newQuiz[0]);
            saveQuiz(newQuiz[0]);
            JOptionPane.showMessageDialog(frame, "Quiz Saved Successfully!");
            createMainMenuButtons();
        });

        backButton.addActionListener(e -> createMainMenuButtons());

        frame.add(quizCreationPanel);
        frame.revalidate();
        frame.repaint();
    }

    private static void displayQuestion() {
        if (currentQuestionIndex >= currentQuiz.getNumQuestions()) {
            showResult(); // แสดงผลลัพธ์ถ้าหมดคำถาม
            return;
        }

        // ดึงคำถามปัจจุบัน
        Question question = currentQuiz.getQuestion(currentQuestionIndex);
        questionLabel.setText((currentQuestionIndex + 1) + ". " + question.getQuestion()); // แสดงเลขข้อด้วย

        // ดึงตัวเลือกของคำถาม
        List<String> choices = question.getChoices();

        for (int i = 0; i < options.length; i++) {
            if (i < choices.size()) {
                options[i].setText(choices.get(i));
                options[i].setSelected(false); // รีเซ็ตการเลือก
                options[i].setVisible(true);
            } else {
                options[i].setVisible(false); // ซ่อนตัวเลือกที่เกิน
            }
        }

        frame.revalidate();
        frame.repaint(); // รีเฟรช UI ให้เปลี่ยนคำถามใหม่
    }

    private static void showResult() {
        resultPanel = new JPanel(); // สร้าง resultPanel ใหม่ทุกครั้ง
        resultPanel.setLayout(new BorderLayout());

        JLabel resultLabel = new JLabel("Your Score: " + score + "/" + currentQuiz.getNumQuestions());
        resultLabel.setFont(new Font("Arial", Font.BOLD, 40));
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton backToMainButton = new JButton("Back to Main Menu");
        styleButton(backToMainButton);
        backToMainButton.addActionListener(e -> createMainMenuButtons()); // ใช้เมธอด backToMainMenu
        buttonPanel.add(backToMainButton);

        JButton exitButton = new JButton("Exit Program");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 16));
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        resultPanel.add(buttonPanel, BorderLayout.SOUTH);


        frame.getContentPane().removeAll();
        frame.setContentPane(resultPanel); // ใช้ setContentPane เพื่อเปลี่ยน panel
        frame.revalidate();
        frame.repaint();
        frame.invalidate();
    }


    private static void checkAnswer() {
        Question question = currentQuiz.getQuestion(currentQuestionIndex);
        int correctChoiceIndex = question.getCorrectChoice();
        boolean isCorrect = false;

        // ตรวจสอบว่าผู้ใช้เลือกตัวเลือกที่ถูกต้องหรือไม่
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected() && i == correctChoiceIndex) {
                isCorrect = true;
            }
        }

        if (isCorrect) {
            score++; // เพิ่มคะแนนถ้าตอบถูก
        }

        currentQuestionIndex++; // ไปยังคำถามถัดไป

        if (currentQuestionIndex < currentQuiz.getNumQuestions()) {
            displayQuestion(); // แสดงคำถามใหม่
        } else {
            SaveData.saveQuizResults(playerName, currentQuiz.getName(), score, currentQuiz.getNumQuestions());
            showResult(); // แสดงผลลัพธ์เมื่อหมดคำถาม
        }
    }

    private static void takeQuiz() {
        if (quizzes.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No quizzes available!");
            return;
        }

        // สร้าง JPanel สำหรับให้ใส่ชื่อผู้เล่นและชื่อ Quiz
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Enter your name:"));
        JTextField nameField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Enter quiz name:"));
        JTextField quizField = new JTextField();
        inputPanel.add(quizField);

        int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Enter Details", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return; // ออกจากเมธอดถ้าผู้ใช้กด Cancel
        }

        playerName = nameField.getText().trim();
        String quizName = quizField.getText().trim();

        if (playerName.isEmpty() || quizName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Name and Quiz name cannot be empty!");
            return;
        }

        currentQuiz = quizzes.get(quizName);
        if (currentQuiz == null) {
            JOptionPane.showMessageDialog(frame, "Quiz not found!");
            return;
        }

        currentQuestionIndex = 0;
        score = 0;

        quizPanel = new JPanel();
        quizPanel.setLayout(new GridLayout(6, 1));
        quizPanel.setBackground(new Color(240, 248, 255));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        quizPanel.add(questionLabel);

        options = new JCheckBox[4];
        for (int i = 0; i < 4; i++) {
            options[i] = new JCheckBox();
            options[i].setFont(new Font("Arial", Font.PLAIN, 16));
            quizPanel.add(options[i]);
        }

        nextButton = new JButton("Next");
        styleButton(nextButton);
        nextButton.addActionListener(e -> checkAnswer());
        quizPanel.add(nextButton);

        frame.getContentPane().removeAll();
        frame.add(quizPanel);
        frame.revalidate();
        frame.repaint();

        displayQuestion();
    }

    private static void viewQuizzes() {
        if (quizzes.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No quizzes available!");
            return;
        }
        StringBuilder quizList = new StringBuilder("Available Quizzes:\n");
        for (String quizName : quizzes.keySet()) {
            quizList.append("- ").append(quizName).append("\n");
        }
        JOptionPane.showMessageDialog(frame, quizList.toString());
    }

    private static void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(300, 70)); // ขยายขนาดปุ่มให้ใหญ่ขึ้น
        button.setMaximumSize(new Dimension(300, 70));   // จำกัดขนาดสูงสุดให้ตรงกับขนาดที่ต้องการ
        button.setBackground(new Color(255, 128, 0));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 20)); // ขยายขนาดตัวอักษร
        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 7, true));
    }
    private static void saveQuiz(Quiz quizToSave) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("D:/quiz_data.txt", true))) {
            writer.write("Quiz: " + quizToSave.getName());
            writer.newLine();
            writer.write("Questions: ");
            writer.newLine();

            for (Question question : quizToSave.getQuestions()) {
                writer.write("Q: " + question.getQuestion());
                writer.newLine();
                writer.write("Choices: ");
                writer.newLine();
                for (String choice : question.getChoices()) {
                    writer.write(choice);
                    writer.newLine();
                }
                writer.write("Correct Choice: " + (question.getCorrectChoice() + 1)); // 1-based index
                writer.newLine();
            }

            writer.write("--- End of Quiz ---");
            writer.newLine();
            JOptionPane.showMessageDialog(frame, "Quiz saved successfully!");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving quiz: " + e.getMessage());
        }

    }
    private static void deleteQuiz() {
        if (quizzes.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No quizzes available to delete!");
            return;
        }

        String[] quizNames = quizzes.keySet().toArray(new String[0]);
        String quizToDelete = (String) JOptionPane.showInputDialog(
                frame,
                "Select a quiz to delete:",
                "Delete Quiz",
                JOptionPane.QUESTION_MESSAGE,
                null,
                quizNames,
                quizNames[0]
        );

        if (quizToDelete != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to delete quiz: " + quizToDelete + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                quizzes.remove(quizToDelete);
                if (deleteQuizFromFile(quizToDelete)) {
                    JOptionPane.showMessageDialog(frame, "Quiz '" + quizToDelete + "' deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Error deleting quiz from file.");
                    // Optionally, you might want to reload quizzes from file here to ensure data consistency
                    loadQuizzesFromFile(); // Reload quizzes if deletion from file failed to sync in memory
                }
            }
        }
    }

    private static boolean deleteQuizFromFile(String quizNameToDelete) {
        File inputFile = new File("D:/quiz_data.txt");
        File tempFile = new File("D:/temp_quiz_data.txt");
        boolean quizDeleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean inQuizToDelete = false;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Quiz: ")) {
                    if (line.substring(6).trim().equals(quizNameToDelete)) {
                        inQuizToDelete = true;
                        quizDeleted = true; // Quiz name found, mark as deleted in file (logically)
                        continue; // Skip writing lines of this quiz to temp file
                    } else {
                        inQuizToDelete = false; // Start of a new quiz, not the one to delete
                    }
                }
                if (!inQuizToDelete) {
                    writer.write(line + System.lineSeparator()); // Write line to temp file if not part of quiz to delete
                } else if (line.equals("--- End of Quiz ---")) {
                    inQuizToDelete = false; // Quiz section ended, stop skipping for next quizzes
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error processing quiz file: " + e.getMessage());
            return false; // Deletion failed
        }

        // Replace original file with temp file
        if (quizDeleted && tempFile.exists()) { // Proceed only if quiz was actually found and marked for deletion
            if (inputFile.delete()) { // Delete original
                if (!tempFile.renameTo(inputFile)) { // Rename temp to original
                    JOptionPane.showMessageDialog(frame, "Error renaming temporary file.");
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Error deleting original quiz data file.");
                return false;
            }
        } else if (!quizDeleted) {
            JOptionPane.showMessageDialog(frame, "Quiz '" + quizNameToDelete + "' not found in data file.");
            tempFile.delete(); // Clean up temp file if quiz wasn't deleted to avoid orphaned temp file
            return false;
        }


        isDataLoaded = false; // Force reload of quizzes from file to update in memory
        loadQuizzesFromFile(); // Reload quizzes to reflect changes in memory
        return quizDeleted; // Return true if quiz was logically deleted and file updated (or attempted)
    }

    class SaveData {
        private static final String FILE_NAME = "D:/name_data.txt"; // กำหนดพาธไฟล์

        public static void saveQuizResults(String playerName, String quizName, int score, int totalQuestions) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                writer.write("Player: " + playerName + ", Quiz: " + quizName + ", Score: " + score + "/" + totalQuestions);
                writer.newLine();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving results: " + e.getMessage());
            }
        }

        public static void viewSavedResults() {
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
                StringBuilder resultData = new StringBuilder("--- Saved Quiz Results ---\n");
                String line;
                while ((line = reader.readLine()) != null) {
                    resultData.append(line).append("\n");
                }
                JOptionPane.showMessageDialog(null, resultData.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "No saved results found.");
            }
        }
    }
    static class Quiz {
        private String name;
        private List<Question> questions = new ArrayList<>();


        public Quiz(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void addQuestion(Question question) {
            questions.add(question);
        }

        public Question getQuestion(int index) {
            return questions.get(index);
        }

        public int getNumQuestions() {
            return questions.size();
        }

        public List<Question> getQuestions() {
            return questions;
        }
    }

    static class Question {
        private String question;
        private List<String> choices;
        private int correctChoice;

        public Question(String question, List<String> choices, int correctChoice) {
            this.question = question;
            this.choices = choices;
            this.correctChoice = correctChoice;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getChoices() {
            return choices;
        }

        public int getCorrectChoice() {
            return correctChoice;
        }
    }
}