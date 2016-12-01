package edu.stonybrook.cs.netsys.appbuilder;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import edu.stonybrook.cs.netsys.appbuilder.data.App;
import edu.stonybrook.cs.netsys.appbuilder.data.Info;
import edu.stonybrook.cs.netsys.appbuilder.data.RuleInfo;
import edu.stonybrook.cs.netsys.appbuilder.utils.XmlUtil;
import sun.security.ssl.Debug;

import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ACTIVITY_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.APP_NAME_TEXT;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.APP_TAG_IN_TEMPLATE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ARRAYS_FILE_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ARRAY_PREFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ARRAY_TAG;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ATTR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.BUILD_GRADLE_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.CODE_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.CONFIG_DIR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.DRAWABLE_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.DRAWABLE_PREFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.IC_LAUNCHER;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ID_ATTR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ID_PREFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ID_VALUE_PREFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.IMAGE_ATTR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.INTEGER_ARRAY_TAG;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ITEM_LAYOUTS_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ITEM_SUFFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ITEM_TAG;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAUNCHER_ICON_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAYOUTS_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAYOUT_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAYOUT_PREFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.LAYOUT_SUFFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.MANIFEST_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.MANIFEST_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.OUTPUT_DIR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.PHONE_ID_SUFFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.PHONE_ITEM_VIEW_ID_ARRAY_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.PHONE_VIEW_IDS_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.PHONE_VIEW_ID_ARRAY_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.PREFS_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.PREF_SUFFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.RESOURCES_TAG;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.RES_DIR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.RULE_DIR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.RULE_SUFFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.STRINGS_FILE_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.STRING_ARRAY_TAG;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.STRING_PREFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.TEMPLATE_FOLDER_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.TEXT_ATTR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.UIWEAR_ACTIVITY_LAYOUT_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.UIWEAR_DEFAULT_ICON_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.VALUES_PATH;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.WEAR_APP_ENV_FOLDER_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.WEAR_ID_SUFFIX;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.WEAR_ITEM_VIEW_ID_ARRAY_VALUE;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.WEAR_VIEW_ID_ARRAY_VALUE;
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
    private static final String unzippedFileDest = "build/temp/";
    private static final String[] TEMPLATES = new String[]{MANIFEST_NAME, BUILD_GRADLE_NAME, ACTIVITY_NAME};
    private static Configuration configuration;
    private static HashMap<String, String> stringMap = new HashMap<>();
    private static HashMap<String, Info> idInfoMap = new HashMap<>();

    private static ArrayList<String> prefs = new ArrayList<>();

    private static ArrayList<String> layouts = new ArrayList<>();
    private static ArrayList<String> wearViewIdArray = new ArrayList<>();
    private static ArrayList<String> phoneViewIdArray = new ArrayList<>();
    private static HashMap<String, ArrayList<String>> wearViewIdIndexMap = new HashMap<>();
    private static HashMap<String, ArrayList<String>> phoneViewIdIndexMap = new HashMap<>();

    private static ArrayList<String> phoneItemViewIds = new ArrayList<>();

    private static ArrayList<String> itemLayouts = new ArrayList<>();
    private static ArrayList<String> wearItemViewIdArray = new ArrayList<>();
    private static ArrayList<String> phoneItemViewIdArray = new ArrayList<>();
    private static HashMap<String, ArrayList<String>> wearItemViewIdIndexMap = new HashMap<>();
    private static HashMap<String, ArrayList<String>> phoneItemViewIdIndexMap = new HashMap<>();

    private static final String pkgName = "com.bandlab.bandlab";

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
        if (isDebug) {
            System.out.println("pkgName: " + pkgName);
            System.out.println();
        }
        String appOutPath = unzippedFileDest + pkgName + File.separator + OUTPUT_DIR_NAME;
        if (isDebug) {
            System.out.println("appOutPath: " + appOutPath);
            System.out.println();
        }

        File appFilesTempFoler = new File(unzippedFileDest + pkgName);
        if (appFilesTempFoler.exists()) {
            FileUtils.deleteDirectory(appFilesTempFoler);
        }

        if (isDebug) {
            FileUtils.copyDirectoryToDirectory(new File(receivedFileSavedPath + pkgName),
                    new File(unzippedFileDest));
        } else {
            // extract zip file to temp folder
            try {
                String fileName = pkgName + ".zip";
                ZipFile zipFile = new ZipFile(receivedFileSavedPath + fileName);
                zipFile.extractAll(unzippedFileDest);
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }

        FileUtils.copyDirectory(new File(WEAR_APP_ENV_FOLDER_NAME), new File(appOutPath));
        Set<PosixFilePermission> permissionsSet = new HashSet<>();
        permissionsSet.add(PosixFilePermission.OWNER_READ);
        permissionsSet.add(PosixFilePermission.OWNER_WRITE);
        permissionsSet.add(PosixFilePermission.OWNER_EXECUTE);
        permissionsSet.add(PosixFilePermission.GROUP_READ);
        permissionsSet.add(PosixFilePermission.GROUP_EXECUTE);
        permissionsSet.add(PosixFilePermission.OTHERS_READ);
        permissionsSet.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(Paths.get(appOutPath, "gradlew"), permissionsSet);
        String outputPath = appOutPath;
        appOutPath += "/app/";

        String appNameFilePath = Paths.get(unzippedFileDest, pkgName,
                CONFIG_DIR_NAME, APP_NAME_TEXT).toString();

        String appName = FileUtils.readFileToString(new File(appNameFilePath),
                Charset.defaultCharset());
        if (isDebug) {
            System.out.println("appName: " + appName);
            System.out.println();
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
        File mappingRuleDir = new File(Paths.get(unzippedFileDest, pkgName, RULE_DIR_NAME)
                .toString());
        File[] mappingRuleFiles = mappingRuleDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FilenameUtils.getBaseName(name).endsWith(RULE_SUFFIX);
            }
        });

        assert mappingRuleFiles != null;
        for (File mappingRuleFile : mappingRuleFiles) {
            ArrayList<RuleInfo> infoList = XmlUtil.parseMappingRule(mappingRuleFile);

            File layoutOutputFile = new File(appOutPath + LAYOUT_PATH,
                    mappingRuleFile.getName().replace(RULE_SUFFIX, LAYOUT_SUFFIX));
            // copy updated layout files to output
            FileUtils.copyFile(new File(Paths.get(unzippedFileDest, pkgName, RES_DIR_NAME)
                            .toString(), mappingRuleFile.getName().replace(RULE_SUFFIX, LAYOUT_SUFFIX)),
                    layoutOutputFile);

            ArrayList<String> wearViewIds = new ArrayList<>();
            ArrayList<String> phoneViewIds = new ArrayList<>();

            for (RuleInfo ruleInfo : infoList) {
                String wearViewId = ruleInfo.getWearViewId();
                if (!wearViewIds.contains(wearViewId)) {
                    if (isDebug) {
                        System.out.println("wearViewId add: " + ID_PREFIX + wearViewId);
                    }
                    wearViewIds.add(ID_PREFIX + wearViewId);
                }

                String phoneViewId = ruleInfo.getPhoneViewId();
                if (!phoneViewIds.contains(phoneViewId)) {
                    if (isDebug) {
                        System.out.println("phoneViewId add: " + phoneViewId);
                    }
                    phoneViewIds.add(phoneViewId);
                }

                if (ruleInfo.isListView()) {
                    phoneItemViewIds.add(phoneViewId);
                }

                String text = ruleInfo.getTextInfo();
                Info changedInfo = new Info();

                if (text != null) {
                    String entryName = FilenameUtils.removeExtension(layoutOutputFile.getName())
                            + "_" + wearViewId;
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
                    File imageFile = new File(Paths.get(unzippedFileDest, pkgName, RES_DIR_NAME)
                            .toString(), image);
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

                idInfoMap.put(ID_VALUE_PREFIX + wearViewId, changedInfo);
            }

            // set text and image info to layout file at once
            updateLayout(layoutOutputFile, idInfoMap);
            idInfoMap.clear();

            String prefName = FilenameUtils.removeExtension(mappingRuleFile.getName())
                    .replace(RULE_SUFFIX, PREF_SUFFIX);
            if (isDebug) {
                System.out.println();
                System.out.println("infoList: " + infoList);
                System.out.println("mappingRule: " + mappingRuleFile);
                System.out.println("prefName: " + prefName);
                System.out.println("layoutFile: " + layoutOutputFile);
            }

            String layoutName = LAYOUT_PREFIX + FilenameUtils.removeExtension(layoutOutputFile
                    .getName());
            String wearIdName = ARRAY_PREFIX + prefName + WEAR_ID_SUFFIX;
            String phoneIdName = ARRAY_PREFIX + prefName + PHONE_ID_SUFFIX;
            if (isDebug) {
                System.out.println(prefName);
                System.out.println(layoutName);
                System.out.println(wearIdName);
                System.out.println(phoneIdName);
            }

            if (!prefName.endsWith(ITEM_SUFFIX + PREF_SUFFIX)) {
                prefs.add(prefName);
            }

            if (prefName.endsWith(ITEM_SUFFIX + PREF_SUFFIX)) {
                itemLayouts.add(layoutName);
                wearItemViewIdArray.add(wearIdName);
                phoneItemViewIdArray.add(phoneIdName);
                wearItemViewIdIndexMap.put(prefName + WEAR_ID_SUFFIX, wearViewIds);
                phoneItemViewIdIndexMap.put(prefName + PHONE_ID_SUFFIX, phoneViewIds);
                System.out.println();
            } else {
                layouts.add(layoutName);
                wearViewIdArray.add(wearIdName);
                phoneViewIdArray.add(phoneIdName);
                wearViewIdIndexMap.put(prefName + WEAR_ID_SUFFIX, wearViewIds);
                phoneViewIdIndexMap.put(prefName + PHONE_ID_SUFFIX, phoneViewIds);
            }
        }

        // write stringMap to strings.xml
        File stringsFile = new File(appOutPath + VALUES_PATH, STRINGS_FILE_NAME);
        serializeMapToFile(stringMap, stringsFile);

        // process mapping rules and layout xml to generate arrays.xml files
        File arraysFile = new File(appOutPath + VALUES_PATH, ARRAYS_FILE_NAME);
        generateArraysXmlFile(arraysFile);

        // put all together, copy app project to WearAppEnv folder to start building apk
        String[] buildApkCmd = new String[]{"bash", "-c", "cd `pwd`/" + outputPath
                + " && ./gradlew build"};
        Runtime run = Runtime.getRuntime();
       final Process pr = run.exec(buildApkCmd);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pr.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        final BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        while (stdoutReader.ready()) {
                            String line = stdoutReader.readLine();
                            System.out.println("stdout: " + line);
                        }

                        try {
                            int exitCode = pr.exitValue();
                            System.out.println("exit code: " + exitCode);
                            // if we get here then the process finished executing
                            break;
                        } catch (IllegalThreadStateException e) {
//                            e.printStackTrace();
                        }
                        // wait 200ms and try again
                        Thread.sleep(200);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

        final BufferedReader stderrReader = new BufferedReader(
                new InputStreamReader(pr.getErrorStream()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        while (stderrReader.ready()) {
                            String line = stderrReader.readLine();
                            System.err.println("stderr: " + line);
                        }
                        try {
                            int exitCode = pr.exitValue();
                            System.out.println("exit code: " + exitCode);
                            // if we get here then the process finished executing
                            break;
                        } catch (IllegalThreadStateException e) {
//                            e.printStackTrace();
                        }
                        // wait 200ms and try again
                        Thread.sleep(200);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();


        // transfer apk back to the phone
    }

    private static void generateArraysXmlFile(File arraysFile) {
        try {
            FileUtils.touch(arraysFile);
            XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
            FileWriter writer = new FileWriter(arraysFile);
            serializer.setOutput(writer);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument("UTF-8", true);
            serializer.comment("\nnote: this file is auto generated by UIWear\n");
            serializer.startTag(null, RESOURCES_TAG);

            // prefs section
            serializer.startTag(null, STRING_ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, PREFS_VALUE);
            for (String pref : prefs) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(pref);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, STRING_ARRAY_TAG);

            // layouts section
            serializer.startTag(null, ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, LAYOUTS_VALUE);
            for (String layout : layouts) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(layout);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, ARRAY_TAG);

            // wear id and index section
            serializer.startTag(null, INTEGER_ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, WEAR_VIEW_ID_ARRAY_VALUE);
            for (String idArray : wearViewIdArray) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(idArray);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, INTEGER_ARRAY_TAG);


            for (String arrayId : wearViewIdIndexMap.keySet()) {
                serializer.startTag(null, ARRAY_TAG);
                serializer.attribute(null, ATTR_NAME, arrayId);
                for (String idArray : wearViewIdIndexMap.get(arrayId)) {
                    serializer.startTag(null, ITEM_TAG);
                    serializer.text(idArray);
                    serializer.endTag(null, ITEM_TAG);
                }
                serializer.endTag(null, ARRAY_TAG);
            }

            // phone id and index section
            serializer.startTag(null, INTEGER_ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, PHONE_VIEW_ID_ARRAY_VALUE);
            for (String idArray : phoneViewIdArray) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(idArray);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, INTEGER_ARRAY_TAG);

            for (String phoneArrayId : phoneViewIdIndexMap.keySet()) {
                serializer.startTag(null, STRING_ARRAY_TAG);
                serializer.attribute(null, ATTR_NAME, phoneArrayId);
                for (String phoneIdArray : phoneViewIdIndexMap.get(phoneArrayId)) {
                    serializer.startTag(null, ITEM_TAG);
                    System.out.println();
                    serializer.text(phoneIdArray);
                    serializer.endTag(null, ITEM_TAG);
                }
                serializer.endTag(null, STRING_ARRAY_TAG);
            }

            //==========item section==========//

            // item binding phone view id section
            serializer.startTag(null, STRING_ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, PHONE_VIEW_IDS_VALUE);
            for (String itemId : phoneItemViewIds) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(itemId);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, STRING_ARRAY_TAG);

            // item layouts section
            serializer.startTag(null, ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, ITEM_LAYOUTS_VALUE);
            for (String layout : itemLayouts) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(layout);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, ARRAY_TAG);

            // item wear id and index section
            serializer.startTag(null, INTEGER_ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, WEAR_ITEM_VIEW_ID_ARRAY_VALUE);
            for (String idArray : wearItemViewIdArray) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(idArray);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, INTEGER_ARRAY_TAG);


            for (String arrayId : wearItemViewIdIndexMap.keySet()) {
                serializer.startTag(null, ARRAY_TAG);
                serializer.attribute(null, ATTR_NAME, arrayId);
                for (String idArray : wearItemViewIdIndexMap.get(arrayId)) {
                    System.out.println();
                    serializer.startTag(null, ITEM_TAG);
                    serializer.text(idArray);
                    serializer.endTag(null, ITEM_TAG);
                }
                serializer.endTag(null, ARRAY_TAG);
            }

            // phone id and index section
            serializer.startTag(null, INTEGER_ARRAY_TAG);
            serializer.attribute(null, ATTR_NAME, PHONE_ITEM_VIEW_ID_ARRAY_VALUE);
            for (String idArray : phoneItemViewIdArray) {
                serializer.startTag(null, ITEM_TAG);
                serializer.text(idArray);
                serializer.endTag(null, ITEM_TAG);
            }
            serializer.endTag(null, INTEGER_ARRAY_TAG);

            for (String phoneArrayId : phoneItemViewIdIndexMap.keySet()) {
                serializer.startTag(null, STRING_ARRAY_TAG);
                serializer.attribute(null, ATTR_NAME, phoneArrayId);
                for (String phoneIdArray : phoneItemViewIdIndexMap.get(phoneArrayId)) {
                    serializer.startTag(null, ITEM_TAG);
                    serializer.text(phoneIdArray);
                    System.out.println();
                    serializer.endTag(null, ITEM_TAG);
                }
                serializer.endTag(null, STRING_ARRAY_TAG);
            }

            serializer.endTag(null, RESOURCES_TAG);
            serializer.endDocument();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    // parse layout, search target node, set node attribute, write back to file
    private static void updateLayout(File layoutFile, HashMap<String, Info> idInfoMap)
            throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(layoutFile));
            Transformer transformer = transformerFactory.newTransformer();

            // search target node and set node attributes
            Set<String> idSet = idInfoMap.keySet();
            for (String wearViewId : idSet) {
                Info info = idInfoMap.get(wearViewId);

                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("*");
                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);

                    Element element = (Element) node;
                    String idAttrValue = element.getAttribute(ID_ATTR_NAME);
                    if (wearViewId.equals(idAttrValue)) {
                        String textAttrValue = info.getText();
                        String imageAttrValue = info.getImage();
                        if (textAttrValue != null) {
                            if (isDebug) {
                                System.out.println("element: " + element.getNodeName());
                                System.out.println("textAttrValue: " + textAttrValue);
                            }
                            element.setAttribute(TEXT_ATTR_NAME, textAttrValue);
                        }
                        if (imageAttrValue != null) {
                            if (isDebug) {
                                System.out.println("element: " + element.getNodeName());
                                System.out.println("imageAttrValue: " + imageAttrValue);
                            }
                            element.setAttribute(IMAGE_ATTR_NAME, imageAttrValue);
                        }

                    }

                    if (idAttrValue != null && idAttrValue.length() > 0) {
                        if (isDebug) {
                            System.out.println("id: " + idAttrValue);
                        }

                    }
                }
            }

            // serialize idMap back to the layoutFile
            transformer.transform(new DOMSource(doc), new StreamResult(new FileWriter(layoutFile)));

        } catch (SAXException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }


}
