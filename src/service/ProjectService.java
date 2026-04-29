package stereovision.service;

import org.opencv.core.Mat;
import stereovision.config.DatabaseInitializer;
import stereovision.model.CameraParameters;
import stereovision.model.ReconstructionSession;
import stereovision.model.StereoImagePair;
import stereovision.model.StereoProject;
import stereovision.repository.CameraParametersRepository;
import stereovision.repository.ReconstructionSessionRepository;
import stereovision.repository.StereoImagePairRepository;
import stereovision.repository.StereoProjectRepository;
import stereovision.util.ExifCameraEstimator;
import stereovision.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ProjectService {
    private final List<StereoProject> projects;
    private final Map<Integer, StereoProject> projectsById;
    private final SortedSet<StereoProject> sortedProjects;
    private final StereoProjectRepository projectRepository;
    private final StereoImagePairRepository pairRepository;
    private final CameraParametersRepository cameraParametersRepository;
    private final ReconstructionSessionRepository sessionRepository;
    private final AuditService auditService;

    public ProjectService() {
        DatabaseInitializer.initialize();
        this.projects = new ArrayList<>();
        this.projectsById = new HashMap<>();
        this.sortedProjects = new TreeSet<>();
        this.projectRepository = new StereoProjectRepository();
        this.pairRepository = new StereoImagePairRepository();
        this.cameraParametersRepository = new CameraParametersRepository();
        this.sessionRepository = new ReconstructionSessionRepository();
        this.auditService = AuditService.getInstance();
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        projects.clear();
        projectsById.clear();
        sortedProjects.clear();

        for (StereoProject project : projectRepository.readAll()) {
            cameraParametersRepository.readByProjectId(project.getId()).ifPresent(project::setCameraParameters);
            project.setImagePairs(new ArrayList<>(pairRepository.readByProjectId(project.getId())));
            project.setSessions(new ArrayList<>(sessionRepository.readByProjectId(project.getId())));
            projects.add(project);
            projectsById.put(project.getId(), project);
            sortedProjects.add(project);
        }
    }

    public StereoProject createProject(String name, String description) {
        StereoProject project = new StereoProject(0, name, description);
        projectRepository.create(project);
        projects.add(project);
        projectsById.put(project.getId(), project);
        sortedProjects.add(project);
        auditService.logAction("create_project");
        return project;
    }

    public StereoProject updateProject(int projectId, String name, String description) {
        StereoProject project = getProjectById(projectId);
        sortedProjects.remove(project);
        project.setName(name);
        project.setDescription(description);
        projectRepository.update(project);
        sortedProjects.add(project);
        auditService.logAction("update_project");
        return project;
    }

    public void deleteProject(int projectId) {
        StereoProject project = getProjectById(projectId);
        projectRepository.delete(projectId);
        projects.remove(project);
        projectsById.remove(projectId);
        sortedProjects.remove(project);
        auditService.logAction("delete_project");
    }

    public List<StereoProject> getProjects() {
        return new ArrayList<>(projects);
    }

    public SortedSet<StereoProject> getSortedProjects() {
        return new TreeSet<>(sortedProjects);
    }

    public StereoProject getProjectById(int projectId) {
        StereoProject project = projectsById.get(projectId);
        if (project == null) {
            throw new IllegalArgumentException("No project exists with id " + projectId + ".");
        }
        return project;
    }

    public StereoImagePair addStereoPair(int projectId, String label, String leftPath, String rightPath) {
        StereoProject project = getProjectById(projectId);

        Mat leftImage = ImageUtils.loadColor(leftPath);
        Mat rightImage = ImageUtils.loadColor(rightPath);
        ImageUtils.validateLoaded(leftImage, leftPath);
        ImageUtils.validateLoaded(rightImage, rightPath);
        ImageUtils.validateSameSize(leftImage, rightImage);

        StereoImagePair pair = new StereoImagePair(
                0,
                label,
                leftPath,
                rightPath,
                leftImage.cols(),
                leftImage.rows()
        );
        pairRepository.create(projectId, pair);
        project.addImagePair(pair);

        if (project.getCameraParameters() == null) {
            CameraParameters parameters = ExifCameraEstimator.estimateOrDefault(
                    leftPath,
                    rightPath,
                    leftImage.cols(),
                    leftImage.rows()
            );
            cameraParametersRepository.upsert(projectId, parameters);
            project.setCameraParameters(parameters);
        }

        auditService.logAction("add_stereo_pair");
        return pair;
    }

    public StereoImagePair updateStereoPair(int projectId, int pairId, String label, String leftPath, String rightPath) {
        StereoImagePair pair = getPairById(projectId, pairId);

        Mat leftImage = ImageUtils.loadColor(leftPath);
        Mat rightImage = ImageUtils.loadColor(rightPath);
        ImageUtils.validateLoaded(leftImage, leftPath);
        ImageUtils.validateLoaded(rightImage, rightPath);
        ImageUtils.validateSameSize(leftImage, rightImage);

        pair.setLabel(label);
        pair.setLeftImagePath(leftPath);
        pair.setRightImagePath(rightPath);
        pair.setImageWidth(leftImage.cols());
        pair.setImageHeight(leftImage.rows());
        pairRepository.update(pair);
        auditService.logAction("update_stereo_pair");
        return pair;
    }

    public void deleteStereoPair(int projectId, int pairId) {
        StereoProject project = getProjectById(projectId);
        StereoImagePair pair = getPairById(projectId, pairId);
        pairRepository.delete(pairId);
        project.getImagePairs().removeIf(existingPair -> existingPair.getId() == pairId);
        project.getSessions().removeIf(session -> session.getPairId() == pairId);
        auditService.logAction("delete_stereo_pair");
    }

    public List<StereoImagePair> getPairsForProject(int projectId) {
        return new ArrayList<>(getProjectById(projectId).getImagePairs());
    }

    public StereoImagePair getPairById(int projectId, int pairId) {
        StereoProject project = getProjectById(projectId);
        return project.getImagePairs()
                .stream()
                .filter(pair -> pair.getId() == pairId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No stereo pair exists with id " + pairId + " in project " + projectId + "."));
    }

    public void setCameraParameters(int projectId, CameraParameters cameraParameters) {
        StereoProject project = getProjectById(projectId);
        cameraParametersRepository.upsert(projectId, cameraParameters);
        project.setCameraParameters(cameraParameters);
        auditService.logAction("configure_camera_parameters");
    }

    public CameraParameters getCameraParameters(int projectId) {
        return getProjectById(projectId).getCameraParameters();
    }

    public void deleteCameraParameters(int projectId) {
        StereoProject project = getProjectById(projectId);
        cameraParametersRepository.deleteByProjectId(projectId);
        project.setCameraParameters(null);
        auditService.logAction("delete_camera_parameters");
    }

    public List<ReconstructionSession> getSessionsForProject(int projectId) {
        return new ArrayList<>(getProjectById(projectId).getSessions());
    }
}
