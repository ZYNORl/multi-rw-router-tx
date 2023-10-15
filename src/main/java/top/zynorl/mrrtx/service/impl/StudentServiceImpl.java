package top.zynorl.mrrtx.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zynorl.mrrtx.entity.Student;
import top.zynorl.mrrtx.mapper.IStudentDao;
import top.zynorl.mrrtx.service.IStudentService;

/**
 * Created by zynorl on 2023/9/19 9:45
 */
@Service
public class StudentServiceImpl implements IStudentService {

    @Autowired
    private IStudentDao studentDao;

    @Override
    public Student queryStudentInfoByStudentId(Student student) {
        return studentDao.queryStudentInfoByStudentId(student);
    }

    @Override
    public Integer insertStudent(Student student) {
        return studentDao.insertStudent(student);
    }
}
