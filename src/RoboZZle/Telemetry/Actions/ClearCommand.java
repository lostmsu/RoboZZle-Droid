//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import RoboZZle.Telemetry.Actions.ClearCommand;
import RoboZZle.Telemetry.Actions.ISolverCommand;

public final class ClearCommand   implements ISolverCommand
{
    public static final char Prefix = '_';
    static final ClearCommand instance = new ClearCommand();
    public static ClearCommand getInstance() {
        return instance;
    }

    ClearCommand() {
    }

    public String toString() {
        try
        {
            return Prefix + "";
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


