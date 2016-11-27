package edu.stonybrook.cs.netsys.appbuilder.data;

/**
 * Created by qqcao on 11/26/16Saturday.
 *
 * View related info, currently support textual info, image info
 */
public class Info {
    private String text;
    private String image;

    public Info() {
    }
    public Info(String text, String image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
