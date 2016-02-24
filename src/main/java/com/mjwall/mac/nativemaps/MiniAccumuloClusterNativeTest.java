package com.mjwall.mac.nativemaps;

import java.io.File;
import com.google.common.io.Files;
import java.io.IOException;
import java.util.HashMap;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.minicluster.MiniAccumuloCluster;
import org.apache.accumulo.minicluster.MiniAccumuloConfig;
import org.apache.accumulo.core.util.shell.Shell;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MiniAccumuloClusterNativeTest {

    private final static Logger LOG = LogManager.getLogger(MiniAccumuloClusterNativeTest.class);

    public void startShell() {
        
        String rootPassword = "rootPassword";
        File tmpDir = null;
        MiniAccumuloCluster accumulo = null;
        Shell shell = null;
        
        try {
            tmpDir = Files.createTempDir();
            tmpDir.deleteOnExit();
            
            MiniAccumuloConfig config = new MiniAccumuloConfig(tmpDir, rootPassword);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Property.TSERV_NATIVEMAP_ENABLED.getKey(), "true");
            config.setSiteConfig(map);
            
            // setup the native maps
            String nativeMapPath = System.getenv("LD_LIBRARY_PATH"); //both LD and DLYIB are set to the same
            config.setNativeLibPaths(nativeMapPath);

            accumulo = new MiniAccumuloCluster(config);
            
            accumulo.start();
            Thread.sleep(5000); // give everything a chance to come up

            LOG.info("MAC temp folder is " + tmpDir.getAbsolutePath());
            LOG.info("Go there to look at logs");

            String[] args = new String[] {"-u", "root", "-p", rootPassword, "-z",
                accumulo.getInstanceName(), accumulo.getZooKeepers()};
            shell = new Shell();
            shell.config(args);            
            shell.start();
        } catch (IOException | InterruptedException e) {
            LOG.error("Something went wrong.", e);
        } finally {
            if (null != shell) {
                shell.shutdown();
            }
            if (null != accumulo) {
                try {
                    accumulo.stop();
                } catch (Exception e) {
                    System.err.println("Error stopping MiniAccumuloCluster: " + e.getMessage());
                }
            }
            if (null != tmpDir) {
                boolean maybe = tmpDir.delete();
                LOG.info("Deleted " + tmpDir + " " +  maybe);
            }

        }
    }
    
    public static void main(String args[]) {
        MiniAccumuloClusterNativeTest test = new MiniAccumuloClusterNativeTest();
        test.startShell();
    }

}
