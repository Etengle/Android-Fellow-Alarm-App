package netdb.course.softwarestudio.service.rest.model;

/**
 * A resource that can be POSTed to remote and then have the ID back in the "Location" response
 * header.
 */
public abstract class PostResource extends Resource {
    public void setId(String id) {
        this.id = id;
    }
}
