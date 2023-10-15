package top.zynorl.mrrtx.service;


import top.zynorl.mrrtx.entity.Teacher;

import java.util.List;

/**
 * Created by zynorl on 2023/9/19 9:45
 */
public interface ITeacherService {
    Teacher queryTeacherInfoByTeacherId(Teacher teacher);
    Integer insertTeacher(Teacher teacher);
    // 添加老师和他的学生们
    Integer insertTeacherAndStudentsWithXA(List<Object> objects);
    Integer insertTeacherAndStudents(List<Object> objects);
    Integer insertTeacherAndStudentsWithXAHasException(List<Object> objects);
    Integer insertTeacherAndStudentsHasException(List<Object> objects);
}