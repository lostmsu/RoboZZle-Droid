//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry;

import java.util.ArrayList;
import RoboZZle.Telemetry.SolutionTelemetry;

public final class TelemetryBag   
{
    public TelemetryBag() {
        this.setSolutions(new ArrayList<SolutionTelemetry>());
    }

    private ArrayList<SolutionTelemetry> __Solutions;
    public ArrayList<SolutionTelemetry> getSolutions() {
        return __Solutions;
    }

    public void setSolutions(ArrayList<SolutionTelemetry> value) {
        __Solutions = value;
    }

}


