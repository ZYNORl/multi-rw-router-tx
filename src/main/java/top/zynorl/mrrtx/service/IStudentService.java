package top.zynorl.mrrtx.service;


import top.zynorl.mrrtx.entity.Student;

/**
 * Created by zynorl on 2023/9/19 9:44
 */
public interface IStudentService {
    Student queryStudentInfoByStudentId(Student student);
    Integer insertStudent(Student student);
}
