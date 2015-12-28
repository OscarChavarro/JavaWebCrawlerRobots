package databaseMongo.model;

//===========================================================================

//Java basic classes
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

//Java JDBC classes
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

//VSDK classes
import vsdk.toolkit.common.Entity;

/**
*/
public class JdbcEntity extends Entity {
  @SuppressWarnings("FieldNameHidesFieldInSuperclass")
  public static final long serialVersionUID = 20150425L;

  private String getStringValue(String t, String n)
  {
      try {
          Class c = this.getClass();

          char f;
          f = n.charAt(0);
          f = Character.toUpperCase(f);
          String name = "get" + f + n.substring(1);

          Method m = c.getMethod(name);

          Object r;
          r = m.invoke(this);

          return "" + r;
      }
      catch ( SecurityException ex ) {
      }
      catch ( IllegalArgumentException ex ) {
      }
      catch ( NoSuchMethodException ex ) {
      }
      catch ( IllegalAccessException ex ) {
      }
      catch ( InvocationTargetException ex ) {
      }
      return "null";
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
      super.clone();
      return null;
  }

  public BasicDBObject exportMongoDocument() {
      BasicDBObject d;
      
      d = new BasicDBObject();

      ArrayList<String> encapsulatedFields;

      encapsulatedFields = this.getEncapsulatedVariables();

      int i;
      for ( i = 0; i < encapsulatedFields.size(); i++ ) {
          String l = encapsulatedFields.get(i);
          StringTokenizer parser;
          parser = new StringTokenizer(l, ":");

          String t = parser.nextToken();
          String n = parser.nextToken();
          String v = getStringValue(t, n);
          
          if ( t.equals("int") ) {
              d.append(n, Integer.parseInt(v));
          }
          else if ( t.equals("double") ) {
              d.append(n, Double.parseDouble(v));
          }
          else {
              d.append(n, v);
          }
      }
      
      return d;
  }

  public String exportMysqlValues()
  {
      String msg;

      ArrayList<String> encapsulatedFields;

      encapsulatedFields = this.getEncapsulatedVariables();

      int i;
      String tables = "(";
      String values = "(";
      for ( i = 0; i < encapsulatedFields.size(); i++ ) {
          StringTokenizer parser;
          parser = new StringTokenizer(encapsulatedFields.get(i), ":");

          String t = parser.nextToken();
          String n = parser.nextToken();

          if ( n.equals("id") ) {
              continue;
          }

          String v = getStringValue(t, n);

          tables += "`" + n + "`";
          values += "\'" + v + "\'";
          if ( i < encapsulatedFields.size() - 1 ) {
              tables += ", ";
              values += ", ";
          }

      }
      tables += ")";
      values += ")";

      msg = tables + " VALUES " + values + ";";

      return msg;
  }

  public Object importFromJdbcRow(ResultSet rs)
  {
      Object other;
      try {
          //----------------------------------------------------------------------
          Class c;
          String t;
          int i;

          c = this.getClass();
          other = c.newInstance();

          //----------------------------------------------------------------------
          ArrayList<String> encapsulatedFields;

          encapsulatedFields = this.getEncapsulatedVariables();

          for ( i = 0; i< encapsulatedFields.size(); i++ ) {
              String f;
              f = encapsulatedFields.get(i);
              StringTokenizer parser = new StringTokenizer(f, ":");
              t = parser.nextToken();

              String varname = parser.nextToken();
              String first = varname.substring(0, 1).toUpperCase();
              String name = "set" + first + varname.substring(1);

              Method m;

              try {
                  if ( t.equals("long") ) {
                      m = other.getClass().getMethod(name, long.class);
                      m.invoke(other, rs.getLong(varname));
                  }
                  else if ( t.equals("int") ) {
                      m = other.getClass().getMethod(name, int.class);
                      m.invoke(other, rs.getInt(varname));
                  }
                  else if ( t.equals("float") ) {
                      m = other.getClass().getMethod(name, float.class);
                      m.invoke(other, rs.getFloat(varname));
                  }
                  else if ( t.equals("double") ) {
                      m = other.getClass().getMethod(name, double.class);
                      m.invoke(other, rs.getDouble(varname));
                  }
                  else if ( t.equals("byte") ) {
                      m = other.getClass().getMethod(name, byte.class);
                      m.invoke(other, rs.getByte(varname));
                  }
                  else if ( t.equals("boolean") ) {
                      m = other.getClass().getMethod(name, boolean.class);
                      m.invoke(other, rs.getBoolean(varname));
                  }
                  else if ( t.equals("char") ) {
                      m = other.getClass().getMethod(name, char.class);
                      m.invoke(other, (char)rs.getByte(varname));
                  }
                  else if ( t.equals("short") ) {
                      m = other.getClass().getMethod(name, short.class);
                      m.invoke(other, rs.getShort(varname));
                  }
                  else if ( t.equals("java.lang.String") ) {
                      m = other.getClass().getMethod(name, String.class);
                      m.invoke(other, rs.getString(varname));
                  }
              }
              catch ( NoSuchMethodException ex ) {
              }
              catch ( SecurityException ex ) {
              }
              catch ( SQLException ex ) {
              }
              catch ( IllegalArgumentException ex ) {
              }
              catch ( InvocationTargetException ex ) {
              }
          }

          //----------------------------------------------------------------------
          Field attributes[] = c.getFields();

          for ( i = 0; i < attributes.length; i++ ) {
              Field a;

              a = attributes[i];
              t = a.getType().getName();

              try {
                  if ( t.equals("long") ) {
                      a.setLong(other, rs.getLong(a.getName()));
                  }
                  else if ( t.equals("int") ) {
                      a.setInt(other, rs.getInt(a.getName()));
                  }
                  else if ( t.equals("float") ) {
                      a.setFloat(other, rs.getFloat(a.getName()));
                  }
                  else if ( t.equals("double") ) {
                      a.setDouble(other, rs.getDouble(a.getName()));
                  }
                  else if ( t.equals("byte") ) {
                      a.setByte(other, rs.getByte(a.getName()));
                  }
                  else if ( t.equals("boolean") ) {
                      a.setBoolean(other, rs.getBoolean(a.getName()));
                  }
                  else if ( t.equals("char") ) {
                      a.setChar(other, (char)rs.getByte(a.getName()));
                  }
                  else if ( t.equals("short") ) {
                      a.setShort(other, rs.getShort(a.getName()));
                  }
                  else if ( t.equals("java.lang.String") ) {
                      a.set(other, rs.getString(a.getName()));
                  }
              }
              catch ( SQLException ex ) {
                  // Variable not found in database, just skip
              }

          }
      }
      catch ( InstantiationException ex ) {
          return null;
      }
      catch ( IllegalAccessException ex ) {
          return null;
      }

      return other;
  }

