package org.jenkinsci.plugins.database.sqlite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import hudson.model.FreeStyleProject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.jenkinsci.plugins.database.PerItemDatabaseConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class PerItemSQLiteDatabaseTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void basics() throws Exception {
        FreeStyleProject p = j.jenkins.createProject(FreeStyleProject.class, "somejob");
        try (PoolingDataSource<?> ds =
                (PoolingDataSource<?>) PerItemDatabaseConfiguration.find().getDataSource(p)) {
            try (Connection con = ds.getConnection()) {
                try (Statement st = con.createStatement()) {
                    st.execute("create table FOO (a int, b int )");
                }
                try (Statement st = con.createStatement()) {
                    st.execute("insert into FOO values (1,2)");
                }
                try (Statement st = con.createStatement()) {
                    try (ResultSet r = st.executeQuery("select b from FOO where a=1")) {
                        r.next();
                        assertThat(r.getInt(1), is(2));
                    }
                }
            }
        }
        System.err.println("XXX " + Arrays.asList(p.getRootDir().list()));
    }
}
