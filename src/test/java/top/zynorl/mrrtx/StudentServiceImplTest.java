package top.zynorl.mrrtx;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.zynorl.mrrtx.entity.Student;
import top.zynorl.mrrtx.service.IStudentService;

/**
 * Created by zynorl on 2023/9/21 10:25
 */
@SpringBootTest
class StudentServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImplTest.class);

    @Autowired
    private IStudentService studentService;


    @BeforeEach
    void setUp() {
        logger.debug("【StudentServiceImplTest】测试开始。。。");
    }

    @AfterEach
    void setDown() {
        logger.debug("【StudentServiceImplTest】测试结束。。。");
    }

    @Test
    void queryStudentInfoByStudentId() {
        Student student = studentService.queryStudentInfoByStudentId(new Student("980765512"));
        logger.debug("测试结果：{}", JSON.toJSONString(student));
    }

    @Test
    void insertStudent() {
        Student student = new Student();
        student.setStudentId("980765512");
        student.setStudentName("小牛同学");
        student.setStudentPassword("123456");
        Integer count = studentService.insertStudent(student);
        Assertions.assertEquals(1, count);
    }
}