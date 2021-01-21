//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry;

import android.util.Log;
import CS2JNet.JavaSupport.util.LocaleSupport;
import CS2JNet.System.ArgumentNullException;
import CS2JNet.System.ArgumentOutOfRangeException;
import CS2JNet.System.InvalidOperationException;
import CS2JNet.System.StringSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import RoboZZle.Telemetry.Actions.ISolverCommand;
import RoboZZle.Telemetry.Actions.PlayCommand;
import RoboZZle.Telemetry.SessionLog;
import RoboZZle.Telemetry.SessionLogEntry;

public final class SessionLogWriter   
{
    final SessionLog log;
    public SessionLogWriter(SessionLog log) {
        this.log = log;
        Log.d("ROB AI", String.format(LocaleSupport.INVARIANT, StringSupport.CSFmtStrToJFmtStr("TELEMETRY: session started @ {0}"),String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime())));
    }

    PlayCommand playCommandInProgress;
    public void Log(ISolverCommand command) {
        if (command == null)
            throw new ArgumentNullException("command");
         
        SessionLogEntry entry = new SessionLogEntry();
        entry.setCommand(command.toString());
        this.log.getEntries().add(entry);
        Log.d("ROB AI", String.format(LocaleSupport.INVARIANT, StringSupport.CSFmtStrToJFmtStr("TELEMETRY: {0}"),entry));
    }

    public void LogPlayStart(int currentSteps) {
        if (currentSteps < 0)
            throw new ArgumentOutOfRangeException("currentSteps");
         
        // negative value indicates incomplete play telemetry
        this.playCommandInProgress = new PlayCommand();
        this.playCommandInProgress.setSteps(-currentSteps);
    }

    public void LogPlayEnd(int currentSteps) {
        if (currentSteps < 0)
            throw new ArgumentOutOfRangeException("currentSteps");
         
        if (this.playCommandInProgress == null)
            throw new InvalidOperationException("The last command was not a play command");
         
        if (this.playCommandInProgress.getSteps() > 0)
            throw new InvalidOperationException("The last play command has already been stopped");
         
        if (currentSteps + this.playCommandInProgress.getSteps() < 0)
        {
            String errorMessage = String.format(LocaleSupport.INVARIANT, StringSupport.CSFmtStrToJFmtStr("Value must be greater than, or equal to {0}"),-((long)this.playCommandInProgress.getSteps()));
            throw new ArgumentOutOfRangeException("currentSteps", currentSteps, errorMessage);
        }
         
        this.playCommandInProgress.setSteps(this.playCommandInProgress.getSteps() + currentSteps);
        this.Log(this.playCommandInProgress);
        this.playCommandInProgress = null;
    }

}


