package edu.stonybrook.cs.netsys.appbuilder;

import edu.stonybrook.cs.netsys.appbuilder.data.Info;
import edu.stonybrook.cs.netsys.appbuilder.data.RuleInfo;
import edu.stonybrook.cs.netsys.appbuilder.utils.XmlUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javafx.util.Pair;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.stonybrook.cs.netsys.appbuilder.data.App;

import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.*;
import static edu.stonybrook.cs.netsys.appbuilder.utils.XmlUtil.serializeMapToFile;

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
 * <p>
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
    private static final boolean isDebug = true;
    private static Configuration configuration;
    private static final String unzippedFileDest = "build/temp/";
    private static final String[] TEMPLATES = new String[]{MANIFEST_NAME, BUILD_GRADLE_NAME, ACTIVITY_NAME};

    private static HashMap<String, String> stringMap = new HashMap<>();
    private static HashMap<String, Info> idInfoMap = new HashMap<>();

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
        if (isDebug) {
            System.out.println("pkgName:\n" + pkgName);
        }
        String appOutPath = unzippedFileDest + pkgName + File.separator + OUTPUT_DIR_NAME;
        if (isDebug) {
            System.out.println("appOutPath:\n" + appOutPath);
        }
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
        if (isDebug) {
            System.out.println("appName:\n" + appName);
        }
        stringMap.put("app_name", appName);
        // step 2, start parsing below code starts here

        // generate project files based on templates
        HashMap<String, Object> root = new HashMap<>();
        App wearApp = new App();
        wearApp.setAppName(appName);
        wearApp.setAppPkgName(pkgName);
        root.put(APP_TAG_IN_TEMPLATE, wearApp);

        // just for test
        if (isDebug) {
            for (String templateName : TEMPLATES) {
                Template temp = configuration.getTemplate(templateName);
                Writer out = new OutputStreamWriter(System.out);
                temp.process(root, out);
            }
        }

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
        File mainLayoutDestFile = new File(appOutPath + LAYOUT_PATH);
        FileUtils.copyFileToDirectory(mainLayoutFile, mainLayoutDestFile);

        /*** below code relies on parsing both mapping rules and layout xml files ***/
        // pre-process mapping rules for generating xml layout like card type


        // process mapping rules and layout xml to set string values and drawable resources
        File mappingRuleDir = new File(Paths.get(unzippedFileDest, pkgName, RULE_DIR_NAME).toString());
        File[] mappingRuleFiles = mappingRuleDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FilenameUtils.getBaseName(name).endsWith(RULE_SUFFIX);
            }
        });

        assert mappingRuleFiles != null;
        for (File mappingRuleFile : mappingRuleFiles) {
            ArrayList<RuleInfo> infoList = XmlUtil.parseMappingRule(mappingRuleFile);

            File layoutOutPutFolder = new File(appOutPath + LAYOUT_PATH,
                    mappingRuleFile.getName().replace(RULE_SUFFIX, LAYOUT_SUFFIX));
            // copy updated layout files to output
            FileUtils.copyFile(new File(Paths.get(unzippedFileDest, pkgName, RES_DIR_NAME).toString(),
                    mappingRuleFile.getName().replace(RULE_SUFFIX, LAYOUT_SUFFIX)), layoutOutPutFolder);

            String prefName = FilenameUtils.removeExtension(mappingRuleFile.getName())
                    .replace(RULE_SUFFIX, PREF_SUFFIX);
            if (isDebug) {
                System.out.println("infoList:\n" + infoList);
                System.out.println("prefName:\n" + prefName);
                System.out.println("layoutFile:\n" + layoutOutPutFolder);
            }


            for (RuleInfo ruleInfo : infoList) {
                String wearViewId = ruleInfo.getWearViewId();
                String text = ruleInfo.getTextInfo();
                Info changedInfo = new Info();

                if (text != null) {
                    String entryName = FilenameUtils.removeExtension(layoutOutPutFolder.getName()) + "_" + wearViewId;
                    // put text to stringMap
                    stringMap.put(entryName, text);
                    // find the node with id and set text
                    String value = STRING_PREFIX + entryName;
                    changedInfo.setText(value);

//                    findViewIdSetInfo(layoutOutPutFolder, wearViewId, value);
                }

                String image = ruleInfo.getImageInfo();
                if (image != null) {
                    // if image file exists
                    File imageFile = new File(Paths.get(unzippedFileDest, pkgName, RES_DIR_NAME).toString(), image);
                    if (!imageFile.exists()) {
                        continue;
                    }
                    // copy to drawable folder
                    FileUtils.copyFileToDirectory(imageFile, new File(appOutPath + DRAWABLE_PATH));
                    // find the node with id and set image
                    String value = DRAWABLE_PREFIX + FilenameUtils.removeExtension(image);
                    changedInfo.setImage(value);
//                    findViewIdSetInfo(layoutOutPutFolder, wearViewId, value);
                }

                idInfoMap.put(wearViewId, changedInfo);
            }

            // set text and image info to layout file at once

        }

        // write stringMap to strings.xml
        File stringsFile = new File(appOutPath + VALUES_PATH, STRINGS_FILE_NAME);
        serializeMapToFile(stringMap, stringsFile);

        // process mapping rules and layout xml to generate arrays.xml files

        // copy above processed xml files to corresponding directory

        // put all together, copy app project to WearAppEnv folder to start building apk
    }

    private static void findViewIdSetInfo(File layoutFile, HashMap<String, Info> idInfoMap) throws IOException {
        String layoutContent = FileUtils.readFileToString(layoutFile, Charset.defaultCharset());
        // parse layout, search target node, set node attribute, write back to file

        //parse layout to hash map, map key is wearViewId, key is list of attribute-value pairs
        HashMap<String, ArrayList<Pair<String, String>>> idMap = new HashMap<>();


        // search target node and add node attributes
        Set<String> idSet = idInfoMap.keySet();
        for (String wearViewId : idSet) {
            ArrayList<Pair<String, String>> pairs = idMap.get(wearViewId);

            Info info = idInfoMap.get(wearViewId);
            String textAttrName = "android:text";
            String textAttrValue = info.getText();
            pairs.add(new Pair<>(textAttrName, textAttrValue));
            String imageAttrName = "android:background";
            String imageAttrValue = info.getImage();
            pairs.add(new Pair<>(imageAttrName, imageAttrValue));

        }

        // serialize idMap back to the layoutFile


    }

}
