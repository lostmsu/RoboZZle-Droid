//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import RoboZZle.Telemetry.Actions.ISolverCommand;
import RoboZZle.Telemetry.Actions.PauseCommand;

public final class PauseCommand   implements ISolverCommand
{
    public static final char Prefix = '|';
    static PauseCommand instance = new PauseCommand();
    public static PauseCommand getInstance() {
        return instance;
    }

    PauseCommand() {
    }

    public String toString() {
        try
        {
            return "||";
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


