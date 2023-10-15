package top.zynorl.mrrtx.constant;

/**
 * Created by zynorl on 2023/9/8 15:48
 */
public enum WriteOrReadEnum {
    WRITE("w"),READ("r");
    private String value;
    WriteOrReadEnum(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
}
