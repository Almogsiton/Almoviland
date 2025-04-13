package MovieService;

import Modules.Movie;
import DAO.MovieDAO;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * RESTful Web Service that provides movie data in JSON format.
 * Accessible via: http://localhost:8080/almoviland/api/movies
 */
@Path("/movies")
public class MovieRestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Movie> getMovies() {
        return MovieDAO.getAllMovies();
    }
}
