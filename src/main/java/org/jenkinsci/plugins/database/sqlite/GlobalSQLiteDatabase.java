package org.jenkinsci.plugins.database.sqlite;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.Database;
import org.jenkinsci.plugins.database.DatabaseDescriptor;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.sqlite.JDBC;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * @author Jiri Holusa
 */
public class GlobalSQLiteDatabase extends Database {
    private final File path;

    private transient DataSource source;
    private static final String DB_FILENAME = "db.db";

    @DataBoundConstructor
    public GlobalSQLiteDatabase(File path) {
        this.path = path;
    }

    public File getPath() {
        return path;
    }

    @Override
    public synchronized DataSource getDataSource() throws SQLException {
        if (source==null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(JDBC.class);
            String path = this.path.toURI().toString();
            fac.setUrl(JDBC.PREFIX + path + (path.endsWith("/") ? "" : "/") + DB_FILENAME);
            source = fac.createDataSource();
        }
        return source;
    }

    @Extension
    public static class DescriptorImpl extends DatabaseDescriptor {
        @Override
        public String getDisplayName() {
            return "SQLite global database";
        }

        public FormValidation doCheckPath(@QueryParameter String value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            if (value.length()==0)
                return FormValidation.ok(); // no value entered yet

            if (new File(value, DB_FILENAME).exists())
                return FormValidation.ok("This database already exists.");
            else if (new File(value).isFile())
                return FormValidation.error("%s is a file; must be a directory.", value);
            else
                return FormValidation.ok("This database doesn't exist yet. It will be created.");
        }
    }

    @Initializer(after=InitMilestone.PLUGINS_STARTED)
    public static void setDefaultGlobalDatabase() {
        Jenkins j = Jenkins.getInstance();
        GlobalDatabaseConfiguration gdc = j.getExtensionList(GlobalConfiguration.class).get(GlobalDatabaseConfiguration.class);
        if (gdc != null && gdc.getDatabase() == null) {
            gdc.setDatabase(new GlobalSQLiteDatabase(new File(j.getRootDir(), "global")));
        }
    }
}
