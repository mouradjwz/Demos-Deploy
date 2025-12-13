package elections.demos.backend.dto;

public class StatDto {
    private String title;
    private long value;
    private String subtitle;
    private boolean showPlus;


    public StatDto(String title, long value, String subtitle, boolean showPlus) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.showPlus = showPlus;
    }

    public String getTitle() {
        return title;
    }
    public long getValue() {
        return value;
    }
    public String getSubtitle() {
        return subtitle; }

    public boolean isShowPlus() {
        return showPlus;
    }


}