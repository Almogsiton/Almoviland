

import DAO.UserDAO;

/**
 * Entry point for the Almoviland system.
 * 
 * This class is responsible for initializing the system, including the creation
 * of the first admin user if no users exist in the database.
 */
public class Main {

    /**
     * The main method that runs the initial setup for the system.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Starting Almoviland System Setup...");
        
        
        // Creates the first admin user if no users exist
        UserDAO.createAdminIfNotExists();

        System.out.println("Setup complete! You can now run the system.");
    }
}
