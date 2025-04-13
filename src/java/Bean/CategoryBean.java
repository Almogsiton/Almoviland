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
 * Managed Bean responsible for managing movie categories in the system.
 * Allows adding, updating, deleting, and retrieving categories.
 */
@Named
@SessionScoped
public class CategoryBean implements Serializable {

    private List<Category> categories;
    private Category newCategory = new Category();
    private Part uploadedFile;
    private Category selectedCategory;

    @Inject
    private PageController pageController;

    /**
     * Initializes the bean and loads categories from the database.
     */
    public CategoryBean() {
        loadCategories();
    }

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
     * Loads all categories from the database.
     */
    private void loadCategories() {
        categories = CategoryDAO.getAllCategories();
        System.out.println("Categories Loaded: " + categories.size());
    }

    /**
     * Adds a new category to the system.
     * Validates input fields and image size before saving.
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
     * Deletes a category by its ID.
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
     * Loads a category selected for editing based on request parameters.
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
     * Updates the selected category details, including a new image if uploaded.
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
     * Returns the base64-encoded image string for the category.
     *
     * @param categoryId the category ID
     * @return base64 image string or default image path
     */
    public String getCategoryImageBase64(String categoryId) {
        byte[] imageBytes = CategoryDAO.getCategoryImage(categoryId);
        if (imageBytes != null && imageBytes.length > 0) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }
        return "/resources/images/default.png";
    }
    
    public String getCategoryImageByName(String name) {
    if (categories == null) return "/resources/images/default.png";
    for (Category c : categories) {
        if (c.getName().equalsIgnoreCase(name)) {
            return getCategoryImageBase64(c.getCategoryId());
        }
    }
    return "/resources/images/default.png";
}

}
