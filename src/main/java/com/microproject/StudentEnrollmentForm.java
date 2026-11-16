
package com.microproject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StudentEnrollmentForm extends JFrame {

    private final JTextField rollNoField = new JTextField(20);
    private final JTextField fullNameField = new JTextField(20);
    private final JTextField classField = new JTextField(20);
    private final JTextField birthDateField = new JTextField(20);
    private final JTextArea addressArea = new JTextArea(3, 20);
    private final JTextField enrollmentDateField = new JTextField(20);

    private final JButton saveButton = new JButton("Save");
    private final JButton updateButton = new JButton("Update");
    private final JButton resetButton = new JButton("Reset");

    private final JsonPowerDBService dbService = new JsonPowerDBService();

    private boolean lookupInProgress = false;

    public StudentEnrollmentForm() {

        setTitle("Student Enrollment Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        buildInterface();
        attachEvents();
        resetForm();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                rollNoField.requestFocusInWindow();
            }
        });
    }

    private void buildInterface() {

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(new EmptyBorder(18, 22, 18, 22));
        main.setBackground(new Color(245, 247, 250));

        JLabel heading = new JLabel(
                "STUDENT ENROLLMENT FORM",
                SwingConstants.CENTER
        );

        heading.setFont(new Font("SansSerif", Font.BOLD, 21));
        heading.setForeground(new Color(35, 55, 80));

        main.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);

        fields.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232)),
                new EmptyBorder(18, 18, 18, 18)
        ));

        addField(fields, "Roll-No (Primary Key)", rollNoField, 0);
        addField(fields, "Full Name", fullNameField, 1);
        addField(fields, "Class", classField, 2);
        addField(fields, "Birth Date (yyyy-MM-dd)", birthDateField, 3);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        fields.add(new JLabel("Address"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        fields.add(new JScrollPane(addressArea), gbc);

        addField(fields, "Enrollment Date (yyyy-MM-dd)",
                enrollmentDateField, 5);

        main.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 14, 5)
        );

        buttons.setBackground(new Color(245, 247, 250));

        saveButton.setPreferredSize(new Dimension(110, 36));
        updateButton.setPreferredSize(new Dimension(110, 36));
        resetButton.setPreferredSize(new Dimension(110, 36));

        buttons.add(saveButton);
        buttons.add(updateButton);
        buttons.add(resetButton);

        main.add(buttons, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void addField(
            JPanel panel,
            String label,
            JTextField field,
            int row
    ) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(field, gbc);
    }

    private void attachEvents() {

        rollNoField.addActionListener(e -> lookupStudent());

        rollNoField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookupStudent();
            }
        });

        saveButton.addActionListener(e -> saveStudent());
        updateButton.addActionListener(e -> updateStudent());
        resetButton.addActionListener(e -> resetForm());
    }

    // SEARCH STUDENT USING JSONPOWERDB

    private void lookupStudent() {

        if (lookupInProgress || !rollNoField.isEnabled()) {
            return;
        }

        String rollText = rollNoField.getText().trim();

        if (rollText.isEmpty()) {
            return;
        }

        final int rollNo;

        try {
            rollNo = Integer.parseInt(rollText);

            if (rollNo <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {

            showError("Roll-No must be a positive whole number.");
            rollNoField.requestFocusInWindow();
            return;
        }

        lookupInProgress = true;

        try {

            JsonObject response = dbService.getStudent(rollNo);

            int status = response.get("status").getAsInt();

            if (status != 200) {

                String message = response.has("message")
                        ? response.get("message").getAsString()
                        : "Unknown database response";

                // A missing record means this is a new student.
                if (status == 400 &&
                        message.toLowerCase().contains("not found")) {

                    prepareNewStudent();
                    return;
                }

                showError(message);
                return;
            }

            // JSONPowerDB returns the actual record inside a JSON string.
            String dataString = response.get("data").getAsString();

            if (dataString == null || dataString.isBlank()) {
                prepareNewStudent();
                return;
            }

            JsonObject data = JsonParser.parseString(dataString)
                    .getAsJsonObject();

            // The actual student information is inside "record".
            if (!data.has("record") || data.get("record").isJsonNull()) {
                prepareNewStudent();
                return;
            }

            JsonObject student = data.getAsJsonObject("record");

            fullNameField.setText(getString(student, "Full_Name"));
            classField.setText(getString(student, "Class"));
            birthDateField.setText(getString(student, "Birth_Date"));
            addressArea.setText(getString(student, "Address"));
            enrollmentDateField.setText(
                    getString(student, "Enrollment_Date")
            );

            setDetailsEnabled(true);

            rollNoField.setEnabled(false);
            saveButton.setEnabled(false);
            updateButton.setEnabled(true);
            resetButton.setEnabled(true);

            fullNameField.requestFocusInWindow();

        } catch (Exception ex) {

            showDatabaseError(ex);

        } finally {

            lookupInProgress = false;
        }
    }

    private String getString(JsonObject object, String key) {

        if (!object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }

        return object.get(key).getAsString();
    }

    private void prepareNewStudent() {

        clearDetails();

        setDetailsEnabled(true);

        rollNoField.setEnabled(true);
        saveButton.setEnabled(true);
        updateButton.setEnabled(false);
        resetButton.setEnabled(true);

        fullNameField.requestFocusInWindow();
    }

    // SAVE NEW STUDENT

    private void saveStudent() {

        StudentData data = readAndValidateForm();

        if (data == null) {
            return;
        }

        try {

            JsonObject student = createStudentJson(data);

            JsonObject response = dbService.insertStudent(student);

            if (response.get("status").getAsInt() == 200) {

                JOptionPane.showMessageDialog(
                        this,
                        "Student record saved successfully.",
                        "Saved",
                        JOptionPane.INFORMATION_MESSAGE
                );

                resetForm();

            } else {

                showError(response.get("message").getAsString());
            }

        } catch (Exception ex) {

            showDatabaseError(ex);
        }
    }

    // UPDATE EXISTING STUDENT

    private void updateStudent() {

        StudentData data = readAndValidateForm();

        if (data == null) {
            return;
        }

        try {

            JsonObject student = createStudentJson(data);

            JsonObject response = dbService.updateStudent(student);

            if (response.get("status").getAsInt() == 200) {

                JOptionPane.showMessageDialog(
                        this,
                        "Student record updated successfully.",
                        "Updated",
                        JOptionPane.INFORMATION_MESSAGE
                );

                resetForm();

            } else {

                showError(response.get("message").getAsString());
            }

        } catch (Exception ex) {

            showDatabaseError(ex);
        }
    }

    // CONVERT FORM DATA INTO JSON

    private JsonObject createStudentJson(StudentData data) {

        JsonObject student = new JsonObject();

        student.addProperty("Roll_No", data.rollNo);
        student.addProperty("Full_Name", data.fullName);
        student.addProperty("Class", data.studentClass);
        student.addProperty("Birth_Date", data.birthDate.toString());
        student.addProperty("Address", data.address);
        student.addProperty(
                "Enrollment_Date",
                data.enrollmentDate.toString()
        );

        return student;
    }

    // FORM VALIDATION

    private StudentData readAndValidateForm() {

        String rollText = rollNoField.getText().trim();
        String name = fullNameField.getText().trim();
        String studentClass = classField.getText().trim();
        String birthText = birthDateField.getText().trim();
        String address = addressArea.getText().trim();
        String enrollmentText = enrollmentDateField.getText().trim();

        if (rollText.isEmpty() || name.isEmpty()
                || studentClass.isEmpty() || birthText.isEmpty()
                || address.isEmpty() || enrollmentText.isEmpty()) {

            showError("All fields are mandatory. Please complete the form.");
            return null;
        }

        int rollNo;

        try {

            rollNo = Integer.parseInt(rollText);

            if (rollNo <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {

            showError("Roll-No must be a positive whole number.");
            rollNoField.requestFocusInWindow();
            return null;
        }

        if (name.length() > 100
                || studentClass.length() > 30
                || address.length() > 255) {

            showError(
                    "Full Name (100), Class (30), or Address (255) "
                            + "exceeds its maximum length."
            );

            return null;
        }

        try {

            LocalDate birthDate = LocalDate.parse(birthText);
            LocalDate enrollmentDate = LocalDate.parse(enrollmentText);

            return new StudentData(
                    rollNo,
                    name,
                    studentClass,
                    birthDate,
                    address,
                    enrollmentDate
            );

        } catch (DateTimeParseException ex) {

            showError(
                    "Enter both dates in yyyy-MM-dd format, "
                            + "for example 2005-08-21."
            );

            return null;
        }
    }

    // RESET FORM

    private void resetForm() {

        rollNoField.setText("");

        clearDetails();

        rollNoField.setEnabled(true);

        setDetailsEnabled(false);

        saveButton.setEnabled(false);
        updateButton.setEnabled(false);
        resetButton.setEnabled(false);

        rollNoField.requestFocusInWindow();
    }

    private void clearDetails() {

        fullNameField.setText("");
        classField.setText("");
        birthDateField.setText("");
        addressArea.setText("");
        enrollmentDateField.setText("");
    }

    private void setDetailsEnabled(boolean enabled) {

        fullNameField.setEnabled(enabled);
        classField.setEnabled(enabled);
        birthDateField.setEnabled(enabled);
        addressArea.setEnabled(enabled);
        enrollmentDateField.setEnabled(enabled);
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation / Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showDatabaseError(Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "Could not complete the JSONPowerDB operation."
                        + "\n\nDetails: " + ex.getMessage(),
                "JSONPowerDB Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // STUDENT DATA MODEL

    private static class StudentData {

        final int rollNo;
        final String fullName;
        final String studentClass;
        final LocalDate birthDate;
        final String address;
        final LocalDate enrollmentDate;

        StudentData(
                int rollNo,
                String fullName,
                String studentClass,
                LocalDate birthDate,
                String address,
                LocalDate enrollmentDate
        ) {

            this.rollNo = rollNo;
            this.fullName = fullName;
            this.studentClass = studentClass;
            this.birthDate = birthDate;
            this.address = address;
            this.enrollmentDate = enrollmentDate;
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
                new StudentEnrollmentForm().setVisible(true)
        );
    }
}
