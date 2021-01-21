//
// Translated by CS2J (http://www.cs2j.com): 9/29/2016 7:16:43 PM
//

package RoboZZle.Telemetry;

import CS2JNet.System.StringSupport;
import RoboZZle.Telemetry.TelemetrySource;

public final class TelemetrySource   
{
    private String __Product;
    public String getProduct() {
        return __Product;
    }

    public void setProduct(String value) {
        __Product = value;
    }

    private String __Version;
    public String getVersion() {
        return __Version;
    }

    public void setVersion(String value) {
        __Version = value;
    }

    private boolean __IsTest;
    public boolean getIsTest() {
        return __IsTest;
    }

    public void setIsTest(boolean value) {
        __IsTest = value;
    }

    public boolean equals(Object obj) {
        try
        {
            TelemetrySource other = obj instanceof TelemetrySource ? (TelemetrySource)obj : (TelemetrySource)null;
            if (other == null)
                return false;
             
            return StringSupport.equals(this.getProduct(), other.getProduct()) && StringSupport.equals(this.getVersion(), other.getVersion());
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

    public int hashCode() {
        try
        {
            return this.getProduct().hashCode() * 12122419 ^ this.getVersion().hashCode();
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

}


