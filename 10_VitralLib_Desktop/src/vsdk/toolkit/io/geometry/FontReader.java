//===========================================================================
//=-------------------------------------------------------------------------=
//= Module history:                                                         =
//= - August 15 2006 - Oscar Chavarro: Original base version                =
//===========================================================================

package vsdk.toolkit.io.geometry;
import vsdk.toolkit.environment.geometry.ParametricCurve;
import vsdk.toolkit.io.PersistenceElement;

/**
This is an abstract class to serve as a base in an abstract factory design
pattern that serves to load font file data from TrueType, Type1 and
other common font encoding schema.

The basic functionality that this must provide, is to extract a glyph
from a specified character from a font file.
*/
public abstract class FontReader extends PersistenceElement
{
    public abstract ParametricCurve extractGlyph(
        String fontFile, String characterAndItsContext);
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
