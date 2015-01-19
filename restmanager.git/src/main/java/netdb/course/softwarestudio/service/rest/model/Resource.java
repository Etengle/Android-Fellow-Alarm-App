package netdb.course.softwarestudio.service.rest.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * An HTTP resource. All subclasses should implement a static method {@link #getCollectionName()}
 * and, if they will obtain instances from the remote, a default constructor.
 */
public abstract class Resource {
    public static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    /**
     * @return the collection name.
     */
    public static String getCollectionName() {
        return null;
    }

    protected String id;

    protected Resource() {
    }

    public String getId() {
        return id;
    }

	public void setId(String id){
		this.id = id;
	}

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    @Override
    public int hashCode() {
        return gson.toJson(this).hashCode();
    }

}
