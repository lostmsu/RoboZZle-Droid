//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import RoboZZle.Telemetry.Actions.ISolverCommand;
import RoboZZle.Telemetry.Actions.StopCommand;

public final class StopCommand   implements ISolverCommand
{
    public static final char Prefix = '=';
    static final StopCommand instance = new StopCommand();
    public static StopCommand getInstance() {
        return instance;
    }

    StopCommand() {
    }

    public String toString() {
        try
        {
            return "=";
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }
    
    }

}


