package elections.demos.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stats")
public class Stat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(name = "stat_value")
    private Integer value;
    private String subtitle;
    @Column(name = "show_plus")
    private boolean showPlus;


    public Stat() {}

    public Stat(String title, int value, String subtitle, boolean showPlus) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.showPlus = showPlus;
    }

    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public int getValue() {
        return value;
    }
    public String getSubtitle() {
        return subtitle;
    }
    public boolean isShowPlus() {
        return showPlus;
    }
}