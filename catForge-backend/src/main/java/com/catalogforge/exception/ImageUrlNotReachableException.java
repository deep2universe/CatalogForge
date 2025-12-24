package com.catalogforge.exception;

/**
 * Exception thrown when an image URL is not reachable.
 */
public class ImageUrlNotReachableException extends CatalogForgeException {

    private final String imageUrl;

    public ImageUrlNotReachableException(String imageUrl) {
        super(String.format("Image URL not reachable: %s", imageUrl));
        this.imageUrl = imageUrl;
    }

    public ImageUrlNotReachableException(String imageUrl, Throwable cause) {
        super(String.format("Image URL not reachable: %s", imageUrl), cause);
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
