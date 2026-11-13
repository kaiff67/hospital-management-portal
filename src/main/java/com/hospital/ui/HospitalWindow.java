package com.hospital.ui;

import com.hospital.model.*;
import com.hospital.service.*;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Swing front end. Business rules and JDBC remain in the service and DAO layers. */
public class HospitalWindow extends JFrame {
    private final AuthService auth = new AuthService();
    private final PatientService patients = new PatientService();
    private final DirectoryService directory = new DirectoryService();
    private final AppointmentService appointments = new AppointmentService();
    private final ClinicalService clinical = new ClinicalService();
    private final BillingService billing = new BillingService();
    private User user;

    public HospitalWindow() {
        setTitle("Hospital Management Portal");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 560));
        setLocationRelativeTo(null);
        showLogin();
    }

    private void showLogin() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 7, 7, 7);
        c.anchor = GridBagConstraints.WEST;
        JTextField username = new JTextField(22);
        JPasswordField password = new JPasswordField(22);
        JLabel message = new JLabel(" ");
        JButton login = new JButton("Sign in");
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Username"), c);
        c.gridx = 1; form.add(username, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Password"), c);
        c.gridx = 1; form.add(password, c);
        c.gridx = 1; c.gridy = 2; form.add(login, c);
        c.gridy = 3; form.add(message, c);
        login.addActionListener(e -> {
            try {
                user = auth.login(username.getText().trim(), new String(password.getPassword()));
                showDashboard();
            } catch (Exception ex) {
                message.setText("Sign in failed. Check the username, password, and account status.");
                password.setText("");
            }
        });
        password.addActionListener(e -> login.doClick());
        JPanel page = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Hospital Management Portal", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        page.add(title, BorderLayout.NORTH);
        page.add(form, BorderLayout.CENTER);
        setContentPane(page);
        setSize(500, 330);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showDashboard() {
        JTabbedPane tabs = new JTabbedPane();
        if (user.role() == Role.ADMIN || user.role() == Role.RECEPTIONIST || user.role() == Role.DOCTOR)
            tabs.addTab("Patients", patientTab());
        if (user.role() != Role.BILLING_STAFF) tabs.addTab("Doctors", doctorTab());
        if (user.role() == Role.ADMIN || user.role() == Role.RECEPTIONIST || user.role() == Role.DOCTOR)
            tabs.addTab("Departments", departmentTab());
        if (user.role() != Role.BILLING_STAFF) tabs.addTab("Appointments", appointmentTab());
        if (user.role() == Role.ADMIN || user.role() == Role.DOCTOR) {
            tabs.addTab("Medical records", recordsTab());
            tabs.addTab("Prescriptions", prescriptionTab());
        }
        if (user.role() == Role.ADMIN || user.role() == Role.BILLING_STAFF)
            tabs.addTab("Billing", billingTab());
        if (user.role() == Role.ADMIN || user.role() == Role.BILLING_STAFF || user.role() == Role.RECEPTIONIST)
            tabs.addTab("Reports", reportTab());
        if (user.role() == Role.ADMIN) tabs.addTab("Users", userTab());

        JLabel signedIn = new JLabel("Signed in as " + user.username() + " (" + user.role() + ")  ");
        JButton changePassword = new JButton("Change password");
        JButton logout = new JButton("Log out");
        changePassword.addActionListener(e -> changePassword());
        logout.addActionListener(e -> { user = null; showLogin(); });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(signedIn); top.add(changePassword); top.add(logout);
        JPanel page = new JPanel(new BorderLayout());
        page.add(top, BorderLayout.NORTH); page.add(tabs, BorderLayout.CENTER);
        setContentPane(page);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        revalidate(); repaint();
    }

    private JPanel patientTab() {
        List<String> labels = new ArrayList<>(List.of("Search patients", "View by ID"));
        List<Action> actions = new ArrayList<>(List.of(this::searchPatients, this::viewPatient));
        if (user.role() != Role.DOCTOR) {
            labels.addAll(List.of("Register patient", "Update patient", "Deactivate patient"));
            actions.addAll(List.of(this::registerPatient, this::updatePatient, this::deactivatePatient));
        }
        return dataTab(labels, actions);
    }

    private void searchPatients(JTextArea out) throws Exception {
        String term = ask("Name, phone, or patient ID (blank lists names):");
        int page = Integer.parseInt(ask("Page number (start with 1):"));
        List<String> rows = new ArrayList<>();
        for (Patient p : patients.search(user, term, page)) rows.add(p.toString());
        display(out, rows);
    }

    private void viewPatient(JTextArea out) throws Exception { display(out, List.of(patients.get(user, number("Patient ID:" )).toString())); }
    private void registerPatient(JTextArea out) throws Exception {
        long id = patients.register(user, ask("Full name:"), ask("Birth date (YYYY-MM-DD):"), ask("Gender:"),
                ask("Phone:"), ask("Address:"), ask("Blood group (optional):"));
        display(out, List.of("Patient registered. ID: " + id));
    }
    private void updatePatient(JTextArea out) throws Exception {
        long id = number("Patient ID:");
        patients.update(user, id, ask("Full name:"), ask("Birth date (YYYY-MM-DD):"), ask("Gender:"),
                ask("Phone:"), ask("Address:"), ask("Blood group (optional):"));
        display(out, List.of("Patient updated."));
    }
    private void deactivatePatient(JTextArea out) throws Exception {
        long id = number("Patient ID:");
        if (confirm("Deactivate patient " + id + "? Their history will be preserved.")) {
            patients.deactivate(user, id); display(out, List.of("Patient deactivated."));
        }
    }

    private JPanel doctorTab() {
        List<String> labels = new ArrayList<>(List.of("List / search doctors"));
        List<Action> actions = new ArrayList<>(List.of(out -> display(out, directory.doctors(user, ask("Name or specialization (blank for all):")))));
        if (user.role() == Role.ADMIN) {
            labels.addAll(List.of("Add doctor", "Set availability", "Activate / deactivate"));
            actions.addAll(List.of(this::addDoctor, this::setAvailability, this::doctorStatus));
        }
        return dataTab(labels, actions);
    }
    private void addDoctor(JTextArea out) throws Exception {
        long id = directory.addDoctor(user, ask("Doctor name:"), ask("Specialization:"), number("Department ID:"),
                ask("Phone:"), new BigDecimal(ask("Consultation fee:")));
        display(out, List.of("Doctor added. ID: " + id));
    }
    private void setAvailability(JTextArea out) throws Exception {
        long id = number("Doctor ID:");
        int weekday = Integer.parseInt(ask("Weekday (1=Monday through 7=Sunday):"));
        Time start = Time.valueOf(LocalTime.parse(ask("Start time (HH:mm):")));
        Time end = Time.valueOf(LocalTime.parse(ask("End time (HH:mm):")));
        int slot = Integer.parseInt(ask("Appointment slot length in minutes:"));
        directory.availability(user, id, weekday, start, end, slot);
        display(out, List.of("Availability saved."));
    }
    private void doctorStatus(JTextArea out) throws Exception {
        long id = number("Doctor ID:");
        directory.doctorActive(user, id, confirm("Set this doctor active? Choose No to deactivate."));
        display(out, List.of("Doctor status updated."));
    }

    private JPanel departmentTab() {
        List<String> labels = new ArrayList<>(List.of("List departments"));
        List<Action> actions = new ArrayList<>(List.of(out -> display(out, directory.departments(user))));
        if (user.role() == Role.ADMIN) {
            labels.addAll(List.of("Add department", "Deactivate department"));
            actions.addAll(List.of(out -> display(out, List.of("Department ID: " + directory.addDepartment(user, ask("Name:"), ask("Description:")))), this::deactivateDepartment));
        }
        return dataTab(labels, actions);
    }
    private void deactivateDepartment(JTextArea out) throws Exception {
        long id = number("Department ID:");
        if (confirm("Deactivate department " + id + "? It must have no active doctors.")) {
            directory.deactivateDepartment(user, id); display(out, List.of("Department deactivated."));
        }
    }

    private JPanel appointmentTab() {
        List<String> labels = new ArrayList<>(List.of("List appointments"));
        List<Action> actions = new ArrayList<>(List.of(this::listAppointments));
        if (user.role() == Role.ADMIN || user.role() == Role.RECEPTIONIST) {
            labels.addAll(List.of("Book appointment", "Cancel", "Reschedule"));
            actions.addAll(List.of(this::bookAppointment, this::cancelAppointment, this::rescheduleAppointment));
        }
        if (user.role() == Role.ADMIN || user.role() == Role.DOCTOR) {
            labels.addAll(List.of("Mark completed", "Mark no-show"));
            actions.addAll(List.of(out -> changeAppointmentStatus(out, "COMPLETED"), out -> changeAppointmentStatus(out, "NO_SHOW")));
        }
        return dataTab(labels, actions);
    }
    private void listAppointments(JTextArea out) throws Exception {
        String date = ask("Date YYYY-MM-DD (leave blank for all):");
        display(out, appointments.list(user, date.isBlank() ? null : LocalDate.parse(date)));
    }
    private void bookAppointment(JTextArea out) throws Exception {
        long id = appointments.book(user, number("Patient ID:"), number("Doctor ID:"),
                LocalDateTime.parse(ask("Start date/time (YYYY-MM-DDTHH:mm):")),
                Integer.parseInt(ask("Duration in minutes:")), ask("Reason (optional):"));
        display(out, List.of("Appointment booked. ID: " + id));
    }
    private void cancelAppointment(JTextArea out) throws Exception {
        long id = number("Appointment ID:");
        if (confirm("Cancel appointment " + id + "?")) changeAppointmentStatus(out, id, "CANCELLED");
    }
    private void changeAppointmentStatus(JTextArea out, String status) throws Exception { changeAppointmentStatus(out, number("Appointment ID:"), status); }
    private void changeAppointmentStatus(JTextArea out, long id, String status) throws Exception {
        appointments.status(user, id, status); display(out, List.of("Appointment marked " + status + "."));
    }
    private void rescheduleAppointment(JTextArea out) throws Exception {
        long id = number("Appointment ID:");
        if (confirm("Reschedule appointment " + id + "?")) {
            appointments.reschedule(user, id, number("New doctor ID:"),
                    LocalDateTime.parse(ask("New start (YYYY-MM-DDTHH:mm):")), Integer.parseInt(ask("Duration in minutes:")));
            display(out, List.of("Appointment rescheduled."));
        }
    }

    private JPanel recordsTab() {
        List<String> labels = new ArrayList<>(); List<Action> actions = new ArrayList<>();
        if (user.role() == Role.DOCTOR) { labels.add("Create record"); actions.add(this::createRecord); }
        labels.add("Patient history"); actions.add(out -> display(out, clinical.history(user, number("Patient ID:"))));
        return dataTab(labels, actions);
    }
    private void createRecord(JTextArea out) throws Exception {
        long id = clinical.createRecord(user, number("Completed appointment ID:"), ask("Symptoms:"), ask("Diagnosis:"), ask("Doctor notes:"));
        display(out, List.of("Medical record created. ID: " + id));
    }
    private JPanel prescriptionTab() {
        List<String> labels = new ArrayList<>(List.of("List prescriptions"));
        List<Action> actions = new ArrayList<>(List.of(out -> display(out, clinical.prescriptions(user, number("Patient ID:")))));
        if (user.role() == Role.DOCTOR) { labels.add("Create prescription"); actions.add(this::createPrescription); }
        return dataTab(labels, actions);
    }
    private void createPrescription(JTextArea out) throws Exception {
        long record = number("Medical record ID:"); List<String[]> items = new ArrayList<>();
        do {
            items.add(new String[]{ask("Medicine name:"), ask("Dosage:"), ask("Frequency:"), ask("Duration:"), ask("Instructions:")});
        } while (confirm("Add another medicine?"));
        display(out, List.of("Prescription created. ID: " + clinical.prescribe(user, record, items)));
    }

    private JPanel billingTab() {
        return dataTab(List.of("Create invoice", "View invoice", "Record payment", "Unpaid invoices"), List.of(
                this::createInvoice,
                out -> display(out, List.of(billing.view(user, number("Invoice ID:")).toString())),
                this::recordPayment,
                out -> display(out, billing.unpaid(user))));
    }
    private void createInvoice(JTextArea out) throws Exception {
        long id = billing.create(user, number("Completed appointment ID:"), new BigDecimal(ask("Subtotal:")),
                new BigDecimal(ask("Discount (enter 0 if none):")), ask("Invoice item description:"));
        display(out, List.of("Invoice created. ID: " + id));
    }
    private void recordPayment(JTextArea out) throws Exception {
        long bill = number("Invoice ID:"); BigDecimal amount = new BigDecimal(ask("Payment amount:"));
        PaymentMethod method = PaymentMethod.valueOf(ask("Method: CASH, CARD, UPI, or BANK_TRANSFER:").toUpperCase(Locale.ROOT));
        billing.pay(user, bill, amount, method, ask("Reference (optional):"));
        display(out, List.of("Payment recorded."));
    }

    private JPanel reportTab() {
        List<String> labels = new ArrayList<>(List.of("Appointment summary"));
        List<Action> actions = new ArrayList<>(List.of(out -> {
            LocalDate from = LocalDate.parse(ask("From date (YYYY-MM-DD):"));
            LocalDate to = LocalDate.parse(ask("Through date (YYYY-MM-DD):"));
            display(out, appointments.report(user, from, to));
        }));
        if (user.role() == Role.ADMIN || user.role() == Role.BILLING_STAFF) {
            labels.add("Billing summary"); actions.add(out -> {
                LocalDate from = LocalDate.parse(ask("From date (YYYY-MM-DD):"));
                LocalDate to = LocalDate.parse(ask("Through date (YYYY-MM-DD):"));
                display(out, billing.paymentReport(user, from, to));
            });
        }
        return dataTab(labels, actions);
    }
    private JPanel userTab() {
        return dataTab(List.of("Register user", "Activate / deactivate user"), List.of(this::registerUser, this::toggleUser));
    }
    private void registerUser(JTextArea out) throws Exception {
        String username = ask("New username:"); JPasswordField passwordField = new JPasswordField();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Temporary password (12+ characters):", passwordField},
                "Register user", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        String password = new String(passwordField.getPassword());
        Role role = Role.valueOf(ask("Role: ADMIN, RECEPTIONIST, DOCTOR, or BILLING_STAFF:").toUpperCase(Locale.ROOT));
        Long doctorId = role == Role.DOCTOR ? number("Doctor profile ID:") : null;
        display(out, List.of("User created. ID: " + auth.register(user, username, password, role, doctorId)));
    }
    private void toggleUser(JTextArea out) throws Exception {
        long id = number("User ID:"); auth.setActive(user, id, confirm("Set this user active? Choose No to deactivate."));
        display(out, List.of("User status updated."));
    }
    private JPanel dataTab(List<String> labels, List<Action> actions) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextArea output = new JTextArea(); output.setEditable(false); output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        for (int i = 0; i < labels.size(); i++) {
            Action action = actions.get(i); JButton button = new JButton(labels.get(i));
            button.addActionListener(e -> runAction(output, action)); buttons.add(button);
        }
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(buttons, BorderLayout.NORTH); panel.add(new JScrollPane(output), BorderLayout.CENTER);
        return panel;
    }
    private void runAction(JTextArea output, Action action) {
        try { action.run(output); }
        catch (Exception ex) {
            String message = ex instanceof SQLException ? "Database operation failed. Check the database setup and try again." : ex.getMessage();
            JOptionPane.showMessageDialog(this, message == null ? "The action could not be completed." : message,
                    "Action failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void changePassword() {
        JPasswordField oldPassword = new JPasswordField(); JPasswordField newPassword = new JPasswordField();
        Object[] fields = {"Current password:", oldPassword, "New password (12+ characters):", newPassword};
        if (JOptionPane.showConfirmDialog(this, fields, "Change password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                auth.changePassword(user, new String(oldPassword.getPassword()), new String(newPassword.getPassword()));
                JOptionPane.showMessageDialog(this, "Password changed. Log out and sign in again.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not change password", JOptionPane.ERROR_MESSAGE); }
        }
    }
    private String ask(String prompt) {
        String value = JOptionPane.showInputDialog(this, prompt);
        if (value == null) throw new IllegalArgumentException("Action cancelled.");
        return value.trim();
    }
    private long number(String prompt) { return Long.parseLong(ask(prompt)); }
    private boolean confirm(String prompt) { return JOptionPane.showConfirmDialog(this, prompt, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
    private void display(JTextArea out, List<String> rows) { out.setText(rows.isEmpty() ? "No records found." : String.join("\n", rows)); }
    @FunctionalInterface private interface Action { void run(JTextArea output) throws Exception; }
}
