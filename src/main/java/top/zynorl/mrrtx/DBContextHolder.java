package top.zynorl.mrrtx;

/**
 * Created by zynorl on 2023/9/18 19:46
 *
 * 数据源上下文
 */
public class DBContextHolder {

    private static final ThreadLocal<String> masterDBKey = new ThreadLocal<>();
    private static final ThreadLocal<String> slaverDBKey = new ThreadLocal<>();
    private static final ThreadLocal<String> tbKey = new ThreadLocal<>();
    private static final ThreadLocal<String> writeOrRead = new ThreadLocal<>();

    public static void setMasterDBKey(String dbKeyIdx){
        masterDBKey.set(dbKeyIdx);
    }

    public static String getMasterDBKey(){
        return masterDBKey.get();
    }

    public static void setSlaverDBKey(String dbKeyIdx){
        slaverDBKey.set(dbKeyIdx);
    }

    public static String getSlaverDBKey(){
        return slaverDBKey.get();
    }

    public static void setTBKey(String tbKeyIdx){
        tbKey.set(tbKeyIdx);
    }

    public static String getTBKey(){
        return tbKey.get();
    }

    public static void setWriteOrRead(String WOrR){
        writeOrRead.set(WOrR);
    }

    public static String getWriteOrRead(){
        return writeOrRead.get();
    }

    public static void clearMasterDBKey(){
        masterDBKey.remove();
    }

    public static void clearSlaverDBKey(){
        slaverDBKey.remove();
    }

    public static void clearTBKey(){
        tbKey.remove();
    }

    public static void clearWriteOrRead(){
        writeOrRead.remove();
    }
}
