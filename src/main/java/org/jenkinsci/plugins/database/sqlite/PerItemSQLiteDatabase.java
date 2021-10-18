package org.jenkinsci.plugins.database.sqlite;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.TopLevelItem;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.sql.DataSource;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.database.BasicDataSource2;
import org.jenkinsci.plugins.database.PerItemDatabase;
import org.jenkinsci.plugins.database.PerItemDatabaseConfiguration;
import org.jenkinsci.plugins.database.PerItemDatabaseDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.sqlite.JDBC;

/**
 * @author Jiri Holusa
 */
public class PerItemSQLiteDatabase extends PerItemDatabase  {
    private transient Map<TopLevelItem,DataSource> sources;
    private static final String DB_FILENAME = "db.db";

    @DataBoundConstructor public PerItemSQLiteDatabase() {
    }

    @Override public DataSource getDataSource(TopLevelItem item) throws SQLException {
        if (sources == null) {
            sources = new WeakHashMap<>();
        }
        DataSource source = sources.get(item);
        if (source == null) {
            BasicDataSource2 fac = new BasicDataSource2();
            fac.setDriverClass(JDBC.class);
            fac.setUrl(JDBC.PREFIX + item.getRootDir().toURI() + DB_FILENAME);
            source = fac.createDataSource();
            sources.put(item, source);
        }
        return source;
    }

    @Extension public static class DescriptorImpl extends PerItemDatabaseDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "SQLite per-item database";
        }

    }

    @Initializer(after = InitMilestone.PLUGINS_STARTED)
    public static void setDefaultPerItemDatabase() {
        Jenkins j = Jenkins.get();
        
        PerItemDatabaseConfiguration pidbc = j.getExtensionList(GlobalConfiguration.class).get(PerItemDatabaseConfiguration.class);
        if (pidbc != null && pidbc.getDatabase() == null) {
            pidbc.setDatabase(new PerItemSQLiteDatabase());
        }
    }

}
