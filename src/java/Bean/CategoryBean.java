package Bean;

import Utils.PageController;
import Modules.Category;
import DAO.CategoryDAO;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.util.Map;
import jakarta.inject.Inject;
import java.util.Base64;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import jakarta.enterprise.context.SessionScoped;
import config.AppConfig;

/**
 * Managed Bean responsible for managing movie categories in the system. Allows
 * adding, updating, deleting, and retrieving categories, including image upload
 * and display. Session-scoped and used for interacting with JSF pages related
 * to category management.
 */
@Named
@SessionScoped
public class CategoryBean implements Serializable {

    private List<Category> categories;// List of all categories loaded from the database
    private Category newCategory = new Category();// Category object used for creating a new category
    private Part uploadedFile;// Uploaded image file associated with the category (poster)
    private Category selectedCategory;// Category selected for editing
    @Inject
    private PageController pageController;// Controller used for navigating between JSF pages

    /**
     * Initializes the bean and loads categories from the database.
     */
    public CategoryBean() {
        loadCategories();
    }

// === Getters and Setters ===
    public List<Category> getCategories() {
        return categories;
    }

    public Category getNewCategory() {
        return newCategory;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    /**
     * Loads all categories from the database and stores them in the local list.
     * Called internally during bean initialization and after category updates.
     *
     * This method is private because it is used only within the bean and not
     * exposed to JSF views.
     */
    private void loadCategories() {
        categories = CategoryDAO.getAllCategories();
        System.out.println("Categories Loaded: " + categories.size());
    }

    /**
     * Adds a new category to the system.
     *
     * This method performs the following: - Validates that name and description
     * are provided. - Checks for duplicate category names. - Validates image
     * size if a poster file is uploaded. - Reads and stores the image as a byte
     * array. - Generates a new UUID for the category. - Adds the category to
     * the database via DAO. - Updates the UI with success or error messages. -
     * Resets the form fields after successful addition.
     */
    public void addCategory() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (newCategory.getName() == null || newCategory.getName().trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Category name is required!"));
            return;
        }
        if (newCategory.getDescription() == null || newCategory.getDescription().trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Category description is required!"));
            return;
        }
        if (CategoryDAO.categoryExists(newCategory.getName())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Category already exists!"));
            return;
        }
        newCategory.setCategoryId(UUID.randomUUID().toString());
        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            if (uploadedFile.getSize() > AppConfig.getMaxImageUploadSize()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "File size exceeds 2MB limit!"));
                return;
            }
            try (InputStream input = uploadedFile.getInputStream()) {
                newCategory.setPosterImage(input.readAllBytes());
            } catch (IOException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to read image!"));
                return;
            }
        }
        boolean success = CategoryDAO.addCategory(newCategory);
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Category added successfully!"));
            categories.add(newCategory);
            newCategory = new Category();
            uploadedFile = null;
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to add category!"));
        }
    }

    /**
     * Deletes a category from the system by its ID.
     *
     * This method performs the following: - Attempts to delete the category
     * using the DAO. - Reloads the category list upon success. - Displays
     * success or error messages accordingly. - Clears any flash messages to
     * avoid message persistence across pages.
     *
     * @param categoryId the ID of the category to delete
     */
    public void deleteCategory(String categoryId) {
        FacesContext context = FacesContext.getCurrentInstance();
        boolean success = CategoryDAO.deleteCategory(categoryId);
        if (success) {
            System.out.println("Category deleted: " + categoryId);
            categories = CategoryDAO.getAllCategories();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Category deleted successfully!"));
        } else {
            System.out.println("Failed to delete category: " + categoryId);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to delete category!"));
        }
        context.getExternalContext().getFlash().setKeepMessages(false);
    }

    /**
     * Loads the category selected for editing based on the "categoryId" request
     * parameter.
     *
     * This method searches through the loaded categories to find the one
     * matching the provided ID, sets it as the currently selected category, and
     * navigates to the editCategory page. If not found, logs a message.
     */
    public void loadSelectedCategory() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
        String categoryId = params.get("categoryId");
        if (categoryId != null) {
            for (Category category : categories) {
                if (category.getCategoryId().equals(categoryId)) {
                    selectedCategory = category;
                    System.out.println("Loaded category for edit: " + selectedCategory.getName());
                    pageController.setPage("editCategory");
                    return;
                }
            }
            System.out.println("Category not found for editing!");
        }
    }

    /**
     * Updates the details of the selected category.
     *
     * This method: - Verifies that a category is selected. - Handles optional
     * image upload and stores it as a byte array. - Updates the category in the
     * database. - Refreshes the category list on success and displays a
     * message.
     */
    public void updateCategory() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (selectedCategory == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "No category selected!"));
            return;
        }
        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            try (InputStream input = uploadedFile.getInputStream()) {
                selectedCategory.setPosterImage(input.readAllBytes());
            } catch (IOException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to read image!"));
                return;
            }
        }
        boolean success = CategoryDAO.updateCategory(selectedCategory);
        if (success) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success!", "Category updated successfully!"));
            context.getExternalContext().getFlash().setKeepMessages(true);
            loadCategories();
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Failed to update category!"));
        }
    }

    /**
     * Returns the base64-encoded poster image for a given category. * This
     * method retrieves the image bytes from the database via DAO and encodes
     * them in Base64 to be displayed directly in the browser. If no image is
     * found, a default placeholder image path is returned.
     *
     *
     * @param categoryId the ID of the category whose image is requested
     * @return a base64-encoded image string (for embedding in HTML) or the path
     * to a default image
     */
    public String getCategoryImageBase64(String categoryId) {
        byte[] imageBytes = CategoryDAO.getCategoryImage(categoryId);
        if (imageBytes != null && imageBytes.length > 0) {
            return AppConfig.getBase64ImagePrefix() + Base64.getEncoder().encodeToString(imageBytes);

        }
        return "/resources/images/default.png";
    }

    /**
     * Returns the base64-encoded image string for a category by its name. *
     * Searches the loaded category list by name (case-insensitive) and returns
     * the base64-encoded poster image if found. If not found or if the list is
     * null, a default placeholder image path is returned.
     *
     *
     * @param name the name of the category
     * @return base64 image string or path to default image
     */
    public String getCategoryImageByName(String name) {
        if (categories == null) {
            return AppConfig.getDefaultImagePath();

        }
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                return getCategoryImageBase64(c.getCategoryId());
            }
        }
        return AppConfig.getDefaultImagePath();
    }

}
