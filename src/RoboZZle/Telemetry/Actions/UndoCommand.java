//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import RoboZZle.Telemetry.Actions.ISolverCommand;
import RoboZZle.Telemetry.Actions.UndoCommand;

public final class UndoCommand   implements ISolverCommand
{
    public static final char Prefix = 'U';
    static UndoCommand instance = new UndoCommand();
    public static UndoCommand getInstance() {
        return instance;
    }

    UndoCommand() {
    }

    public String toString() {
        try
        {
            return "U";
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


