package top.zynorl.mrrtx.entity;

import java.util.Date;

/**
 * Created by zynorl on 2023/9/19 9:40
 */
public class Teacher{

    private Long id;
    private String teacherId;          // 老师ID
    private String teacherName;    // 姓名
    private String teacherPassword;    // 密码
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间

    public Teacher() {
    }

    public Teacher(String teacherId) {
        this.teacherId = teacherId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherPassword() {
        return teacherPassword;
    }

    public void setTeacherPassword(String teacherPassword) {
        this.teacherPassword = teacherPassword;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
