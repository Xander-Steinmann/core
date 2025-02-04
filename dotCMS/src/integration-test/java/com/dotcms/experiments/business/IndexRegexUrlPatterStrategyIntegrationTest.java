package com.dotcms.experiments.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.dotcms.datagen.FolderDataGen;
import com.dotcms.datagen.HTMLPageDataGen;
import com.dotcms.datagen.SiteDataGen;
import com.dotcms.datagen.TemplateDataGen;
import com.dotcms.util.IntegrationTestInitService;
import com.dotmarketing.beans.Host;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.htmlpageasset.model.HTMLPageAsset;
import com.dotmarketing.portlets.templates.model.Template;
import org.junit.BeforeClass;
import org.junit.Test;

public class IndexRegexUrlPatterStrategyIntegrationTest {

    private static String EXPECTED_REGEX = "^(http|https):\\/\\/(localhost|127.0.0.1|\\b(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,})(:\\d{1,5})?\\/%s(\\/[iI][nN][dD][eE][xX]|\\/)?(\\/?\\?.*)?$";

    @BeforeClass
    public static void prepare() throws Exception {
        IntegrationTestInitService.getInstance().init();
    }

    /**
     * Method to test: {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} and
     * {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)}
     * When: The page an index page but not the root Index Page
     * Should: the {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} return true
     * and the {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)} return the correct regex pattern
     * also the regex pattern should match the page url
     *
     * @throws DotDataException
     */
    @Test
    public void realIndexPage() throws DotDataException {
        final Host host = new SiteDataGen().nextPersisted();
        final Folder folder = new FolderDataGen().site(host).nextPersisted();
        final Template template = new TemplateDataGen().nextPersisted();

        final HTMLPageAsset htmlPageAsset = new HTMLPageDataGen(folder, template)
                .pageURL("index")
                .nextPersisted();

        final IndexRegexUrlPatterStrategy indexRegexUrlPatterStrategy = new IndexRegexUrlPatterStrategy();
        final boolean match = indexRegexUrlPatterStrategy.isMatch(htmlPageAsset);
        assertTrue(match);

        assertEquals(String.format(EXPECTED_REGEX, folder.getName()), indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset));

