package stereovision.gui;

import stereovision.model.CameraParameters;
import stereovision.model.ReconstructionSession;
import stereovision.model.StereoImagePair;
import stereovision.model.StereoProject;
import stereovision.service.ProjectService;
import stereovision.service.ReconstructionService;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StereoVisionDemoFrame extends JFrame {
    private static final DateTimeFormatter STATUS_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SESSION_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProjectService projectService;
    private final ReconstructionService reconstructionService;

    private final DefaultListModel<StereoProject> projectListModel = new DefaultListModel<>();
    private final DefaultListModel<StereoImagePair> pairListModel = new DefaultListModel<>();
    private final DefaultListModel<ReconstructionSession> sessionListModel = new DefaultListModel<>();

    private final JList<StereoProject> projectList = new JList<>(projectListModel);
    private final JList<StereoImagePair> pairList = new JList<>(pairListModel);
    private final JList<ReconstructionSession> sessionList = new JList<>(sessionListModel);

    private final JTextField projectNameField = new JTextField(20);
    private final JTextField projectDescriptionField = new JTextField(20);

    private final JTextField pairLabelField = new JTextField(20);
    private final JTextField leftImagePathField = new JTextField(20);
    private final JTextField rightImagePathField = new JTextField(20);

    private final JTextField fxField = new JTextField(12);
    private final JTextField fyField = new JTextField(12);
    private final JTextField cxField = new JTextField(12);
    private final JTextField cyField = new JTextField(12);
    private final JTextField baselineField = new JTextField(12);
    private final JCheckBox approximateCheckBox = new JCheckBox("Approximate");

    private final JTextField outputDirectoryField = new JTextField("output", 20);

    private final JComboBox<String> previewTypeCombo = new JComboBox<>(new String[]{
            "Disparity",
            "Disparity Heatmap",
            "Invalid Disparity Mask",
            "Depth",
            "Depth Heatmap"
    });

    private final JButton createProjectButton = new JButton("Create Project");
    private final JButton refreshProjectsButton = new JButton("Refresh Projects");
    private final JButton addPairButton = new JButton("Add Stereo Pair");
    private final JButton saveCameraButton = new JButton("Save Camera Parameters");
    private final JButton runStereoBMButton = new JButton("Run StereoBM");
    private final JButton runStereoSGBMButton = new JButton("Run StereoSGBM");

    private final JLabel previewLabel = new JLabel("No preview available.", SwingConstants.CENTER);
    private final JTextArea statusArea = new JTextArea(8, 40);

    private BufferedImage currentPreviewImage;
    private String currentPreviewPath;

    public StereoVisionDemoFrame() {
        this.projectService = new ProjectService();
        this.reconstructionService = new ReconstructionService();

        setTitle("Stereo Vision 3D Reconstruction System - Demo GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));
        setSize(1320, 860);
        setLocationRelativeTo(null);

        initializeComponents();
        setContentPane(buildContent());
        bindActions();
        reloadProjects(null, null, null);
        appendStatus("GUI ready.");
    }

    private void initializeComponents() {
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pairList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        projectList.setCellRenderer(new ProjectListRenderer());
        pairList.setCellRenderer(new PairListRenderer());
        sessionList.setCellRenderer(new SessionListRenderer());

        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setPreferredSize(new Dimension(600, 420));

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        previewLabel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updatePreviewIcon();
            }
        });
    }

    private JComponent buildContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildManagementPanel(), buildOutputPanel());
        splitPane.setResizeWeight(0.46);
        splitPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return splitPane;
    }

    private JComponent buildManagementPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildProjectPanel());
        content.add(Box.createVerticalStrut(10));
        content.add(buildPairPanel());
        content.add(Box.createVerticalStrut(10));
        content.add(buildCameraPanel());
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JComponent buildOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(buildReconstructionPanel(), BorderLayout.NORTH);
        panel.add(buildPreviewPanel(), BorderLayout.CENTER);
        panel.add(buildStatusPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildProjectPanel() {
        JPanel panel = createSectionPanel("Projects");
        panel.add(createLabeledRow("Name", projectNameField, null));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("Description", projectDescriptionField, null));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createButtonRow(createProjectButton, refreshProjectsButton));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createListScrollPane(projectList, 180));
        return panel;
    }

    private JPanel buildPairPanel() {
        JPanel panel = createSectionPanel("Stereo Image Pairs");
        panel.add(createLabeledRow("Label", pairLabelField, null));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("Left Image", leftImagePathField, createBrowseFileButton(leftImagePathField)));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("Right Image", rightImagePathField, createBrowseFileButton(rightImagePathField)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createButtonRow(addPairButton));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createListScrollPane(pairList, 180));
        return panel;
    }

    private JPanel buildCameraPanel() {
        JPanel panel = createSectionPanel("Camera Parameters");
        panel.add(createLabeledRow("fx", fxField, null));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("fy", fyField, null));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("cx", cxField, null));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("cy", cyField, null));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createLabeledRow("Baseline", baselineField, null));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createButtonRow(approximateCheckBox, saveCameraButton));
        return panel;
    }

    private JPanel buildReconstructionPanel() {
        JPanel panel = createSectionPanel("Reconstruction");
        panel.add(createLabeledRow("Output Dir", outputDirectoryField, createBrowseDirectoryButton(outputDirectoryField)));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createButtonRow(runStereoBMButton, runStereoSGBMButton));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createListScrollPane(sessionList, 200));
        return panel;
    }

    private JPanel buildPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Preview"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.add(createLabeledRow("Result", previewTypeCombo, null), BorderLayout.NORTH);
        panel.add(previewLabel, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildStatusPanel() {
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Status / Output"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.setPreferredSize(new Dimension(100, 180));
        return scrollPane;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel createLabeledRow(String labelText, JComponent centerComponent, JComponent trailingComponent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(90, 24));
        row.add(label, BorderLayout.WEST);
        row.add(centerComponent, BorderLayout.CENTER);
        if (trailingComponent != null) {
            row.add(trailingComponent, BorderLayout.EAST);
        }
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JPanel createButtonRow(JComponent... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent component : components) {
            row.add(component);
        }
        return row;
    }

    private JScrollPane createListScrollPane(JList<?> list, int preferredHeight) {
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(100, preferredHeight));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
        return scrollPane;
    }

    private JButton createBrowseFileButton(JTextField targetField) {
        JButton button = new JButton("Browse");
        button.addActionListener(event -> chooseImageFile(targetField));
        return button;
    }

    private JButton createBrowseDirectoryButton(JTextField targetField) {
        JButton button = new JButton("Browse");
        button.addActionListener(event -> chooseDirectory(targetField));
        return button;
    }

    private void bindActions() {
        createProjectButton.addActionListener(event -> createProject());
        refreshProjectsButton.addActionListener(event -> {
            Integer selectedProjectId = getSelectedProjectId();
            Integer selectedPairId = getSelectedPairId();
            Integer selectedSessionId = getSelectedSessionId();
            reloadProjects(selectedProjectId, selectedPairId, selectedSessionId);
            appendStatus("Project list refreshed.");
        });
        addPairButton.addActionListener(event -> addStereoPair());
        saveCameraButton.addActionListener(event -> saveCameraParameters());
        runStereoBMButton.addActionListener(event -> runReconstruction("StereoBM"));
        runStereoSGBMButton.addActionListener(event -> runReconstruction("StereoSGBM"));

        projectList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                refreshSelectedProject(null, null);
            }
        });

        sessionList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updatePreviewFromSelection();
            }
        });

        previewTypeCombo.addActionListener(event -> updatePreviewFromSelection());
    }

    private void createProject() {
        String name = projectNameField.getText().trim();
        String description = projectDescriptionField.getText().trim();

        if (name.isEmpty()) {
            showValidationError("Project name is required.");
            return;
        }

        try {
            StereoProject project = projectService.createProject(name, description);
            projectNameField.setText("");
            projectDescriptionField.setText("");
            reloadProjects(project.getId(), null, null);
            appendStatus("Created project #" + project.getId() + " (" + project.getName() + ").");
        } catch (RuntimeException exception) {
            handleUiError("Could not create the project.", exception);
        }
    }

    private void addStereoPair() {
        StereoProject project = getSelectedProject();
        if (project == null) {
            showValidationError("Select a project first.");
            return;
        }

        String label = pairLabelField.getText().trim();
        String leftPath = leftImagePathField.getText().trim();
        String rightPath = rightImagePathField.getText().trim();

        if (label.isEmpty() || leftPath.isEmpty() || rightPath.isEmpty()) {
            showValidationError("Label, left image, and right image are required.");
            return;
        }

        try {
            StereoImagePair pair = projectService.addStereoPair(project.getId(), label, leftPath, rightPath);
            pairLabelField.setText("");
            reloadProjects(project.getId(), pair.getId(), null);
            appendStatus("Added stereo pair #" + pair.getId() + " to project #" + project.getId() + ".");
            CameraParameters parameters = projectService.getCameraParameters(project.getId());
            if (parameters != null) {
                appendStatus("Camera parameters: " + parameters);
            }
        } catch (RuntimeException exception) {
            handleUiError("Could not add the stereo pair.", exception);
        }
    }

    private void saveCameraParameters() {
        StereoProject project = getSelectedProject();
        if (project == null) {
            showValidationError("Select a project first.");
            return;
        }

        try {
            CameraParameters parameters = new CameraParameters(
                    0,
                    parseDoubleField(fxField, "fx"),
                    parseDoubleField(fyField, "fy"),
                    parseDoubleField(cxField, "cx"),
                    parseDoubleField(cyField, "cy"),
                    parseDoubleField(baselineField, "baseline"),
                    approximateCheckBox.isSelected()
            );
            projectService.setCameraParameters(project.getId(), parameters);
            reloadProjects(project.getId(), getSelectedPairId(), getSelectedSessionId());
            appendStatus("Saved camera parameters for project #" + project.getId() + ".");
        } catch (RuntimeException exception) {
            handleUiError("Could not save camera parameters.", exception);
        }
    }

    private void runReconstruction(String algorithmName) {
        StereoProject selectedProject = getSelectedProject();
        StereoImagePair selectedPair = getSelectedPair();

        if (selectedProject == null) {
            showValidationError("Select a project first.");
            return;
        }
        if (selectedPair == null) {
            showValidationError("Select a stereo pair first.");
            return;
        }

        String outputDirectory = outputDirectoryField.getText().trim();
        if (outputDirectory.isEmpty()) {
            showValidationError("Output directory is required.");
            return;
        }

        int projectId = selectedProject.getId();
        int pairId = selectedPair.getId();

        setActionButtonsEnabled(false);
        appendStatus("Running " + algorithmName + " for project #" + projectId + ", pair #" + pairId + "...");

        SwingWorker<ReconstructionSession, Void> worker = new SwingWorker<>() {
            @Override
            protected ReconstructionSession doInBackground() {
                StereoProject project = projectService.getProjectById(projectId);
                StereoImagePair pair = projectService.getPairById(projectId, pairId);
                return reconstructionService.runReconstruction(project, pair, algorithmName, outputDirectory);
            }

            @Override
            protected void done() {
                setActionButtonsEnabled(true);
                try {
                    ReconstructionSession session = get();
                    reloadProjects(projectId, pairId, session.getId());
                    appendStatus("Reconstruction finished. Session #" + session.getId() + " saved.");
                    appendStatus("Disparity: " + session.getDisparityOutputPath());
                    appendStatus("Disparity heatmap: " + session.getDisparityHeatMapOutputPath());
                    appendStatus("Invalid disparity mask: " + session.getInvalidDisparityMaskOutputPath());
                    appendStatus("Depth: " + session.getDepthOutputPath());
                    appendStatus("Depth heatmap: " + session.getDepthHeatMapOutputPath());
                    appendStatus("Points CSV: " + session.getPointsOutputPath());
                    if (session.getStats() != null) {
                        appendStatus("Stats: " + session.getStats());
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    handleUiError("Reconstruction was interrupted.", exception);
                } catch (ExecutionException exception) {
                    handleUiError("Could not run the reconstruction.", unwrap(exception));
                }
            }
        };
        worker.execute();
    }

    private void reloadProjects(Integer projectIdToSelect, Integer pairIdToSelect, Integer sessionIdToSelect) {
        List<StereoProject> projects = projectService.getProjects();
        StereoProject projectToSelect = null;

        projectListModel.clear();
        for (StereoProject project : projects) {
            projectListModel.addElement(project);
            if (projectIdToSelect != null && project.getId() == projectIdToSelect) {
                projectToSelect = project;
            }
        }

        if (projectToSelect != null) {
            projectList.setSelectedValue(projectToSelect, true);
        } else if (!projectListModel.isEmpty() && projectList.getSelectedIndex() < 0) {
            projectList.setSelectedIndex(0);
        }

        if (projectList.getSelectedValue() == null) {
            clearProjectDetails();
            return;
        }

        refreshSelectedProject(pairIdToSelect, sessionIdToSelect);
    }

    private void refreshSelectedProject(Integer pairIdToSelect, Integer sessionIdToSelect) {
        StereoProject project = getSelectedProject();
        if (project == null) {
            clearProjectDetails();
            return;
        }

        loadCameraParameters(project.getCameraParameters());
        reloadPairs(project, pairIdToSelect);
        reloadSessions(project, sessionIdToSelect);
    }

    private void reloadPairs(StereoProject project, Integer pairIdToSelect) {
        StereoImagePair pairToSelect = null;
        pairListModel.clear();
        for (StereoImagePair pair : projectService.getPairsForProject(project.getId())) {
            pairListModel.addElement(pair);
            if (pairIdToSelect != null && pair.getId() == pairIdToSelect) {
                pairToSelect = pair;
            }
        }

        if (pairToSelect != null) {
            pairList.setSelectedValue(pairToSelect, true);
        } else if (!pairListModel.isEmpty() && pairList.getSelectedIndex() < 0) {
            pairList.setSelectedIndex(0);
        } else if (pairListModel.isEmpty()) {
            pairList.clearSelection();
        }
    }

    private void reloadSessions(StereoProject project, Integer sessionIdToSelect) {
        ReconstructionSession sessionToSelect = null;
        sessionListModel.clear();
        List<ReconstructionSession> sessions = projectService.getSessionsForProject(project.getId());
        for (ReconstructionSession session : sessions) {
            sessionListModel.addElement(session);
            if (sessionIdToSelect != null && session.getId() == sessionIdToSelect) {
                sessionToSelect = session;
            }
        }

        if (sessionToSelect != null) {
            sessionList.setSelectedValue(sessionToSelect, true);
        } else if (!sessionListModel.isEmpty()) {
            sessionList.setSelectedIndex(sessionListModel.size() - 1);
        } else {
            sessionList.clearSelection();
            clearPreview("No preview available.");
        }
    }

    private void loadCameraParameters(CameraParameters parameters) {
        if (parameters == null) {
            fxField.setText("");
            fyField.setText("");
            cxField.setText("");
            cyField.setText("");
            baselineField.setText("");
            approximateCheckBox.setSelected(false);
            return;
        }

        fxField.setText(Double.toString(parameters.getFx()));
        fyField.setText(Double.toString(parameters.getFy()));
        cxField.setText(Double.toString(parameters.getCx()));
        cyField.setText(Double.toString(parameters.getCy()));
        baselineField.setText(Double.toString(parameters.getBaseline()));
        approximateCheckBox.setSelected(parameters.isApproximate());
    }

    private void clearProjectDetails() {
        pairListModel.clear();
        sessionListModel.clear();
        loadCameraParameters(null);
        clearPreview("No preview available.");
    }

    private void updatePreviewFromSelection() {
        ReconstructionSession session = getSelectedSession();
        if (session == null) {
            clearPreview("No preview available.");
            return;
        }

        String previewPath = getPreviewPath(session);
        if (previewPath == null || previewPath.isBlank()) {
            clearPreview("Selected result has no preview image.");
            return;
        }

        if (previewPath.equals(currentPreviewPath) && currentPreviewImage != null) {
            updatePreviewIcon();
            return;
        }

        File imageFile = new File(previewPath);
        if (!imageFile.exists()) {
            clearPreview("Preview file not found.");
            return;
        }

        try {
            currentPreviewImage = ImageIO.read(imageFile);
            currentPreviewPath = previewPath;
            if (currentPreviewImage == null) {
                clearPreview("Could not read preview image.");
                return;
            }
            updatePreviewIcon();
        } catch (IOException exception) {
            clearPreview("Could not load preview image.");
            appendStatus("Preview error: " + exception.getMessage());
        }
    }

    private String getPreviewPath(ReconstructionSession session) {
        String selectedPreviewType = (String) previewTypeCombo.getSelectedItem();
        if ("Disparity".equals(selectedPreviewType)) {
            return session.getDisparityOutputPath();
        }
        if ("Disparity Heatmap".equals(selectedPreviewType)) {
            return session.getDisparityHeatMapOutputPath();
        }
        if ("Invalid Disparity Mask".equals(selectedPreviewType)) {
            return session.getInvalidDisparityMaskOutputPath();
        }
        if ("Depth".equals(selectedPreviewType)) {
            return session.getDepthOutputPath();
        }
        return session.getDepthHeatMapOutputPath();
    }

    private void updatePreviewIcon() {
        if (currentPreviewImage == null) {
            return;
        }

        int availableWidth = Math.max(previewLabel.getWidth(), 600) - 20;
        int availableHeight = Math.max(previewLabel.getHeight(), 420) - 20;
        Dimension scaledSize = scaleToFit(
                currentPreviewImage.getWidth(),
                currentPreviewImage.getHeight(),
                availableWidth,
                availableHeight
        );

        Image scaledImage = currentPreviewImage.getScaledInstance(
                scaledSize.width,
                scaledSize.height,
                Image.SCALE_SMOOTH
        );
        previewLabel.setIcon(new ImageIcon(scaledImage));
        previewLabel.setText("");
    }

    private Dimension scaleToFit(int imageWidth, int imageHeight, int maxWidth, int maxHeight) {
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Dimension(Math.max(maxWidth, 1), Math.max(maxHeight, 1));
        }

        double widthRatio = (double) maxWidth / imageWidth;
        double heightRatio = (double) maxHeight / imageHeight;
        double scale = Math.min(widthRatio, heightRatio);
        if (scale > 1.0) {
            scale = 1.0;
        }

        int width = Math.max(1, (int) Math.round(imageWidth * scale));
        int height = Math.max(1, (int) Math.round(imageHeight * scale));
        return new Dimension(width, height);
    }

    private void clearPreview(String message) {
        currentPreviewImage = null;
        currentPreviewPath = null;
        previewLabel.setIcon(null);
        previewLabel.setText(message);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        createProjectButton.setEnabled(enabled);
        refreshProjectsButton.setEnabled(enabled);
        addPairButton.setEnabled(enabled);
        saveCameraButton.setEnabled(enabled);
        runStereoBMButton.setEnabled(enabled);
        runStereoSGBMButton.setEnabled(enabled);
    }

    private void chooseImageFile(JTextField targetField) {
        JFileChooser chooser = new JFileChooser(resolveChooserDirectory(targetField.getText()));
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void chooseDirectory(JTextField targetField) {
        JFileChooser chooser = new JFileChooser(resolveChooserDirectory(targetField.getText()));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            targetField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private File resolveChooserDirectory(String currentValue) {
        if (currentValue != null && !currentValue.isBlank()) {
            File file = new File(currentValue.trim());
            if (file.isDirectory()) {
                return file;
            }
            File parent = file.getParentFile();
            if (parent != null && parent.exists()) {
                return parent;
            }
        }
        return new File(System.getProperty("user.dir"));
    }

    private double parseDoubleField(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid value for " + fieldName + ".");
        }
    }

    private StereoProject getSelectedProject() {
        return projectList.getSelectedValue();
    }

    private StereoImagePair getSelectedPair() {
        return pairList.getSelectedValue();
    }

    private ReconstructionSession getSelectedSession() {
        return sessionList.getSelectedValue();
    }

    private Integer getSelectedProjectId() {
        StereoProject project = getSelectedProject();
        return project != null ? project.getId() : null;
    }

    private Integer getSelectedPairId() {
        StereoImagePair pair = getSelectedPair();
        return pair != null ? pair.getId() : null;
    }

    private Integer getSelectedSessionId() {
        ReconstructionSession session = getSelectedSession();
        return session != null ? session.getId() : null;
    }

    private void appendStatus(String message) {
        statusArea.append("[" + LocalTime.now().format(STATUS_TIME_FORMAT) + "] " + message + System.lineSeparator());
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void handleUiError(String title, Throwable error) {
        String message = error.getMessage() != null ? error.getMessage() : error.toString();
        appendStatus("Error: " + message);
        JOptionPane.showMessageDialog(this, title + "\n" + message, "Stereo Vision GUI", JOptionPane.ERROR_MESSAGE);
    }

    private Throwable unwrap(Throwable throwable) {
        return throwable.getCause() != null ? throwable.getCause() : throwable;
    }

    private static class ProjectListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof StereoProject project) {
                label.setText("#" + project.getId() + " - " + project.getName());
            }
            return label;
        }
    }

    private static class PairListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof StereoImagePair pair) {
                label.setText("#" + pair.getId() + " - " + pair.getLabel() + " (" + pair.getImageWidth() + "x" + pair.getImageHeight() + ")");
            }
            return label;
        }
    }

    private static class SessionListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ReconstructionSession session) {
                String createdAt = session.getCreatedAt() != null
                        ? session.getCreatedAt().format(SESSION_TIME_FORMAT)
                        : "n/a";
                label.setText("#" + session.getId() + " - " + session.getAlgorithmName() + " - pair " + session.getPairId() + " - " + createdAt);
            }
            return label;
        }
    }
}
