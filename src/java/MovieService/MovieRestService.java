package MovieService;

import Modules.Movie;
import DAO.MovieDAO;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * RESTful Web Service that provides access to movie data.
 *
 * Responds with a list of all movies in JSON format. Endpoint:
 * http://localhost:8080/almoviland/api/movies
 */
@Path("/movies")
public class MovieRestService {

    /**
     * Retrieves a list of all movies from the database.
     *
     * @return a list of Movie objects in JSON format
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Movie> getMovies() {
        return MovieDAO.getAllMovies();
    }
}
