package top.zynorl.mrrtx.entity;

import java.util.Date;

/**
 * Created by zynorl on 2023/9/19 9:37
 */
public class Student{
    private Long id;
    private String studentId;          // 学生ID
    private String studentName;    // 昵称
    private String studentPassword;    // 密码
    private Date createTime;        // 创建时间
    private Date updateTime;        // 更新时间

    public Student() {
    }

    public Student(String studentId) {
        this.studentId = studentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword;
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
