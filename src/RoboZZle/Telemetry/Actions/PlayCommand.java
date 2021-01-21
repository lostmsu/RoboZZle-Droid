//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import CS2JNet.JavaSupport.util.LocaleSupport;
import CS2JNet.System.ArgumentNullException;
import CS2JNet.System.StringSupport;
import java.util.Locale;
import RoboZZle.Telemetry.Actions.ISolverCommand;
import RoboZZle.Telemetry.Actions.PlayCommand;

/**
* Represents a solution solving command of having program executing deterministric number of steps.
*/
public final class PlayCommand   implements ISolverCommand
{
    /**
    * Only 
    *  {@link #PlayCommand}
    *  can start with this prefix in a string representation
    */
    public static final char Prefix = '>';
    /**
    * Number of
    */
    private int __Steps;
    public int getSteps() {
        return __Steps;
    }

    public void setSteps(int value) {
        __Steps = value;
    }

    /**
    * Converts this command to its string representation
    */
    public String toString() {
        try
        {
            return String.format(LocaleSupport.INVARIANT, StringSupport.CSFmtStrToJFmtStr(">{0}"),this.getSteps());
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

    /**
    * Parses 
    *  {@link #PlayCommand}
    *  from its string representation
    */
    public static PlayCommand Parse(String command) {
        if (StringSupport.isNullOrEmpty(command))
            throw new ArgumentNullException("command");
         
        int steps = Integer.parseInt(command.substring(1));
        PlayCommand result = new PlayCommand();
        result.setSteps(steps);
        return result;
    }

    /**
    * Gets hash code for this command
    */
    public int hashCode() {
        try
        {
            return this.getSteps();
        }
        catch (RuntimeException __dummyCatchVar1)
        {
            throw __dummyCatchVar1;
        }
        catch (Exception __dummyCatchVar1)
        {
            throw new RuntimeException(__dummyCatchVar1);
        }
    
    }

    /**
    * Checks if passed object structurally equals to this object.
    */
    public boolean equals(Object obj) {
        try
        {
            PlayCommand otherPlayCommand = obj instanceof PlayCommand ? (PlayCommand)obj : (PlayCommand)null;
            return otherPlayCommand != null && otherPlayCommand.getSteps() == this.getSteps();
        }
        catch (RuntimeException __dummyCatchVar2)
        {
            throw __dummyCatchVar2;
        }
        catch (Exception __dummyCatchVar2)
        {
            throw new RuntimeException(__dummyCatchVar2);
        }
    
    }

}


