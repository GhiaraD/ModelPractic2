package ro.pub.cs.systems.eim.lab03.modelpractic2;

import androidx.annotation.NonNull;

public class BPIinfo {
    private final String code;
    private final String rate;
    private final String desc;
    private final String ratef;

    public BPIinfo(String code, String rate, String desc, String ratef) {
        this.code = code;
        this.rate = rate;
        this.desc = desc;
        this.ratef = ratef;
    }

    public String getCode() {
        return code;
    }

    public String getRate() {
        return rate;
    }

    public String getDesc() {
        return desc;
    }

    public String getRatef() {
        return ratef;
    }

    @NonNull
    @Override
    public String toString() {
        return "BPIinfo{" +
                "code='" + code + '\'' +
                ", rate='" + rate + '\'' +
                ", desc='" + desc + '\'' +
                ", ratef='" + ratef + '\'' +
                '}';
    }
}
