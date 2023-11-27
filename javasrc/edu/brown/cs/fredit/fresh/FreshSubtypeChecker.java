/********************************************************************************/
/*                                                                              */
/*              FreshSubtypeChecker.java                                        */
/*                                                                              */
/*      Handle mapping of subtype X subtype                                     */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.fredit.fresh;

import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshSubtypeChecker implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<FreshSubtypeValueImpl,Map<FreshSubtypeValueImpl,CheckData>> result_map;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSubtypeChecker() 
{
   result_map = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

void addMapping(FreshSubtypeValueImpl v1,FreshSubtypeValueImpl v2,ErrorLevel lvl,String msg)
{
   Map<FreshSubtypeValueImpl,CheckData> sm = result_map.get(v1);
   if (sm == null) {
      sm = new HashMap<>();
      result_map.put(v1,sm);
    }
   sm.put(v2,new CheckData(lvl,msg));
}



void addMapping(FreshSubtypeValueImpl v1,FreshSubtypeValueImpl v2,FreshSubtypeValueImpl rv)
{
   Map<FreshSubtypeValueImpl,CheckData> sm = result_map.get(v1);
   if (sm == null) {
      sm = new HashMap<>();
      result_map.put(v1,sm);
    }
   sm.put(v2,new CheckData(rv));
}



void addMapping(FreshSubtypeValueImpl v1,FreshSubtypeValueImpl v2,boolean r)
{
   Map<FreshSubtypeValueImpl,CheckData> sm = result_map.get(v1);
   if (sm == null) {
      sm = new HashMap<>();
      result_map.put(v1,sm);
    }
   sm.put(v2,new CheckData(r));
}



ErrorLevel getErrorLevel(FreshSubtypeValueImpl v1,FreshSubtypeValueImpl v2)
{
   Map<FreshSubtypeValueImpl,CheckData> sm = result_map.get(v1);
   if (sm == null) return null;
   return sm.get(v2).getErrorLevel();
}



String getErrorMessage(FreshSubtypeValueImpl v1,FreshSubtypeValueImpl v2)
{
   Map<FreshSubtypeValueImpl,CheckData> sm = result_map.get(v1);
   if (sm == null) return null;
   return sm.get(v2).getErrorMessage();
}


FreshSubtypeValueImpl getResultValue(FreshSubtypeValueImpl v1,FreshSubtypeValueImpl v2)
{
   Map<FreshSubtypeValueImpl,CheckData> sm = result_map.get(v1);
   if (sm == null) return null;
   return sm.get(v2).getResultValue();
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(String what,IvyXmlWriter xw)
{
   for (FreshSubtypeValueImpl from : result_map.keySet()) {
      Map<FreshSubtypeValueImpl,CheckData> map = result_map.get(from);
      for (FreshSubtypeValueImpl to : map.keySet()) {
         CheckData cd = map.get(to);
         cd.outputXml(what,from,to,xw);
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Data for an error                                                       */
/*                                                                              */
/********************************************************************************/

private static class CheckData {
   
   private ErrorLevel error_level;
   private String error_message;
   private FreshSubtypeValueImpl alt_value;
   private Boolean    bool_value;
   
   CheckData(ErrorLevel lvl,String msg) {
      error_level = lvl;
      error_message = msg;
      alt_value = null;
      bool_value = null;
    }
   
   CheckData(FreshSubtypeValueImpl sv) {
      error_level = null;
      error_message = null;
      alt_value = sv;
      bool_value = null;
    }
   
   CheckData(boolean b) {
      error_level = null;
      error_message = null;
      alt_value = null;
      bool_value = b;
    }
   
   ErrorLevel getErrorLevel()                   { return error_level; }
   String getErrorMessage()                     { return error_message; }
   FreshSubtypeValueImpl getResultValue()               { return alt_value; }
   
   void outputXml(String what,FreshSubtypeValueImpl from,FreshSubtypeValueImpl to,IvyXmlWriter xw) {
      xw.begin(what);
      switch (what) {
         case "MERGE" :
         case "RESTRICT" :
            xw.field("VALUE",from.getName());
            xw.field("WITH",to.getName());
            if (alt_value != null) xw.field("YIELDS",alt_value.getName());
            else {
               xw.field(error_level.toString(),error_message);
             }
            break;
         case "PREDECESSOR" :
            xw.field("CURRENT",from.getName());
            xw.field("PREDECSSOR",to.getName());
            xw.field("RESULT",bool_value);
            break;
       }
      xw.end(what);
    }
   
}       // end of inner class CheckData






}       // end of class FreshSubtypeChecker




/* end of FreshSubtypeChecker.java */

