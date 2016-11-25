package edu.stonybrook.cs.netsys.appbuilder.appInfo;

/**
 * Created by qqcao on 11/24/16Thursday.
 * App meta infomation like app name, app package name etc.
 */
public class App {
    private String appName;
    private String appPkgName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPkgName() {
        return appPkgName;
    }

    public void setAppPkgName(String appPkgName) {
        this.appPkgName = appPkgName;
    }
}
