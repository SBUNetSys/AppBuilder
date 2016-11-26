package edu.stonybrook.cs.netsys.appbuilder;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;

import edu.stonybrook.cs.netsys.appbuilder.data.App;

import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ACTIVITY_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.APP_NAME_TEXT;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.APP_TAG_IN_TEMPLATE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.BUILD_GRADLE_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.CODE_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.CONFIG_DIR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.IC_LAUNCHER;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAUNCHER_ICON_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAYOUT_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.MANIFEST_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.MANIFEST_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.OUTPUT_DIR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.TEMPLATE_FOLDER_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.UIWEAR_ACTIVITY_LAYOUT_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.UIWEAR_DEFAULT_ICON_PATH;

/**
 * Created by qqcao on 11/24/16 Thursday. <p> Wear app building process: <p>
 * 1. receive zip file from UIWear phone proxy, unzip file and validate preferences
 * and mapping rules (currently skip)
 * 2. process project files, pre-process mapping rules for generating xml layout (if using UIWear
 * provided view types, like Card etc)
 * 3. process generated xml layout files from step 2 or user provided customize xml layout
 * files: fill default values in the layout, e.g., initial text for TextView, initial image for
 * ImageView etc.
 * 4. use UIWear rendering protocol to generate arrays.xml by parsing preference file name,
 * extracting the view id.
 * 5. put all together and generate app project files, copy to WearAppEnv project, call gradle
 * build scripts to build apk, send back to phone proxy.
 *
 * Rendering protocol: generally there are three sections for the arrays.xml :
 * 1. The first section contains preference id array and wear app layout array,
 * these arrays in place in strict order, since how UIWear find the right layout for inflating
 * and rendering is based on the order(or index of the array).
 * 2. Second section is for general view types like card and customize view, except
 * for list-like view types. In this section, UIWear indexes all view ids(phone and wear) for the
 * view of each preference.
 * 3. Third section is specifically designed for list-like view like
 * WearableListView, RecyclerView, ListView etc. The different part to section 2 is that for
 * list-like view types, besides these view themselves, they need view for their items. Each item
 * shares the same (or varies a little) style. In addition to the section 2 view id indexation, this
 * section also indexes item view ids and relate them to their parent, i.e., list-like views.
 */
public class AppBuilderMain {
    private static Configuration configuration;
    private static final String unzippedFileDest = "build/temp/";

    static {
        try {
            configuration = FreeMarker.initialize(new File(TEMPLATE_FOLDER_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, TemplateException {
        // step 0, open socket listening for new wear app building request
        // step 1, new thread handle received file, pkgName.zip, unzip and put to temp folder
        String receivedFileSavedPath = "src/main/resources/";
        String fileName = "com.spotify.music.zip";
        String pkgName = FilenameUtils.getBaseName(fileName);
        System.out.println(pkgName);
        String appOutPath = receivedFileSavedPath + pkgName + File.separator + OUTPUT_DIR_NAME;
        System.out.println(appOutPath);
        // extract zip file to temp folder
        try {
            ZipFile zipFile = new ZipFile(receivedFileSavedPath + fileName);
            zipFile.extractAll(unzippedFileDest);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        String appNameFilePath = Paths.get(unzippedFileDest, pkgName,
                CONFIG_DIR_NAME, APP_NAME_TEXT).toString();

        String appName = FileUtils.readFileToString(new File(appNameFilePath),
                Charset.defaultCharset());
        System.out.println(appName);
        // step 2, start parsing below code starts here

        // generate project files based on templates
        HashMap<String, Object> root = new HashMap<>();
        App wearApp = new App();
        wearApp.setAppName(appName);
        wearApp.setAppPkgName(pkgName);
        root.put(APP_TAG_IN_TEMPLATE, wearApp);

//        // just for test
//        for (String templateName : TEMPLATES) {
//            Template temp = configuration.getTemplate(templateName);
//            Writer out = new OutputStreamWriter(System.out);
//            temp.process(root, out);
//        }

        // process build.gradle
        Template buildGradleTemplate = configuration.getTemplate(BUILD_GRADLE_NAME);
        File buildGradleFile = new File(appOutPath, FilenameUtils.getBaseName(BUILD_GRADLE_NAME));
        FileUtils.touch(buildGradleFile);
        buildGradleTemplate.process(root, new FileWriter(buildGradleFile));

        // process MainActivity
        Template mainActivityTemplate = configuration.getTemplate(ACTIVITY_NAME);
        File mainActivityFile = new File(appOutPath + CODE_PATH + pkgName.replace(".", "/"),
                FilenameUtils.getBaseName(ACTIVITY_NAME));
        FileUtils.touch(mainActivityFile);
        mainActivityTemplate.process(root, new FileWriter(mainActivityFile));

        // process AndroidManifest.xml
        Template manifestTemplate = configuration.getTemplate(MANIFEST_NAME);
        File manifestFile = new File(appOutPath + MANIFEST_PATH,
                FilenameUtils.getBaseName(MANIFEST_NAME));
        FileUtils.touch(manifestFile);
        manifestTemplate.process(root, new FileWriter(manifestFile));


        // process launcher icon and activity_main.xml
        File launcherIconFile = new File(Paths.get(unzippedFileDest, pkgName,
                CONFIG_DIR_NAME, IC_LAUNCHER).toString());
        File launcherIconDestFile = new File(appOutPath + LAUNCHER_ICON_PATH + IC_LAUNCHER);
        if (launcherIconFile.exists()) {
            FileUtils.copyFile(launcherIconFile, launcherIconDestFile);
        } else {
            //copy default icon
            FileUtils.copyFile(new File(UIWEAR_DEFAULT_ICON_PATH), launcherIconDestFile);
        }

        File mainLayoutFile = new File(UIWEAR_ACTIVITY_LAYOUT_PATH);
        File mainLayoutDestFile = new File(appOutPath + LAYOUT_PATH );
        FileUtils.copyFileToDirectory(mainLayoutFile, mainLayoutDestFile);

        /*** below code relies on parsing both mapping rules and layout xml files ***/
        // pre-process mapping rules for generating xml layout like card type


        // process mapping rules and layout xml to set string values and drawable resources

        // process mapping rules and layout xml to generate arrays.xml files

        // copy above processed xml files to corresponding directory

        // put all together, copy app project to WearAppEnv folder to start building apk
    }

}
