package com.Kenta;

import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.projects.Project;

import java.io.IOException;

public interface ClassifierWrapper<T> {

    public ObjectClassifier<T> getClassifier() throws IOException;

}