  public int getNumberOfFields()
  {
      Class c = this.getClass();
      Field attributes[] = c.getFields();
      return attributes.length;
  }

  /**
  Designed for filling definitions needed for a JTable.
  @param inSource
  @param inOutData
  @param inOutColumnNames

  PRE: inOutColumnNames are created an has a size of
  <code>getNumberOfFields();</code> and inOutData is 1.
  */
  public void exportToTableDefinition(
      ArrayList<Object> inSource,
      Object[][] inOutData,
      String[] inOutColumnNames)
  {

      Object o = inSource.get(0);
      Class c = o.getClass();
      Field attributes[] = c.getFields();
      int i;

      for ( i = 0; i < attributes.length; i++ ) {
          Field a;

          a = attributes[i];
          String t;
          t = a.getType().getName();
          String val = "<empty>";
          try {
              if ( t.equals("long") ) {
                  val = "" + a.getLong(o);
              }
              else if ( t.equals("int") ) {
                  val = "" + a.getInt(o);
              }
              else if ( t.equals("float") ) {
                  val = "" + a.getFloat(o);
              }
              else if ( t.equals("double") ) {
                  val = "" + a.getDouble(o);
              }
              else if ( t.equals("byte") ) {
                  val = "" + a.getByte(o);
              }
              else if ( t.equals("boolean") ) {
                  val = "" + a.getBoolean(o);
              }
              else if ( t.equals("char") ) {
                  val = "" + a.getChar(o);
              }
              else if ( t.equals("short") ) {
                  val = "" + a.getShort(o);
              }
              else if ( t.equals("java.lang.String") ) {
                  val = "" + a.get(o);
              }
          }
          catch ( IllegalArgumentException ex ) {
              val = "ERROR argument";
          }
          catch ( IllegalAccessException ex ) {
              val = "ERROR access";
          }

          inOutColumnNames[i] = a.getName();
          if ( inOutData[0] == null ) {
              inOutData[0] = new Object[attributes.length];
          }
          inOutData[0][i] = val;
      }
  }

  public void importMongoFields(DBObject ei) {
      ArrayList<String> encapsulatedFields;

      encapsulatedFields = this.getEncapsulatedVariables();

      int i;
      for ( i = 0; i < encapsulatedFields.size(); i++ ) {
          String l = encapsulatedFields.get(i);
          StringTokenizer parser;
          parser = new StringTokenizer(l, ":");

          String t = parser.nextToken();
          String varname = parser.nextToken();
          
          Object o;
          
          if ( ei.containsField(varname) ) {
              o = ei.get(varname);
              
              if ( o == null ) {
                  continue;
              }
              
              String first = varname.substring(0, 1).toUpperCase();
              String name = "set" + first + varname.substring(1);

              Method m;

              try {
                  if ( t.equals("long") ) {
                      m = this.getClass().getMethod(name, long.class);
                      m.invoke(this, (long)o);
                  }
                  else if ( t.equals("int") ) {
                      m = this.getClass().getMethod(name, int.class);
                      m.invoke(this, (int)o);
                  }
                  else if ( t.equals("float") ) {
                      m = this.getClass().getMethod(name, float.class);
                      m.invoke(this, (float)o);
                  }
                  else if ( t.equals("double") ) {
                      m = this.getClass().getMethod(name, double.class);
                      m.invoke(this, (double)o);
                  }
                  else if ( t.equals("byte") ) {
                      m = this.getClass().getMethod(name, byte.class);
                      m.invoke(this, (byte)o);
                  }
                  else if ( t.equals("boolean") ) {
                      m = this.getClass().getMethod(name, boolean.class);
                      m.invoke(this, (boolean)o);
                  }
                  else if ( t.equals("char") ) {
                      m = this.getClass().getMethod(name, char.class);
                      m.invoke(this, (char)o);
                  }
                  else if ( t.equals("short") ) {
                      m = this.getClass().getMethod(name, short.class);
                      m.invoke(this, (short)o);
                  }
                  else if ( t.equals("java.lang.String") ) {
                      m = this.getClass().getMethod(name, String.class);
                      m.invoke(this, o.toString());
                  }
              }
              catch ( NoSuchMethodException ex ) {
              }
              catch ( SecurityException ex ) {
              }
              catch ( IllegalArgumentException ex ) {
              }
              catch ( InvocationTargetException ex ) {
              } 
              catch ( IllegalAccessException ex ) {
              }
      
          }
      }
  }

}

//===========================================================================
//= EOF                                                                     =
//===========================================================================

