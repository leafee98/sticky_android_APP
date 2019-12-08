package bistu.share;

import java.io.Serializable;
import java.sql.Timestamp;

public class Overview implements Serializable {
    private long id;
    private Timestamp modifyTime;
    private String summary;

    public Overview(long id, Timestamp modifyTime, String summary) {
        this.id = id;
        this.modifyTime = modifyTime;
        this.summary = summary;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Timestamp getModifyTime() { return modifyTime; } 
    public void setModifyTime(Timestamp modifyTime) { this.modifyTime = modifyTime; } 

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    @Override
    public String toString() {
        return String.format("Overview: id=%ld, modifyTime=%s, summary=%s", id, modifyTime.toString(), summary);
    }

}