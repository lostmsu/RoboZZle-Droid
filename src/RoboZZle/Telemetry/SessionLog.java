//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import RoboZZle.Telemetry.SessionLogEntry;

/**
* Represents puzzle solution editing session log.
*/
public final class SessionLog   
{
    public SessionLog() {
        this.setEntries(new ArrayList<SessionLogEntry>());
        this.setStartTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
    }

    /**
    * ID of the puzzle being edited
    */
    private int __PuzzleID;
    public int getPuzzleID() {
        return __PuzzleID;
    }

    public void setPuzzleID(int value) {
        __PuzzleID = value;
    }

    /**
    * Solution actions
    */
    private ArrayList<SessionLogEntry> __Entries;
    public ArrayList<SessionLogEntry> getEntries() {
        return __Entries;
    }

    public void setEntries(ArrayList<SessionLogEntry> value) {
        __Entries = value;
    }

    /**
    * Session start time
    */
    private Date __StartTime;
    public Date getStartTime() {
        return __StartTime;
    }

    public void setStartTime(Date value) {
        __StartTime = value;
    }

}


