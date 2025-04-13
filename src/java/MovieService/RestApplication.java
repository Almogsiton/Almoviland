package MovieService;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Registers the base URI path for all RESTful web services.
 * Example: /api/movies
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // No additional configuration needed.
}
