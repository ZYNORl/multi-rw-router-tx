package top.zynorl.mrrtx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.zynorl.mrrtx.entity.Student;
import top.zynorl.mrrtx.entity.Teacher;
import top.zynorl.mrrtx.service.ITeacherService;

import java.util.ArrayList;

/**
 * Created by zynorl on 2023/9/21 10:27
 */
@SpringBootTest
class TeacherServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(TeacherServiceImplTest.class);

    @Autowired
    private ITeacherService teacherService;


    @BeforeEach
    void setUp() {
        logger.debug("【TeacherServiceImplTest】测试开始。。。");
    }

    @AfterEach
    void setDown() {
        logger.debug("【TeacherServiceImplTest】测试结束。。。");
    }


    // 添加老师和他的学生们, 多数据源操作，进行事务控制，没有异常
    // 期望数据库添加成功
    @Test
    void insertTeacherAndStudentsWithXA() {
        Teacher teacher = new Teacher();
        teacher.setTeacherId("9807005");
        teacher.setTeacherName("刘老师");
        teacher.setTeacherPassword("123456");
        Student student = new Student();
        student.setStudentId("9807655");
        student.setStudentName("小牛同学");
        student.setStudentPassword("123456");
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(teacher);
        objects.add(student);
        Assertions.assertEquals(objects.size(), teacherService.insertTeacherAndStudentsWithXA(objects));
    }

    // 添加老师和他的学生们, 多数据源操作，进行事务控制，有异常
    // 期望数据库添加失败，同时抛出异常
    @Test
    void insertTeacherAndStudentsWithXAHasException() {
        Teacher teacher = new Teacher();
        teacher.setTeacherId("9807005");
        teacher.setTeacherName("刘老师");
        teacher.setTeacherPassword("123456");
        Student student = new Student();
        student.setStudentId("9807655");
        student.setStudentName("小牛同学");
        student.setStudentPassword("123456");
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(teacher);
        objects.add(student);
        Assertions.assertThrows(ArithmeticException.class, ()-> teacherService.insertTeacherAndStudentsWithXAHasException(objects));
    }

    // 添加老师和他的学生们, 多数据源操作，不进行事务控制，没有异常
    // 期望数据库添加成功
    @Test
    void insertTeacherAndStudents() {
        Teacher teacher = new Teacher();
        teacher.setTeacherId("9807005");
        teacher.setTeacherName("刘老师");
        teacher.setTeacherPassword("123456");
        Student student = new Student();
        student.setStudentId("9807655");
        student.setStudentName("小牛同学");
        student.setStudentPassword("123456");
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(teacher);
        objects.add(student);
        Assertions.assertEquals(objects.size(), teacherService.insertTeacherAndStudents(objects));
    }

    // 添加老师和他的学生们, 多数据源操作，不进行事务控制，有异常
    // 期望数据库添加成功，同时抛出异常
    @Test
    void insertTeacherAndStudentsHasException() {
        Teacher teacher = new Teacher();
        teacher.setTeacherId("9807005");
        teacher.setTeacherName("刘老师");
        teacher.setTeacherPassword("123456");
        Student student = new Student();
        student.setStudentId("9807655");
        student.setStudentName("小牛同学");
        student.setStudentPassword("123456");
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(teacher);
        objects.add(student);
        Assertions.assertThrows(ArithmeticException.class, ()-> teacherService.insertTeacherAndStudentsHasException(objects));
    }
}