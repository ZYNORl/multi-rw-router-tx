package top.zynorl.mrrtx.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.zynorl.mrrtx.annotation.DBRouter;
import top.zynorl.mrrtx.entity.Student;

/**
 * Created by zynorl on 2023/9/19 9:48
 */
@Mapper
public interface IStudentDao {
    @DBRouter(key = "studentId", splitTable = true)
    Student queryStudentInfoByStudentId(Student student);

    @DBRouter(key = "studentId", splitTable = true)
    Integer insertStudent(Student student);
}
