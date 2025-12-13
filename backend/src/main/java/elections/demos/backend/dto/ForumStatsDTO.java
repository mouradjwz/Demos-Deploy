package elections.demos.backend.dto;

public class ForumStatsDTO {
    private long activePosts;
    private long reactions;
    private long likes;
    private long users;

    public ForumStatsDTO() {}

    public ForumStatsDTO(long activePosts, long reactions, long likes, long users) {
        this.activePosts = activePosts;
        this.reactions = reactions;
        this.likes = likes;
        this.users = users;
    }

    public long getActivePosts() { return activePosts; }
    public void setActivePosts(long activePosts) { this.activePosts = activePosts; }

    public long getReactions() { return reactions; }
    public void setReactions(long reactions) { this.reactions = reactions; }

    public long getLikes() { return likes; }
    public void setLikes(long likes) { this.likes = likes; }

    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }
}

