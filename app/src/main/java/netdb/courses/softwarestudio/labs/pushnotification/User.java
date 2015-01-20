package netdb.courses.softwarestudio.labs.pushnotification;

import netdb.course.softwarestudio.service.rest.model.Putable;
import netdb.course.softwarestudio.service.rest.model.Resource;

/**
 * Created by Slighten on 2015/1/19.
 */
public class User extends Resource {

    private String name;
    @Putable
    private String Id;

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setId(String Id){
        this.Id = Id;
    }

    public String getId(){
        return Id;
    }

    public static String getCollectionName() {
        return "users";
    }
}
