package cl.figonzal.lastquakechile.model;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class ChangeLog {

    private String version;
    private String releaseDate;
    private String[] changes;
    private boolean isPreRelease;

    public ChangeLog(String version, String releaseDate, String[] changes, boolean isPreRelease) {
        this.version = version;
        this.releaseDate = releaseDate;
        this.changes = changes;
        this.isPreRelease = isPreRelease;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String[] getChanges() {
        return changes;
    }

    public void setChanges(String[] changes) {
        this.changes = changes;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isPreRelease() {
        return isPreRelease;
    }

    public void setPreRelease(boolean preRelease) {
        isPreRelease = preRelease;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChangeLog{" +
                "version='" + version + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", changes=" + Arrays.toString(changes) +
                ", isPreRelease=" + isPreRelease +
                '}';
    }
}
