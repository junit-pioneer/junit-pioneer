package org.junitpioneer.jupiter;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.FileNotFoundException;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class Platformer {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectClass(StopwatchExtensionTests.MethodLevelAnnotationTest.class)
                )
                .build();

        Launcher launcher = LauncherFactory.create();

        // Register a listener of your choice
        ReportEntryToXmlListener listener;
        try {
            listener = new ReportEntryToXmlListener();
            launcher.registerTestExecutionListeners(listener);

            listener.open();
            launcher.execute(request);
            listener.close();
        } catch (FileNotFoundException e) {
            System.out.println("Hello World! File creation failed!");
            e.printStackTrace();
        }
    }
}
