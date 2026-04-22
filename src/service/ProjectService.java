package stereovision.service;

import org.opencv.core.Mat;
import stereovision.model.CameraParameters;
import stereovision.model.StereoImagePair;
import stereovision.model.StereoProject;
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
    private int nextProjectId;
    private int nextPairId;

    public ProjectService() {
        this.projects = new ArrayList<>();
        this.projectsById = new HashMap<>();
        this.sortedProjects = new TreeSet<>();
        this.nextProjectId = 1;
        this.nextPairId = 1;
    }

    public StereoProject createProject(String name, String description) {
        StereoProject project = new StereoProject(nextProjectId++, name, description);
        projects.add(project);
        projectsById.put(project.getId(), project);
        sortedProjects.add(project);
        return project;
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
                nextPairId++,
                label,
                leftPath,
                rightPath,
                leftImage.cols(),
                leftImage.rows()
        );
        project.addImagePair(pair);

        if (project.getCameraParameters() == null) {
            project.setCameraParameters(ExifCameraEstimator.estimateOrDefault(
                    leftPath,
                    rightPath,
                    leftImage.cols(),
                    leftImage.rows()
            ));
        }

        return pair;
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
        getProjectById(projectId).setCameraParameters(cameraParameters);
    }
}
