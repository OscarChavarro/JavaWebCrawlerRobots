//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - May 17 2006 - Gina Chiquillo: Original base version                   =
//===========================================================================

package vsdk.toolkit.io;

import vsdk.toolkit.common.VSDKException;

public class XmlException extends VSDKException {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20060502L;

   public XmlException() {
   }

   public XmlException(String message) {
       super(message);
   }

   public XmlException(String message, Throwable cause) {
       super(message, cause);
   }

   public XmlException(Throwable cause) {
       super(cause);
   }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
