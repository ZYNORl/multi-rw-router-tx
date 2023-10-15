package top.zynorl.mrrtx.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.zynorl.mrrtx.annotation.DBRouter;
import top.zynorl.mrrtx.entity.Teacher;

/**
 * Created by zynorl on 2023/9/19 9:49
 */
@Mapper
@DBRouter(key = "teacherId", splitTable = true)
public interface ITeacherDao {

    @DBRouter(key = "teacherId", splitTable = true)
    Teacher queryTeacherInfoByTeacherId(Teacher teacher);

    @DBRouter(key = "teacherId", splitTable = true)
    Integer insertTeacher(Teacher teacher);
}
