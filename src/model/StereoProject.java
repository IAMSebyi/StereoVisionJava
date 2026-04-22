package stereovision.model;

import java.util.ArrayList;
import java.util.List;

public class StereoProject extends BaseEntity implements Comparable<StereoProject> {
    private String name;
    private String description;
    private CameraParameters cameraParameters;
    private List<StereoImagePair> imagePairs;
    private List<ReconstructionSession> sessions;

    public StereoProject() {
        this.imagePairs = new ArrayList<>();
        this.sessions = new ArrayList<>();
    }

    public StereoProject(int id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
        this.imagePairs = new ArrayList<>();
        this.sessions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CameraParameters getCameraParameters() {
        return cameraParameters;
    }

    public void setCameraParameters(CameraParameters cameraParameters) {
        this.cameraParameters = cameraParameters;
    }

    public List<StereoImagePair> getImagePairs() {
        return imagePairs;
    }

    public void setImagePairs(List<StereoImagePair> imagePairs) {
        this.imagePairs = imagePairs;
    }

    public List<ReconstructionSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<ReconstructionSession> sessions) {
        this.sessions = sessions;
    }

    public void addImagePair(StereoImagePair pair) {
        this.imagePairs.add(pair);
    }

    public void addSession(ReconstructionSession session) {
        this.sessions.add(session);
    }

    @Override
    public int compareTo(StereoProject other) {
        int byName = this.name.compareToIgnoreCase(other.name);
        if (byName != 0) {
            return byName;
        }
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return "StereoProject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cameraParameters=" + cameraParameters +
                ", imagePairs=" + imagePairs.size() +
                ", sessions=" + sessions.size() +
                '}';
    }
}
