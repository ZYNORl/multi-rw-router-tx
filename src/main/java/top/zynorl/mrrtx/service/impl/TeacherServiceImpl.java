package top.zynorl.mrrtx.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zynorl.mrrtx.annotation.MultiDBTransaction;
import top.zynorl.mrrtx.entity.Student;
import top.zynorl.mrrtx.entity.Teacher;
import top.zynorl.mrrtx.mapper.IStudentDao;
import top.zynorl.mrrtx.mapper.ITeacherDao;
import top.zynorl.mrrtx.service.IStudentService;
import top.zynorl.mrrtx.service.ITeacherService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zynorl on 2023/9/19 9:45
 */
@Service
public class TeacherServiceImpl implements ITeacherService {
    @Autowired
    private ITeacherDao teacherDao;
    @Autowired
    private IStudentDao studentDao;
    @Autowired
    private IStudentService studentService;

    @Override
    public Teacher queryTeacherInfoByTeacherId(Teacher teacher) {
        return teacherDao.queryTeacherInfoByTeacherId(teacher);
    }

    @Override
    public Integer insertTeacher(Teacher teacher) {
        return teacherDao.insertTeacher(teacher);
    }

    // 添加老师和他的学生们, 多数据源操作，进行事务控制，没有异常
    @MultiDBTransaction
    @Override
    public Integer insertTeacherAndStudentsWithXA(List<Object> objects) {
        return doMain(objects);
    }

    // 添加老师和他的学生们, 多数据源操作，进行事务控制，有异常
    @MultiDBTransaction
    @Override
    public Integer insertTeacherAndStudentsWithXAHasException(List<Object> objects) {
        Integer count = doMain(objects);
        int a= 1/0;
        return count;
    }

    // 添加老师和他的学生们, 多数据源操作，不进行事务控制，没有异常
    @Override
    public Integer insertTeacherAndStudents(List<Object> objects) {
        return doMain(objects);
    }

    // 添加老师和他的学生们, 多数据源操作，不进行事务控制，有异常
    @Override
    public Integer insertTeacherAndStudentsHasException(List<Object> objects) {
        Integer count = doMain(objects);
        int a= 1/0;
        return count;
    }

    private Integer doMain(List<Object> objects) {
        AtomicInteger atomicCount = new AtomicInteger(0);
        objects.forEach(object->{
            if(object instanceof Teacher){
                atomicCount.addAndGet(teacherDao.insertTeacher((Teacher) object));
            }else if(object instanceof Student){
                atomicCount.addAndGet(studentService.insertStudent((Student) object));
            }
        });
        return atomicCount.get();
    }
}
