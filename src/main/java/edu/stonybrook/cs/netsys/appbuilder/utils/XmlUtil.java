package edu.stonybrook.cs.netsys.appbuilder.utils;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.stonybrook.cs.netsys.appbuilder.data.RuleInfo;

import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.ATTR_NAME;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.RESOURCES_TAG;
import static edu.stonybrook.cs.netsys.appbuilder.data.Constants.STRING_TAG;

/**
 * Created by qqcao on 11/26/16Saturday.
 * <p>
 * XmlUtil for parsing preference files, mapping rules and layout xml files.
 */
public class XmlUtil {
    private static final String IMAGE_TAG = "image";
    private static final String TEXT_TAG = "text";
    private static final String FROM_TAG = "from";
    private static final String TO_TAG = "to";

    public static ArrayList<RuleInfo> parseMappingRule(File mappingRuleFile) {
        ArrayList<RuleInfo> nodes = new ArrayList<>();

        try {
            FileReader stringReader = new FileReader(mappingRuleFile);
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setInput(stringReader);
            parser.nextTag();
            nodes = readNodes(parser);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    private static ArrayList<RuleInfo> readNodes(XmlPullParser parser) {
        ArrayList<RuleInfo> nodes = new ArrayList<>();
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                RuleInfo info = new RuleInfo();

                String from = parser.getAttributeValue(null, FROM_TAG);
                if (from == null) {
                    continue;
                } else {
                    info.setPhoneViewId(from);
                }

                String to = parser.getAttributeValue(null, TO_TAG);
                if (to == null) {
                    System.err.println("has from attribute: " + from
                            + " but no to attribute for view:" + parser.getName());
                    continue;
                } else {
                    info.setWearViewId(to);
                }

                String name = parser.getName();
                if (name.endsWith("ListView") || name.endsWith("RecyclerView")) {
                    info.setListView(true);
                }
                parser.nextTag();
                name = parser.getName();
                if (TEXT_TAG.equals(name)) {
                    String text = readInfo(parser, TEXT_TAG);
//                    System.out.println("text:" + text);
                    info.setTextInfo(text);
                }

                if (IMAGE_TAG.equals(name)) {
                    String image = readInfo(parser, IMAGE_TAG);
//                    System.out.println("image:" + image);
                    info.setImageInfo(image);
                }

//                System.out.println("info: " + info);
                nodes.add(info);

            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    private static String readInfo(XmlPullParser parser, String infoTag) {
        String id = null;
        try {
            parser.require(XmlPullParser.START_TAG, null, infoTag);
            id = readText(parser);
            parser.require(XmlPullParser.END_TAG, null, infoTag);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return id;
    }

    private static String readText(XmlPullParser parser) {
        String result = "";
        try {
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void serializeMapToFile(HashMap<String, String> stringMap, File stringsFile) {
        try {
            FileUtils.touch(stringsFile);
            XmlSerializer serializer = XmlPullParserFactory.newInstance().newSerializer();
            FileWriter writer = new FileWriter(stringsFile);
            serializer.setOutput(writer);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(null, RESOURCES_TAG);
            Set<String> keySet = stringMap.keySet();
            for (String stringName : keySet) {
                String stringValue = stringMap.get(stringName);
                serializer.startTag(null, STRING_TAG);
                serializer.attribute(null, ATTR_NAME, stringName);
                serializer.text(stringValue);
                serializer.endTag(null, STRING_TAG);
            }
            serializer.endTag(null, RESOURCES_TAG);
            serializer.endDocument();

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
}
