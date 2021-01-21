//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry;

import java.util.ArrayList;
import RoboZZle.Telemetry.SessionLog;
import RoboZZle.Telemetry.TelemetrySource;

public final class SolutionTelemetry   
{
    public SolutionTelemetry() {
        this.setSessions(new ArrayList<SessionLog>());
    }

    private int __PuzzleID;
    public int getPuzzleID() {
        return __PuzzleID;
    }

    public void setPuzzleID(int value) {
        __PuzzleID = value;
    }

    private String __StartingProgram;
    public String getStartingProgram() {
        return __StartingProgram;
    }

    public void setStartingProgram(String value) {
        __StartingProgram = value;
    }

    private ArrayList<SessionLog> __Sessions;
    public ArrayList<SessionLog> getSessions() {
        return __Sessions;
    }

    public void setSessions(ArrayList<SessionLog> value) {
        __Sessions = value;
    }

    private TelemetrySource __Source;
    public TelemetrySource getSource() {
        return __Source;
    }

    public void setSource(TelemetrySource value) {
        __Source = value;
    }

}


