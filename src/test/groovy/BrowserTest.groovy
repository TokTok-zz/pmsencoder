@Typed

package com.chocolatey.pmsencoder

public class BrowserTest extends PMSEncoderTestCase {
    private final String BBC_NEWS = 'http://news.bbc.co.uk/'
    private Browser browser

    public void setUp() {
        browser = new Browser()
    }

    public void testNavigation() {
        browser.navigate(BBC_NEWS)
        assertJsEquals('window.location', BBC_NEWS)
        assertJsEquals('$("title").text()', 'BBC News - Home')
    }

    private void assertJsEquals(String lhs, String rhs) {
        def val = browser.eval(lhs)
        assert val == rhs
    }
}
