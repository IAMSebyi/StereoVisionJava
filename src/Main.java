package stereovision;

import stereovision.algorithm.StereoMatcherAlgorithm;
import stereovision.config.OpenCVLoader;
import stereovision.gui.StereoVisionDemoFrame;
import stereovision.model.CameraParameters;
import stereovision.model.ReconstructionSession;
import stereovision.model.StereoImagePair;
import stereovision.model.StereoProject;
import stereovision.service.AuditService;
import stereovision.service.ProjectService;
import stereovision.service.ReconstructionService;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final ProjectService projectService = new ProjectService();
    private static final ReconstructionService reconstructionService = new ReconstructionService();
    private static final AuditService auditService = AuditService.getInstance();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length == 0 || isGuiFlag(args)) {
            launchGui();
            return;
        }

        if (!loadOpenCv(false)) {
            return;
        }

        if (isInteractiveCliFlag(args)) {
            runInteractiveConsole();
            return;
        }

        runFromCommandLine(args);
    }

    private static boolean isGuiFlag(String[] args) {
        return args.length == 1 && "--gui".equalsIgnoreCase(args[0]);
    }

    private static boolean isInteractiveCliFlag(String[] args) {
        return args.length == 1 && "--cli".equalsIgnoreCase(args[0]);
    }

    private static void launchGui() {
        applySystemLookAndFeel();
        if (!loadOpenCv(true)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                StereoVisionDemoFrame frame = new StereoVisionDemoFrame();
                frame.setVisible(true);
            } catch (RuntimeException exception) {
                showGuiStartupError("Could not start the GUI.", exception);
            }
        });
    }

    private static boolean loadOpenCv(boolean showDialog) {
        try {
            OpenCVLoader.load();
            return true;
        } catch (UnsatisfiedLinkError error) {
            System.err.println("Could not load the OpenCV native library.");
            System.err.println("Check java.library.path and make sure OpenCV Java is installed correctly.");
            System.err.println(error.getMessage());
            if (showDialog) {
                JOptionPane.showMessageDialog(
                        null,
                        "Could not load the OpenCV native library.\nCheck java.library.path and your OpenCV installation.\n" + error.getMessage(),
                        "Stereo Vision GUI",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            return false;
        }
    }

    private static void applySystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private static void showGuiStartupError(String message, Throwable error) {
        System.err.println(message);
        error.printStackTrace(System.err);
        JOptionPane.showMessageDialog(
                null,
                message + "\n" + error.getMessage(),
                "Stereo Vision GUI",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static void runInteractiveConsole() {
        boolean running = true;
        while (running) {
            printMenu();
            int option = readInt("Choose an option: ");

            try {
                switch (option) {
                    case 1 -> createProject();
                    case 2 -> listProjects();
                    case 3 -> updateProject();
                    case 4 -> deleteProject();
                    case 5 -> addStereoPair();
                    case 6 -> updateStereoPair();
                    case 7 -> deleteStereoPair();
                    case 8 -> configureCameraParameters();
                    case 9 -> deleteCameraParameters();
                    case 10 -> listPairs();
                    case 11 -> showAlgorithms();
                    case 12 -> runReconstruction("StereoBM");
                    case 13 -> runReconstruction("StereoSGBM");
                    case 14 -> showProjectSessions();
                    case 15 -> deleteReconstructionSession();
                    case 16 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (RuntimeException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }

        System.out.println("Program closed.");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== Stereo Vision 3D Reconstruction System ===");
        System.out.println("1. Create stereo project");
        System.out.println("2. List projects");
        System.out.println("3. Update project");
        System.out.println("4. Delete project");
        System.out.println("5. Add stereo pair");
        System.out.println("6. Update stereo pair");
        System.out.println("7. Delete stereo pair");
        System.out.println("8. Configure camera parameters");
        System.out.println("9. Delete camera parameters");
        System.out.println("10. List stereo pairs for a project");
        System.out.println("11. Show available algorithms");
        System.out.println("12. Run reconstruction with StereoBM");
        System.out.println("13. Run reconstruction with StereoSGBM");
        System.out.println("14. Show project sessions");
        System.out.println("15. Delete reconstruction session");
        System.out.println("16. Exit");
    }

    private static void createProject() {
        System.out.print("Project name: ");
        String name = scanner.nextLine();
        System.out.print("Project description: ");
        String description = scanner.nextLine();

        StereoProject project = projectService.createProject(name, description);
        System.out.println("Created project: " + project);
    }

    private static void listProjects() {
        List<StereoProject> projects = projectService.getProjects();
        auditService.logAction("list_projects");
        if (projects.isEmpty()) {
            System.out.println("No projects exist.");
            return;
        }

        System.out.println("Projects (insertion order):");
        for (StereoProject project : projects) {
            System.out.println(project);
        }

        System.out.println("Projects (sorted order):");
        for (StereoProject project : projectService.getSortedProjects()) {
            System.out.println(project);
        }
    }

    private static void updateProject() {
        int projectId = readInt("Project ID: ");
        System.out.print("New project name: ");
        String name = scanner.nextLine();
        System.out.print("New project description: ");
        String description = scanner.nextLine();
        StereoProject project = projectService.updateProject(projectId, name, description);
        System.out.println("Updated project: " + project);
    }

    private static void deleteProject() {
        int projectId = readInt("Project ID: ");
        projectService.deleteProject(projectId);
        System.out.println("Project deleted.");
    }

    private static void addStereoPair() {
        int projectId = readInt("Project ID: ");
        System.out.print("Pair label: ");
        String label = scanner.nextLine();
        System.out.print("Left image path: ");
        String leftPath = scanner.nextLine();
        System.out.print("Right image path: ");
        String rightPath = scanner.nextLine();

        StereoImagePair pair = projectService.addStereoPair(projectId, label, leftPath, rightPath);
        System.out.println("Added pair: " + pair);
        System.out.println("Camera parameters: " + projectService.getProjectById(projectId).getCameraParameters());
    }

    private static void updateStereoPair() {
        int projectId = readInt("Project ID: ");
        int pairId = readInt("Pair ID: ");
        System.out.print("New pair label: ");
        String label = scanner.nextLine();
        System.out.print("New left image path: ");
        String leftPath = scanner.nextLine();
        System.out.print("New right image path: ");
        String rightPath = scanner.nextLine();

        StereoImagePair pair = projectService.updateStereoPair(projectId, pairId, label, leftPath, rightPath);
        System.out.println("Updated pair: " + pair);
    }

    private static void deleteStereoPair() {
        int projectId = readInt("Project ID: ");
        int pairId = readInt("Pair ID: ");
        projectService.deleteStereoPair(projectId, pairId);
        System.out.println("Stereo pair deleted.");
    }

    private static void configureCameraParameters() {
        int projectId = readInt("Project ID: ");
        double fx = readDouble("fx: ");
        double fy = readDouble("fy: ");
        double cx = readDouble("cx: ");
        double cy = readDouble("cy: ");
        double baseline = readDouble("baseline: ");

        CameraParameters cameraParameters = new CameraParameters(0, fx, fy, cx, cy, baseline, false);
        projectService.setCameraParameters(projectId, cameraParameters);
        System.out.println("Camera parameters were updated.");
    }

    private static void deleteCameraParameters() {
        int projectId = readInt("Project ID: ");
        projectService.deleteCameraParameters(projectId);
        System.out.println("Camera parameters deleted.");
    }

    private static void listPairs() {
        int projectId = readInt("Project ID: ");
        List<StereoImagePair> pairs = projectService.getPairsForProject(projectId);
        auditService.logAction("list_stereo_pairs");
        if (pairs.isEmpty()) {
            System.out.println("The project has no stereo pairs.");
            return;
        }

        for (StereoImagePair pair : pairs) {
            System.out.println(pair);
        }
    }

    private static void showAlgorithms() {
        auditService.logAction("show_available_algorithms");
        for (StereoMatcherAlgorithm algorithm : reconstructionService.getAvailableAlgorithms()) {
            System.out.println(algorithm);
        }
    }

    private static void runReconstruction(String algorithmName) {
        int projectId = readInt("Project ID: ");
        int pairId = readInt("Pair ID: ");
        System.out.print("Output directory (example: output): ");
        String outputDir = scanner.nextLine();

        StereoProject project = projectService.getProjectById(projectId);
        StereoImagePair pair = projectService.getPairById(projectId, pairId);
        ReconstructionSession session = reconstructionService.runReconstruction(project, pair, algorithmName, outputDir);

        System.out.println("Reconstruction complete.");
        System.out.println(session);
    }

    private static void showProjectSessions() {
        int projectId = readInt("Project ID: ");
        List<ReconstructionSession> sessions = projectService.getSessionsForProject(projectId);
        auditService.logAction("show_project_sessions");
        if (sessions.isEmpty()) {
            System.out.println("No reconstruction sessions exist for this project.");
            return;
        }

        for (ReconstructionSession session : sessions) {
            System.out.println(session);
        }
    }

    private static void deleteReconstructionSession() {
        int projectId = readInt("Project ID: ");
        int sessionId = readInt("Session ID: ");
        StereoProject project = projectService.getProjectById(projectId);
        reconstructionService.deleteSession(project, sessionId);
        System.out.println("Reconstruction session deleted.");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine();
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid real number.");
            }
        }
    }

    private static void runFromCommandLine(String[] args) {
        if (args.length < 2 || args.length > 4) {
            printCommandLineUsage();
            return;
        }

        String leftImagePath = args[0];
        String rightImagePath = args[1];
        String outputDirectory = args.length >= 3 ? args[2] : "output";
        String algorithmName = args.length >= 4 ? args[3] : "StereoSGBM";

        try {
            StereoProject project = projectService.createProject("command-line-run", "Stereo pair reconstruction from CLI");
            StereoImagePair pair = projectService.addStereoPair(project.getId(), "input-pair", leftImagePath, rightImagePath);
            ReconstructionSession session = reconstructionService.runReconstruction(project, pair, algorithmName, outputDirectory);
            auditService.logAction("command_line_run");

            System.out.println("Reconstruction complete.");
            System.out.println("Camera parameters: " + project.getCameraParameters());
            System.out.println("Disparity map: " + session.getDisparityOutputPath());
            System.out.println("Disparity heatmap: " + session.getDisparityHeatMapOutputPath());
            System.out.println("Invalid disparity mask: " + session.getInvalidDisparityMaskOutputPath());
            System.out.println("Depth map: " + session.getDepthOutputPath());
            System.out.println("Depth heatmap: " + session.getDepthHeatMapOutputPath());
            System.out.println("Point cloud CSV: " + session.getPointsOutputPath());
            System.out.println("Stats: " + session.getStats());
        } catch (RuntimeException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void printCommandLineUsage() {
        System.out.println("Usage:");
        System.out.println("  java \"-Djava.library.path=<opencv-native-dir>\" -cp \"<classes>;<opencv-jar>;<sqlite-jdbc-jar>\" stereovision.Main");
        System.out.println("  java \"-Djava.library.path=<opencv-native-dir>\" -cp \"<classes>;<opencv-jar>;<sqlite-jdbc-jar>\" stereovision.Main --cli");
        System.out.println("  java \"-Djava.library.path=<opencv-native-dir>\" -cp \"<classes>;<opencv-jar>;<sqlite-jdbc-jar>\" stereovision.Main <left-image> <right-image> [output-dir] [StereoBM|StereoSGBM]");
    }
}
