package com.team242.robozzle.telemetry;

import RoboZZle.Telemetry.*;

/**
 * Created by lost on 10/25/2015.
 */
public class TelemetryClientTest {
    public String serialize() {
        TelemetryClient client = new TelemetryClient();

        SessionLogEntry sessionEntry = new SessionLogEntry();
        sessionEntry.setCommand(">");

        SessionLog session = new SessionLog();
        session.setPuzzleID(42);
        session.getEntries().add(sessionEntry);

        TelemetrySource source = new TelemetrySource();
        source.setProduct("RDroid");
        source.setVersion("0.1");

        SolutionTelemetry solution = new SolutionTelemetry();
        solution.setSource(source);
        solution.setPuzzleID(42);
        solution.setStartingProgram("||");
        solution.getSessions().add(session);

        TelemetryBag telemetry = new TelemetryBag();
        telemetry.getSolutions().add(solution);

        String serializedTelemetry = client.serialize(telemetry);
        return serializedTelemetry;
    }
}