package org.jenkinsci.plugins.database.sqlite;

import hudson.model.FreeStyleProject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.PoolingDataSource;
import static org.hamcrest.CoreMatchers.is;
import org.jenkinsci.plugins.database.PerItemDatabaseConfiguration;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

public class PerItemSQLiteDatabaseTest {
    
    @Rule public JenkinsRule j = new JenkinsRule();

    @Test public void basics() throws Exception {
        FreeStyleProject p = j.jenkins.createProject(FreeStyleProject.class, "somejob");
        DataSource ds = PerItemDatabaseConfiguration.find().getDataSource(p);
        Connection con = ds.getConnection();
        try {
            Statement st = con.createStatement();
            try {
                st.execute("create table FOO (a int, b int )");
            } finally {
                st.close();
            }
            st = con.createStatement();
            try {
                st.execute("insert into FOO values (1,2)");
            } finally {
                st.close();
            }
            st = con.createStatement();
            try {
                ResultSet r = st.executeQuery("select b from FOO where a=1");
                try {
                    r.next();
                    assertThat(r.getInt(1), is(2));
                } finally {
                    r.close();
                }
            } finally {
                st.close();
            }
        } finally {
            con.close();
            ((PoolingDataSource) ds).close();
        }
        System.err.println("XXX " + Arrays.asList(p.getRootDir().list()));
    }

}
