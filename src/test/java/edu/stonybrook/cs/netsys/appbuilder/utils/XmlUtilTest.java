package edu.stonybrook.cs.netsys.appbuilder.utils;

import edu.stonybrook.cs.netsys.appbuilder.data.RuleInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by qqcao on 11/26/16Saturday.
 * <p>
 * Tests for XmlUtil
 */
public class XmlUtilTest {

    private ArrayList<RuleInfo> nodes;
    private File ruleFile;

    private ArrayList<RuleInfo> nodes2;
    private File ruleFile2;

    @Before
    public void setUp() throws Exception {
        nodes = new ArrayList<>();
        nodes.add(new RuleInfo("header", "none", "Recently Played", null));
        nodes.add(new RuleInfo("content_list", "com.spotify.music:id/row_view", null, null));
        ruleFile = new File("src/test/resources/recently_played_rule.xml");

        nodes2 = new ArrayList<>();
        nodes2.add(new RuleInfo("title", "com.spotify.music:id/title", "Song title", null));
        nodes2.add(new RuleInfo("text", "com.spotify.music:id/artistAndAlbum", "Album artist", null));
        nodes2.add(new RuleInfo("icon", "com.spotify.music:id/btn_play", null, "btn_play.png"));
        nodes2.add(new RuleInfo("card", "com.spotify.music:id/image", null, "bg_placeholder.png"));
        ruleFile2 = new File("src/test/resources/play_song_rule.xml");
    }

    @After
    public void tearDown() throws Exception {


    }

    @Test
    public void parseMappingRule() throws Exception {
        assertTrue(nodes.equals(XmlUtil.parseMappingRule(ruleFile)));
    }

    @Test
    public void parseMappingRuleTwo() throws Exception {
        assertTrue(nodes2.equals(XmlUtil.parseMappingRule(ruleFile2)));
    }

}