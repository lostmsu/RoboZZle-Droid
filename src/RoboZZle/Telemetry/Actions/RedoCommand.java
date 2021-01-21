//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import RoboZZle.Telemetry.Actions.ISolverCommand;
import RoboZZle.Telemetry.Actions.RedoCommand;

public final class RedoCommand   implements ISolverCommand
{
    public static final char Prefix = 'R';
    static RedoCommand instance = new RedoCommand();
    public static RedoCommand getInstance() {
        return instance;
    }

    RedoCommand() {
    }

    public String toString() {
        try
        {
            return "R";
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


