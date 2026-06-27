package com.example.fooddelivery.ui.reviews.model;

/** Model cho một đánh giá */
public class ReviewItem {
    private String avatarInitial;
    private int avatarColor;
    private String reviewerName;
    private int stars;
    private String timeAgo;
    private String reviewText;
    private String[] tags;
    private boolean hasPhoto;

    public ReviewItem(String avatarInitial, int avatarColor, String reviewerName,
                      int stars, String timeAgo, String reviewText,
                      String[] tags, boolean hasPhoto) {
        this.avatarInitial = avatarInitial;
        this.avatarColor   = avatarColor;
        this.reviewerName  = reviewerName;
        this.stars         = stars;
        this.timeAgo       = timeAgo;
        this.reviewText    = reviewText;
        this.tags          = tags;
        this.hasPhoto      = hasPhoto;
    }

    public String getAvatarInitial() { return avatarInitial; }
    public int    getAvatarColor()   { return avatarColor; }
    public String getReviewerName()  { return reviewerName; }
    public int    getStars()         { return stars; }
    public String getTimeAgo()       { return timeAgo; }
    public String getReviewText()    { return reviewText; }
    public String[] getTags()        { return tags; }
    public boolean isHasPhoto()      { return hasPhoto; }
}
