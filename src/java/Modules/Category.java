package Modules;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a movie category in the system.
 * 
 * Each category has a unique ID, name, description, and an optional poster image.
 */
public class Category implements Serializable {

    private String categoryId;
    private String name;
    private String description;
    private transient byte[] posterImage;

    /**
     * Constructs a new Category with a randomly generated ID.
     */
    public Category() {
        this.categoryId = UUID.randomUUID().toString();
    }

    /**
     * Constructs a Category with the specified details.
     *
     * @param categoryId   the unique ID of the category
     * @param name         the name of the category
     * @param description  the description of the category
     */
    public Category(String categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    /**
     * @return the category ID
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category ID.
     *
     * @param categoryId the category ID to set
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * @return the name of the category
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the category description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the poster image as byte array
     */
    public byte[] getPosterImage() {
        return posterImage;
    }

    /**
     * Sets the poster image.
     *
     * @param posterImage the poster image bytes to set
     */
    public void setPosterImage(byte[] posterImage) {
        this.posterImage = posterImage;
    }

    /**
     * Returns a string representation of the category.
     *
     * @return string with categoryId, name, and description
     */
    @Override
    public String toString() {
        return "Category{categoryId='" + categoryId + "', name='" + name + "', description='" + description + "'}";
    }
}
