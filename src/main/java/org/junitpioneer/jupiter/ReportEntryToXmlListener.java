package org.junitpioneer.jupiter;

import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ReportEntryToXmlListener implements TestExecutionListener {

    private final BufferedWriter writer;
    private final Map<String, Map<String, String>> displayNameAndEntries = new HashMap<>();

    public ReportEntryToXmlListener() throws FileNotFoundException {
        this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("stopwatch.xml")));
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        displayNameAndEntries.put(testIdentifier.getDisplayName().replace("()", ""), entry.getKeyValuePairs());
    }

    public void open() {
        open("StopwatchReport");
        newline();
    }

    public void close() {
        displayNameAndEntries.forEach((displayName, reportEntry) -> {
            open(displayName);
            newline();
            reportEntry.forEach((key, value) -> {
                open(key);
                write(value);
                close(key);
            });
            close(displayName);
        });
        close("StopwatchReport");
    }

    private void open(String s) {
        try {
            writer.write("<" + s + ">");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close(String s) {
        try {
            writer.write("</" + s + ">");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String s) {
        try {
            writer.write(s);
            writer.flush();
        } catch (IOException ignored) {
        }
    }

    private void newline() {
        try {
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
