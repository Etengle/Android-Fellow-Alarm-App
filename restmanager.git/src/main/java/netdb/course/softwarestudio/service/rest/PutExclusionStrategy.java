package netdb.course.softwarestudio.service.rest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import netdb.course.softwarestudio.service.rest.model.Putable;

public class PutExclusionStrategy implements ExclusionStrategy {

    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(Putable.class) == null;
    }
}

