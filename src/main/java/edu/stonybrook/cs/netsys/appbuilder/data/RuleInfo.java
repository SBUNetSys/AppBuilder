package edu.stonybrook.cs.netsys.appbuilder.data;

/**
 * Created by qqcao on 11/26/16Saturday.
 * <p>
 * RuleInfo class is intended to record view element's attributes:
 * <p>
 * wear viewId(from wear layout xml), phone viewId, text and image info (from mapping rules)
 */
public class RuleInfo {
    private String wearViewId;
    private String phoneViewId;
    private String textInfo;
    private String imageInfo;

    public RuleInfo() {
    }

    public RuleInfo(String wearViewId, String phoneViewId, String textInfo, String imageInfo) {
        this.wearViewId = wearViewId;
        this.phoneViewId = phoneViewId;
        this.textInfo = textInfo;
        this.imageInfo = imageInfo;
    }

    public String getWearViewId() {
        return wearViewId;
    }

    public void setWearViewId(String wearViewId) {
        this.wearViewId = wearViewId;
    }

    public String getPhoneViewId() {
        return phoneViewId;
    }

    public void setPhoneViewId(String phoneViewId) {
        this.phoneViewId = phoneViewId;
    }

    public String getTextInfo() {
        return textInfo;
    }

    public void setTextInfo(String textInfo) {
        this.textInfo = textInfo;
    }

    public String getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(String imageInfo) {
        this.imageInfo = imageInfo;
    }

    @Override
    public String toString() {
        return "RuleInfo{" +
                "wearViewId='" + wearViewId + '\'' +
                ", phoneViewId='" + phoneViewId + '\'' +
                ", textInfo='" + textInfo + '\'' +
                ", imageInfo='" + imageInfo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleInfo)) return false;

        RuleInfo ruleInfo = (RuleInfo) o;

        return wearViewId.equals(ruleInfo.wearViewId)
                && (phoneViewId != null ? phoneViewId.equals(ruleInfo.phoneViewId) : ruleInfo.phoneViewId == null
                && (textInfo != null ? textInfo.equals(ruleInfo.textInfo) : ruleInfo.textInfo == null
                && (imageInfo != null ? imageInfo.equals(ruleInfo.imageInfo) : ruleInfo.imageInfo == null)));

    }

    @Override
    public int hashCode() {
        int result = wearViewId.hashCode();
        result = 31 * result + (phoneViewId != null ? phoneViewId.hashCode() : 0);
        result = 31 * result + (textInfo != null ? textInfo.hashCode() : 0);
        result = 31 * result + (imageInfo != null ? imageInfo.hashCode() : 0);
        return result;
    }
}
