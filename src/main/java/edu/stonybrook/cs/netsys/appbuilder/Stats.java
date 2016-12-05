package edu.stonybrook.cs.netsys.appbuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Created by qqcao on 12/5/16.
 *
 * UI Pattern stats
 */
public class Stats {
    private static final String[] PATTERNS = {"BoxInsetLayout", "CardFragment", "CircledImageView",
            "ConfirmationActivity", "DelayedConfirmationView", "DismissOverlayView",
            "GridViewPager", "DotsPageIndicator", "WatchViewStub", "WearableListView", "Others"};

    public static void main(String[] args) {

        String xmlFiles = "src/main/resources/";
        try {
            String filepath = xmlFiles + "layout/wear_page_indicator.xml";
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            String expStr = "//*[contains(local-name(), 'ImageView')]";
            XPathExpression expr = xpath.compile(expStr);

            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            System.out.println("Total of elements : " + nl.getLength());

        } catch (ParserConfigurationException | IOException
                | XPathExpressionException | SAXException e) {
            e.printStackTrace();
        }
    }
}