        final String regexPattern = indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset);
        assertTrue(("http://localhost:8080" + folder.getPath() + "index").matches(regexPattern));
        assertTrue(("http://localhost" + folder.getPath() + "index").matches(regexPattern));

        assertFalse(("http://localhost:8080" + folder.getPath() + "index/index").matches(regexPattern));
        assertFalse(("http://localhost" + folder.getPath() + "index/index").matches(regexPattern));

        assertTrue(("http://localhost:8080" + folder.getPath() + "INDEX").matches(regexPattern));
        assertTrue(("http://localhost" + folder.getPath() + "INDEX").matches(regexPattern));

        assertTrue(("http://localhost:8080" + folder.getPath() + "IndEX").matches(regexPattern));
        assertTrue(("http://localhost" +folder.getPath() + "IndEX").matches(regexPattern));

        assertTrue(("http://localhost:8080" + folder.getPath()).matches(regexPattern));
        assertTrue(("http://localhost" + folder.getPath()).matches(regexPattern));

        assertFalse(("http://localhost:8080" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("http://localhost:8080" + folder.getPath() + " aaaa").matches(regexPattern));

        assertFalse(("http://localhost" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("http://localhost" + folder.getPath() +  "aaaa").matches(regexPattern));

        assertFalse(("http://localhost" + folder.getPath() + "xedni").matches(regexPattern));

        assertFalse(("http://localhost:8080" + folder.getPath() + "indexindex").matches(regexPattern));
        assertFalse(("http://localhost" + folder.getPath() + "indexindex").matches(regexPattern));

        final String folderPathWithoutSlash = folder.getPath().substring(0, folder.getPath().length() - 1);
        assertTrue(("http://localhost:8080" + folderPathWithoutSlash).matches(regexPattern));
        assertTrue(("http://localhost" + folderPathWithoutSlash).matches(regexPattern));

        assertFalse(("http://localhost:8080/any_folder/index").matches(regexPattern));
        assertFalse(("http://localhost/any_folder/index").matches(regexPattern));

        assertFalse(("http://localhost:8080/index").matches(regexPattern));
        assertFalse(("http://localhost/index").matches(regexPattern));

        assertFalse(("http://localhost:8080/INDEX").matches(regexPattern));
        assertFalse(("http://localhost/INDEX").matches(regexPattern));

        assertFalse(("http://localhost:8080/IndEX").matches(regexPattern));
        assertFalse(("http://localhost/IndEX").matches(regexPattern));

        assertFalse(("http://localhost:8080/").matches(regexPattern));
        assertFalse(("http://localhost/").matches(regexPattern));

        assertFalse(("http://localhost:8080/aaaa/bbb").matches(regexPattern));
        assertFalse(("http://localhost:8080/aaaa").matches(regexPattern));

        assertFalse(("http://localhost/aaaa/bbb").matches(regexPattern));
        assertFalse(("http://localhost/aaaa").matches(regexPattern));

        assertFalse(("http://localhost/xedni").matches(regexPattern));

        assertFalse(("http://localhost:8080/indexindex").matches(regexPattern));
        assertFalse(("http://localhost/indexindex").matches(regexPattern));

        assertFalse(("http://localhost:8080").matches(regexPattern));
        assertFalse(("http://localhost").matches(regexPattern));
        assertFalse(("http://localhost").matches(regexPattern));

        assertTrue(("http://localhost:8080" + folder.getPath() + "/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
        assertTrue(("http://localhost" + folder.getPath() + "/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));

        assertFalse(("http://localhost:8080/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
        assertFalse(("http://localhost/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
    }

    /**
     * Method to test: {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} and
     * {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)}
     * When: The page is an index page and the protocol is https
     * Should: the {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} return true
     * and the {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)} return the correct regex pattern
     * also the regex pattern should match the page url
     *
     * @throws DotDataException
     */
    @Test
    public void httpsRealRootIndexPage() throws DotDataException {
        final Host host = new SiteDataGen().nextPersisted();
        final Folder folder = new FolderDataGen().site(host).nextPersisted();
        final Template template = new TemplateDataGen().nextPersisted();

        final HTMLPageAsset htmlPageAsset = new HTMLPageDataGen(folder, template)
                .pageURL("index")
                .nextPersisted();

        final IndexRegexUrlPatterStrategy indexRegexUrlPatterStrategy = new IndexRegexUrlPatterStrategy();
        final boolean match = indexRegexUrlPatterStrategy.isMatch(htmlPageAsset);
        assertTrue(match);

        assertEquals(String.format(EXPECTED_REGEX, folder.getName()), indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset));

        final String regexPattern = indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset);
        assertTrue(("https://localhost:8080" + folder.getPath() + "index").matches(regexPattern));
        assertTrue(("https://localhost" + folder.getPath() + "index").matches(regexPattern));

        assertFalse(("https://localhost:8080" + folder.getPath() + "index/index").matches(regexPattern));
        assertFalse(("https://localhost" + folder.getPath() + "index/index").matches(regexPattern));

        assertTrue(("https://localhost:8080" + folder.getPath() + "INDEX").matches(regexPattern));
        assertTrue(("https://localhost" + folder.getPath() + "INDEX").matches(regexPattern));

        assertTrue(("https://localhost:8080" + folder.getPath() + "IndEX").matches(regexPattern));
        assertTrue(("https://localhost" +folder.getPath() + "IndEX").matches(regexPattern));

        assertTrue(("https://localhost:8080" + folder.getPath()).matches(regexPattern));
        assertTrue(("https://localhost" + folder.getPath()).matches(regexPattern));

        assertFalse(("https://localhost:8080" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("https://localhost:8080" + folder.getPath() + " aaaa").matches(regexPattern));

        assertFalse(("https://localhost" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("https://localhost" + folder.getPath() +  "aaaa").matches(regexPattern));

        assertFalse(("https://localhost" + folder.getPath() + "xedni").matches(regexPattern));

        assertFalse(("https://localhost:8080" + folder.getPath() + "indexindex").matches(regexPattern));
        assertFalse(("https://localhost" + folder.getPath() + "indexindex").matches(regexPattern));

        final String folderPathWithoutSlash = folder.getPath().substring(0, folder.getPath().length() - 1);
        assertTrue(("https://localhost:8080" + folderPathWithoutSlash).matches(regexPattern));
        assertTrue(("https://localhost" + folderPathWithoutSlash).matches(regexPattern));

        assertFalse(("https://localhost:8080/any_folder/index").matches(regexPattern));
        assertFalse(("https://localhost/any_folder/index").matches(regexPattern));

        assertFalse(("https://localhost:8080/index").matches(regexPattern));
        assertFalse(("https://localhost/index").matches(regexPattern));

        assertFalse(("https://localhost:8080/INDEX").matches(regexPattern));
        assertFalse(("https://localhost/INDEX").matches(regexPattern));

        assertFalse(("https://localhost:8080/IndEX").matches(regexPattern));
        assertFalse(("https://localhost/IndEX").matches(regexPattern));

        assertFalse(("https://localhost:8080/").matches(regexPattern));
        assertFalse(("https://localhost/").matches(regexPattern));

        assertFalse(("https://localhost:8080/aaaa/bbb").matches(regexPattern));
        assertFalse(("https://localhost:8080/aaaa").matches(regexPattern));

        assertFalse(("https://localhost/aaaa/bbb").matches(regexPattern));
        assertFalse(("https://localhost/aaaa").matches(regexPattern));

        assertFalse(("https://localhost/xedni").matches(regexPattern));

        assertFalse(("https://localhost:8080/indexindex").matches(regexPattern));
        assertFalse(("https://localhost/indexindex").matches(regexPattern));

        assertFalse(("https://localhost:8080").matches(regexPattern));
        assertFalse(("https://localhost").matches(regexPattern));
        assertFalse(("https://localhost").matches(regexPattern));

        assertTrue(("https://localhost:8080" + folder.getPath() + "/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
        assertTrue(("https://localhost" + folder.getPath() + "/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));

        assertFalse(("https://localhost:8080/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
        assertFalse(("https://localhost/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
    }



    /**
     * Method to test: {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} and
     * {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)}
     * When: The page is an index page but not the root Index Page and the Domain is not localhost
     * Should: the {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} return true
     * and the {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)} return the correct regex pattern
     * also the regex pattern should match the page url
     *
     * @throws DotDataException
     */
    @Test
    public void realIndexPageNotLocalhost() throws DotDataException {
        final Host host = new SiteDataGen().nextPersisted();
        final Folder folder = new FolderDataGen().site(host).nextPersisted();
        final Template template = new TemplateDataGen().nextPersisted();

        final HTMLPageAsset htmlPageAsset = new HTMLPageDataGen(folder, template)
                .pageURL("index")
                .nextPersisted();

        final IndexRegexUrlPatterStrategy indexRegexUrlPatterStrategy = new IndexRegexUrlPatterStrategy();
        final boolean match = indexRegexUrlPatterStrategy.isMatch(htmlPageAsset);
        assertTrue(match);

        assertEquals(String.format(EXPECTED_REGEX, folder.getName()), indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset));

        final String regexPattern = indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset);
        assertTrue(("http://demo.dotcms.com:8080" + folder.getPath() + "index").matches(regexPattern));
        assertTrue(("http://demo.dotcms.com" + folder.getPath() + "index").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080" + folder.getPath() + "index/index").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com" + folder.getPath() + "index/index").matches(regexPattern));

        assertTrue(("http://demo.dotcms.com:8080" + folder.getPath() + "INDEX").matches(regexPattern));
        assertTrue(("http://demo.dotcms.com" + folder.getPath() + "INDEX").matches(regexPattern));

        assertTrue(("http://demo.dotcms.com:8080" + folder.getPath() + "IndEX").matches(regexPattern));
        assertTrue(("http://demo.dotcms.com" +folder.getPath() + "IndEX").matches(regexPattern));

        assertTrue(("http://demo.dotcms.com:8080" + folder.getPath()).matches(regexPattern));
        assertTrue(("http://demo.dotcms.com" + folder.getPath()).matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com:8080" + folder.getPath() + " aaaa").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com" + folder.getPath() +  "aaaa").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com" + folder.getPath() + "xedni").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080" + folder.getPath() + "indexindex").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com" + folder.getPath() + "indexindex").matches(regexPattern));

        final String folderPathWithoutSlash = folder.getPath().substring(0, folder.getPath().length() - 1);
        assertTrue(("http://demo.dotcms.com:8080" + folderPathWithoutSlash).matches(regexPattern));
        assertTrue(("http://demo.dotcms.com" + folderPathWithoutSlash).matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/any_folder/index").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/any_folder/index").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/index").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/index").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/INDEX").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/INDEX").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/IndEX").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/IndEX").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/aaaa/bbb").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com:8080/aaaa").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com/aaaa/bbb").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/aaaa").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com/xedni").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/indexindex").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/indexindex").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com").matches(regexPattern));

        assertTrue(("http://demo.dotcms.com:8080" + folder.getPath() + "/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
        assertTrue(("http://demo.dotcms.com" + folder.getPath() + "/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));

        assertFalse(("http://demo.dotcms.com:8080/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
        assertFalse(("http://demo.dotcms.com/?variantName=dotexperiment-fcaef4575a-variant-1&redirect=true").matches(regexPattern));
    }

    /**
     * Method to test: {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} and
     * {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)}
     * When: The page is the an index page and the protocol is https and the Doiman is not Localhost
     * Should: the {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} return true
     * and the {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)} return the correct regex pattern
     * also the regex pattern should match the page url
     *
     * @throws DotDataException
     */
    @Test
    public void httpsRealRootIndexPageNotLocalhost() throws DotDataException {
        final Host host = new SiteDataGen().nextPersisted();
        final Folder folder = new FolderDataGen().site(host).nextPersisted();
        final Template template = new TemplateDataGen().nextPersisted();

        final HTMLPageAsset htmlPageAsset = new HTMLPageDataGen(folder, template)
                .pageURL("index")
                .nextPersisted();

        final IndexRegexUrlPatterStrategy indexRegexUrlPatterStrategy = new IndexRegexUrlPatterStrategy();
        final boolean match = indexRegexUrlPatterStrategy.isMatch(htmlPageAsset);
        assertTrue(match);

        assertEquals(String.format(EXPECTED_REGEX, folder.getName()), indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset));

        final String regexPattern = indexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset);
        assertTrue(("https://demo.dotcms.com:8080" + folder.getPath() + "index").matches(regexPattern));
        assertTrue(("https://demo.dotcms.com" + folder.getPath() + "index").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080" + folder.getPath() + "index/index").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com" + folder.getPath() + "index/index").matches(regexPattern));

        assertTrue(("https://demo.dotcms.com:8080" + folder.getPath() + "INDEX").matches(regexPattern));
        assertTrue(("https://demo.dotcms.com" + folder.getPath() + "INDEX").matches(regexPattern));

        assertTrue(("https://demo.dotcms.com:8080" + folder.getPath() + "IndEX").matches(regexPattern));
        assertTrue(("https://demo.dotcms.com" +folder.getPath() + "IndEX").matches(regexPattern));

        assertTrue(("https://demo.dotcms.com:8080" + folder.getPath()).matches(regexPattern));
        assertTrue(("https://demo.dotcms.com" + folder.getPath()).matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com:8080" + folder.getPath() + " aaaa").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com" + folder.getPath() + "aaaa/bbb").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com" + folder.getPath() +  "aaaa").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com" + folder.getPath() + "xedni").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080" + folder.getPath() + "indexindex").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com" + folder.getPath() + "indexindex").matches(regexPattern));

        final String folderPathWithoutSlash = folder.getPath().substring(0, folder.getPath().length() - 1);
        assertTrue(("https://demo.dotcms.com:8080" + folderPathWithoutSlash).matches(regexPattern));
        assertTrue(("https://demo.dotcms.com" + folderPathWithoutSlash).matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/any_folder/index").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/any_folder/index").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/index").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/index").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/INDEX").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/INDEX").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/IndEX").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/IndEX").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/aaaa/bbb").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com:8080/aaaa").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com/aaaa/bbb").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/aaaa").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com/xedni").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080/indexindex").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com/indexindex").matches(regexPattern));

        assertFalse(("https://demo.dotcms.com:8080").matches(regexPattern));
        assertFalse(("https://locdemo.dotcms.comalhost").matches(regexPattern));
        assertFalse(("https://demo.dotcms.com").matches(regexPattern));
    }

    /**
     * Method to test: {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} and
     * {@link IndexRegexUrlPatterStrategy#getRegexPattern(HTMLPageAsset)}
     * When: The page is not an index page
     * Should: the {@link IndexRegexUrlPatterStrategy#isMatch(HTMLPageAsset)} return false
     * and the regex pattern should not match the page url
     *
     * @throws DotDataException
     */
    @Test
    public void notIndexPage() throws DotDataException {
            final Host host = new SiteDataGen().nextPersisted();
        final Template template = new TemplateDataGen().nextPersisted();
        final Folder folder = new FolderDataGen().site(host).nextPersisted();

        final HTMLPageAsset htmlPageAsset = new HTMLPageDataGen(folder, template)
                .nextPersisted();

        final RootIndexRegexUrlPatterStrategy rootIndexRegexUrlPatterStrategy = new RootIndexRegexUrlPatterStrategy();
        final boolean match = rootIndexRegexUrlPatterStrategy.isMatch(htmlPageAsset);
        assertFalse(match);

        final String regexPattern = rootIndexRegexUrlPatterStrategy.getRegexPattern(htmlPageAsset);

        assertFalse(("http://localhost:8080" + htmlPageAsset.getURI()).matches(regexPattern));
    }
}
