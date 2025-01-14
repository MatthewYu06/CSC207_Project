package main.java.use_case.courses;


import main.java.entity.Course;

import java.util.Map;

public interface AddCourseDataAccessInterface {
    public Course getCourse(String courseId);
    public void saveCourse(Course course);
    public boolean existsByID(String courseId);

    Map<String, Course> getCourses();
}
