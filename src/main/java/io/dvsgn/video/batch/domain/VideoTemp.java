package io.dvsgn.video.batch.domain;

public class VideoTemp {
    private Long id;
    private boolean adult;
    private String originalTitle;
    private double popularity;
    private boolean video;

    public VideoTemp(Long id, boolean adult, String originalTitle, double popularity, boolean video) {
        this.id = id;
        this.adult = adult;
        this.originalTitle = originalTitle;
        this.popularity = popularity;
        this.video = video;
    }

    public Long getId() {
        return id;
    }

    public boolean isAdult() {
        return adult;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public double getPopularity() {
        return popularity;
    }

    public boolean isVideo() {
        return video;
    }

    @Override
    public String toString() {
        return "VideoTemp{" +
                "id=" + id +
                ", adult=" + adult +
                ", originalTitle='" + originalTitle + '\'' +
                ", popularity=" + popularity +
                ", video=" + video +
                '}';
    }
}
