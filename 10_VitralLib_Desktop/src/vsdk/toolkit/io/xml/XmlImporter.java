//===========================================================================
package vsdk.toolkit.io.xml;

import java.util.ArrayList;

import vsdk.toolkit.io.PersistenceElement;

/**
This class provides a basic processing capability for a one line of XML code.
*/
public class XmlImporter extends PersistenceElement
{
    public ArrayList<XmlNode> importFromLine(String line)
    {
        return importFromByteArray(line.getBytes());
    }

    /**
    Given a line of text with XML code, this method creates an structured
    set of XmlNodes, with tags and values inside.
    Note that this method also process "empty" data: white space and
    comment as dummy "_NOTAG_" XmlNodes, in order to permit the
    reproduction of original data on filter applications.
    @param arr
    @return 
    */
    public ArrayList<XmlNode> importFromByteArray(byte arr[])
    {
        boolean onString = false;
        StringBuffer currentString = new StringBuffer("");
        StringBuffer notagString = null;

        boolean onTag = false;
        boolean onTagName = true;

        StringBuffer currentIdentifier = new StringBuffer("");

        ArrayList<XmlNode> list;
        list = new ArrayList<XmlNode>();
        int i;
        XmlNode currentNode = null;
        XmlNode notagNode = null;
        int notagStart = -1;
        int currentTagStart = -1;
        int utf8charindex = -1;
        byte utf8parts[] = new byte[2];
        byte currentStringDelimiter = 0;

        for ( i = 0; i < arr.length; i++ ) {
            if ( arr[i] == '\"' ||
                 arr[i] == '\'' ) {
                if ( !onString ) {
                    onString = true;
                    currentString = new StringBuffer("");
                    currentStringDelimiter = arr[i];
                }
                else if ( arr[i] == currentStringDelimiter ) {
                    onString = false;
                    if ( currentNode != null ) {
                        currentNode.addString(currentString.toString());
                    }
                }
                continue;
            }

            if ( onString ) {
                if ( (arr[i] >> 7 ) != 0 ) {
                    utf8charindex++;
                    utf8parts[utf8charindex] = arr[i];
                    if ( utf8charindex >= 1 ) {
                        utf8charindex = -1;
                        currentString.append(buildUtf8Char(utf8parts));
                    }
                }
                else {
                    currentString.append((char)arr[i]);
                }
/*
                if ( i > 0 && (int)arr[i-1] == -61 && (int)arr[i] == -79 ) {
                    // ENIE
                    currentString.append("\u00f1");
                    //currentString.append(""); // poner una enie entre comillas ... no siempre compila
                }
                else if ( (int)arr[i] != -61 ) {
                    currentString.append((char)arr[i]);
                }
*/
            }
            else if ( arr[i] == '<' && !onTag ) {
                if ( notagNode != null ) {
                    //notagNode.sourceStart = currentTagStart;
                    //notagNode.sourceEnd = i-1;
                    //System.out.println("  * Adding " + notagNode);
                    list.add(notagNode);
                    notagNode = null;
                }
                if ( notagString != null && notagStart != -1 ) {
                    notagNode = new XmlNode("_NOTAG_", notagString.toString());
                    notagNode.setSourceEnd(i-1);
                    notagNode.setSourceStart(notagStart);
                    //System.out.println("  * Adding " + notagNode);
                    list.add(notagNode);
                    notagString = null;
                    notagStart = -1;
                    notagNode = null;
                }
                onTag = true;
                onTagName = true;
                currentTagStart = i;
                //System.out.println("  -- Starting tag --");
            }
            else if ( arr[i] == '>' && onTag ) {
                onTag = false;
                if ( currentIdentifier.length() > 0 ) {
                    //System.out.println("  - Identifier: " + currentIdentifier);
                    if ( onTagName ) {
                        if ( currentNode != null ) {
                            //System.out.println("  * Adding " + currentNode);
                            list.add(currentNode);
                        }
                        else {
                            if ( currentNode != null ) {
                                currentNode.addIdentifier(currentIdentifier.toString());
                            }
                        }
                        currentNode = new XmlNode(currentIdentifier.toString());
                    }
                    onTagName=false;
                }
                if ( currentIdentifier.toString().equals("/") ) {
                    currentNode.setClosed(true);
		}

                currentIdentifier = new StringBuffer("");
                currentNode.setSourceStart(currentTagStart);
                currentNode.setSourceEnd(i);

                //System.out.println("  -- Ending tag --");
                //System.out.println("  * Adding " + currentNode);

                list.add(currentNode);
                currentNode = null;
            }
            else if ( !onTag ) {
                //System.out.print("!");
                if ( notagStart == -1 ) {
                    notagStart = i;
                    notagString = new StringBuffer("");
                }
                notagString.append((char)arr[i]);
            }
            else {
                if ( arr[i] == ' ' || arr[i] == '=' ) {
                    if ( currentIdentifier.length() > 0 ) {
                        //System.out.println("  - Identifier: " + currentIdentifier);
                        if ( onTagName ) {
                            if ( currentNode != null ) {
                                //System.out.println("  * Adding " + currentNode);
                                list.add(currentNode);
                            }
                            currentNode = new XmlNode(currentIdentifier.toString());
                        }
                        else {
                            if ( currentNode != null ) {
                                currentNode.addIdentifier(currentIdentifier.toString());
                            }
                        }
                        onTagName=false;
                    }
                    currentIdentifier = new StringBuffer("");
                }
                else {
                    currentIdentifier.append((char)arr[i]);
                    //System.out.println("    . Voy en [" + currentIdentifier + "]");
                }
            }
        }

        if ( currentNode != null ) {
            currentNode.setSourceStart(currentTagStart);
            currentNode.setSourceEnd(i-1);
            //System.out.println("  * Adding " + currentNode);
            list.add(currentNode);
        }

        if ( notagNode != null ) {
            //notagNode.sourceStart = currentTagStart;
            //notagNode.sourceEnd = i-1;
            //System.out.println("  * Adding " + notagNode);
            list.add(notagNode);
        }

        return list;
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
