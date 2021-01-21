//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry;

import CS2JNet.JavaSupport.util.LocaleSupport;
import CS2JNet.System.StringSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
* Represents user action during puzzle solution session
*/
public final class SessionLogEntry   
{
    public SessionLogEntry() {
        this.setTimeStamp(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
    }

    /**
    * Command, executed by user
    */
    private String __Command;
    public String getCommand() {
        return __Command;
    }

    public void setCommand(String value) {
        __Command = value;
    }

    /**
    * Time, when command was executed
    */
    private Date __TimeStamp;
    public Date getTimeStamp() {
        return __TimeStamp;
    }

    public void setTimeStamp(Date value) {
        __TimeStamp = value;
    }

    /**
    * Converts this object to its string representation.
    */
    public String toString() {
        try
        {
            return String.format(LocaleSupport.INVARIANT, StringSupport.CSFmtStrToJFmtStr("{0}=>{1}"),this.getTimeStamp(),this.getCommand());
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


