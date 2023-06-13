package com.instaclustr.version;


import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersion() {
        final Version version = Version.parse(new String[]{});
        final Version version2 = Version.parse(new String[]{"name", "buildTime", "gitCommit"});
        final Version version3 = Version.parse(null);
    }
}
