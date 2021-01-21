//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry.Actions;

import CS2JNet.JavaSupport.util.LocaleSupport;
import CS2JNet.System.StringSupport;
import java.util.Locale;
import RoboZZle.Telemetry.Actions.ISolverCommand;

/**
* Represents a solution solving command of editing program's specific command slot
*/
public final class EditCommand   implements ISolverCommand
{
    /**
    * Only 
    *  {@link #EditCommand}
    *  can start with this prefix
    */
    public static final char Prefix = 'E';
    /**
    * Index of the fuction being edited
    */
    private int __Function;
    public int getFunction() {
        return __Function;
    }

    public void setFunction(int value) {
        __Function = value;
    }

    /**
    * Offset of the command, which is being edited, inside a function
    */
    private int __CommandOffset;
    public int getCommandOffset() {
        return __CommandOffset;
    }

    public void setCommandOffset(int value) {
        __CommandOffset = value;
    }

    /**
    * New command
    */
    private String __NewCommand;
    public String getNewCommand() {
        return __NewCommand;
    }

    public void setNewCommand(String value) {
        __NewCommand = value;
    }

    /**
    * Command before replacement
    */
    private String __OldCommand;
    public String getOldCommand() {
        return __OldCommand;
    }

    public void setOldCommand(String value) {
        __OldCommand = value;
    }

    /**
    * Converts this command to its string representation
    */
    public String toString() {
        try
        {
            String oldCommandString = this.getOldCommand();
            String newCommandString = this.getNewCommand();
            return String.format(LocaleSupport.INVARIANT, StringSupport.CSFmtStrToJFmtStr("E{0}{1}:{2}->{3}"),this.getFunction(),this.getCommandOffset(),oldCommandString,newCommandString);
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


