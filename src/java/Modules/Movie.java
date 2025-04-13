package Modules;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a movie in the system.
 *
 * Each movie has an ID, title, description, quantity information,
 * release date, category names, and an optional poster image.
 */
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private String movieId;
    private String title;
    private String description;
    private Integer copiesAvailable;
    private Integer quantity;
    private Date dateRelease;
    private String categoryNames;
    private transient byte[] posterImage;

    /**
     * Default constructor.
     * Creates an empty Movie object.
     */
    public Movie() {}

    /**
     * Constructs a Movie with full initialization.
     *
     * @param movieId         the unique identifier of the movie
     * @param title           the title of the movie
     * @param description     the movie description
     * @param copiesAvailable number of available copies
     * @param quantity        total number of copies
     * @param dateRelease     the movie's release date
     * @param categoryNames   string representing the categories
     */
    public Movie(String movieId, String title, String description, int copiesAvailable, int quantity, Date dateRelease, String categoryNames) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.copiesAvailable = copiesAvailable;
        this.quantity = quantity;
        this.dateRelease = dateRelease;
        this.categoryNames = categoryNames;
    }

    /**
     * @return the movie ID
     */
    public String getMovieId() {
        return movieId;
    }

    /**
     * Sets the movie ID.
     * @param movieId the unique movie identifier
     */
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    /**
     * @return the movie title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the movie title.
     * @param title the movie title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the movie description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the movie description.
     * @param description a textual description of the movie
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the number of available copies (0 if null)
     */
    public Integer getCopiesAvailable() {
        if (copiesAvailable == null) {
            return 0;
        }
        return copiesAvailable;
    }

    /**
     * Sets the number of available copies.
     * @param copiesAvailable the number of copies available
     */
    public void setCopiesAvailable(Integer copiesAvailable) {
        this.copiesAvailable = copiesAvailable;
    }

    /**
     * @return the total number of copies (0 if null)
     */
    public Integer getQuantity() {
        if (quantity == null) {
            return 0;
        }
        return quantity;
    }

    /**
     * Sets the total quantity and updates available copies accordingly.
     * @param quantity the number of total copies
     */
    public void setQuantity(Integer quantity) {
        if (quantity == null) {
            this.quantity = 0;
        } else {
            this.quantity = quantity;
        }
        
    }

    /**
     * @return the release date of the movie
     */
    public Date getDateRelease() {
        return dateRelease;
    }

    /**
     * Sets the release date of the movie.
     * @param dateRelease the release date to assign
     */
    public void setDateRelease(Date dateRelease) {
        this.dateRelease = dateRelease;
    }

    /**
     * @return comma-separated category names
     */
    public String getCategoryNames() {
        return categoryNames;
    }

    /**
     * Sets the category names as a comma-separated string.
     * @param categoryNames the categories assigned to the movie
     */
    public void setCategoryNames(String categoryNames) {
        this.categoryNames = categoryNames;
    }

    /**
     * @return the poster image as a byte array
     */
    public byte[] getPosterImage() {
        return posterImage;
    }

    /**
     * Sets the poster image for the movie.
     * @param posterImage the byte array representing the image
     */
    public void setPosterImage(byte[] posterImage) {
        this.posterImage = posterImage;
    }
}
