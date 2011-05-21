/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.grass.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runner for GRASS commands.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GrassRunner implements GrassRunnerListener {
    private List<GrassRunnerListener> listeners = new ArrayList<GrassRunnerListener>();
    private String tmpMapset;
    private File tmpGisrc;
    private final boolean isLatLong;
    private final PrintStream outputStream;
    private final PrintStream errorStream;
    private StringBuffer outSb = new StringBuffer();
    private StringBuffer errSb = new StringBuffer();

    public GrassRunner( final PrintStream outputStream, final PrintStream errorStream, boolean isLatLong ) {
        this.outputStream = outputStream;
        this.errorStream = errorStream;
        this.isLatLong = isLatLong;
    }

    public String runModule( String[] cmdArgs ) throws Exception {
        addListener(this);

        String gisbaseProperty = System.getProperty(GrassUtils.GRASS_ENVIRONMENT_GISBASE_KEY);
        if (gisbaseProperty == null || !new File(gisbaseProperty).exists()) {
            throw new IllegalArgumentException("GISBASE environment not set.");
        }

        final boolean[] outputDone = {false};
        final boolean[] errorDone = {false};

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        String homeDir = System.getProperty("java.home");
        processBuilder.directory(new File(homeDir));
        Map<String, String> environment = processBuilder.environment();

        tmpGisrc = File.createTempFile(GrassUtils.TMP_PREFIX, ".gisrc");
        environment.put("GISBASE", gisbaseProperty);
        environment.put("GISRC", tmpGisrc.getAbsolutePath());
        environment.put("LD_LIBRARY_PATH", gisbaseProperty + "/lib");

        if (GrassUtils.isWindows()) {
            environment.put("PATH", "%PATH%;%GISBASE%/bin:%GISBASE%/scripts");
        } else {
            environment.put("PATH", "$PATH:$GISBASE/bin:$GISBASE/scripts");
        }

        tmpMapset = GrassUtils.createTemporaryMapsetName();
        GrassUtils.createTemporaryMapset(tmpMapset, isLatLong);

        File tmpMapsetFile = new File(tmpMapset);
        File tmpLocationFile = tmpMapsetFile.getParentFile();
        File tmpGrassdbFile = tmpLocationFile.getParentFile();

        BufferedWriter gisRcWriter = new BufferedWriter(new FileWriter(tmpGisrc));
        gisRcWriter.write("GISDBASE: " + tmpGrassdbFile.getAbsolutePath() + "\n");
        gisRcWriter.write("LOCATION_NAME: " + tmpLocationFile.getName() + "\n");
        gisRcWriter.write("MAPSET: " + tmpMapsetFile.getName() + "\n");
        gisRcWriter.write("GRASS_GUI: text\n");
        gisRcWriter.close();

        final Process process = processBuilder.start();

        Thread outputThread = new Thread(){
            public void run() {
                try {
                    InputStream is = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while( (line = br.readLine()) != null ) {
                        print(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    printErr(e.getLocalizedMessage());
                } finally {
                    outputDone[0] = true;
                    for( GrassRunnerListener listener : listeners ) {
                        listener.processfinished(tmpMapset);
                    }
                }
            }
        };

        Thread errorThread = new Thread(){
            public void run() {
                try {
                    InputStream is = process.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while( (line = br.readLine()) != null ) {
                        print(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    printErr(e.getLocalizedMessage());
                } finally {
                    errorDone[0] = true;
                }
            };
        };

        outputThread.start();
        errorThread.start();

        /*
         * if streams were provided, then the user doesn't expect 
         * a sync result and the text of it.
         */
        if (outputStream == null) {
            while( !outputDone[0] && !errorDone[0] ) {
                Thread.sleep(100);
            }

            if (errSb.length() != 0) {
                return errSb.toString();
            } else {
                return outSb.toString();
            }
        }

        return null;
    }

    public void addListener( GrassRunnerListener listener ) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener( GrassRunnerListener listener ) {
        listeners.remove(listener);
    }

    private void print( String line ) {
        if (outputStream != null) {
            outputStream.append(line).append("\n");
        } else {
            outSb.append(line).append("\n");
        }
    };

    private void printErr( String line ) {
        if (errorStream != null) {
            errorStream.append(line).append("\n");
        } else {
            errSb.append(line).append("\n");
        }
    };

    @Override
    public void processfinished( String mapsetFolder ) {
        GrassUtils.deleteTempMapset(mapsetFolder);
    }
}
