package bistu.share;

import java.io.Serializable;
import java.sql.Timestamp;

public class Detail implements Serializable {
    private long id;
    private Timestamp modifyTime;
    private String fullText;

    public Detail(long id, Timestamp modifyTime, String fullText) {
        this.id = id;
        this.modifyTime = modifyTime;
        this.fullText = fullText;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Timestamp getModifyTime() { return modifyTime; }
    public void setModifyTime(Timestamp modifyTime) { this.modifyTime = modifyTime; }

    public String getFullText() { return fullText; }
    public void setFullText(String fullText) { this.fullText = fullText; }

    @Override
    public String toString() {
        return String.format("Detail: id=%d, modifyTime=%s, fullText=%s", id, modifyTime.toString(), fullText);
    }
    
}