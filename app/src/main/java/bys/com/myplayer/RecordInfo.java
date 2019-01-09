package bys.com.myplayer;

public class RecordInfo {
    public String number,date, duration, songUrl;

    public RecordInfo(){
    }

    public RecordInfo(String number, String date, String duration, String songUrl) {
        this.number = number;
        this.date = date;
        this.duration = duration;
        this.songUrl = songUrl;
    }

}
