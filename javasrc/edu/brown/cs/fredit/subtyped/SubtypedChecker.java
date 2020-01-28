/********************************************************************************/
/*                                                                              */
/*              SubtypedChecker.java                                            */
/*                                                                              */
/*      Handle mapping of subtype X subtype => Error                            */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.fredit.subtyped;

import java.util.HashMap;
import java.util.Map;

class SubtypedChecker implements SubtypedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<SubtypedValue,Map<SubtypedValue,CheckData>> result_map;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SubtypedChecker() 
{
   result_map = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

void addMapping(SubtypedValue v1,SubtypedValue v2,ErrorLevel lvl,String msg)
{
   Map<SubtypedValue,CheckData> sm = result_map.get(v1);
   if (sm == null) {
      sm = new HashMap<>();
      result_map.put(v1,sm);
    }
   sm.put(v2,new CheckData(lvl,msg));
}



void addMapping(SubtypedValue v1,SubtypedValue v2,SubtypedValue rv)
{
   Map<SubtypedValue,CheckData> sm = result_map.get(v1);
   if (sm == null) {
      sm = new HashMap<>();
      result_map.put(v1,sm);
    }
   sm.put(v2,new CheckData(rv));
}



void addMapping(SubtypedValue v1,SubtypedValue v2,boolean r)
{
   Map<SubtypedValue,CheckData> sm = result_map.get(v1);
   if (sm == null) {
      sm = new HashMap<>();
      result_map.put(v1,sm);
    }
   sm.put(v2,new CheckData(r));
}



ErrorLevel getErrorLevel(SubtypedValue v1,SubtypedValue v2)
{
   Map<SubtypedValue,CheckData> sm = result_map.get(v1);
   if (sm == null) return null;
   return sm.get(v2).getErrorLevel();
}



String getErrorMessage(SubtypedValue v1,SubtypedValue v2)
{
   Map<SubtypedValue,CheckData> sm = result_map.get(v1);
   if (sm == null) return null;
   return sm.get(v2).getErrorMessage();
}


SubtypedValue getResultValue(SubtypedValue v1,SubtypedValue v2)
{
   Map<SubtypedValue,CheckData> sm = result_map.get(v1);
   if (sm == null) return null;
   return sm.get(v2).getResultValue();
}




/********************************************************************************/
/*                                                                              */
/*      Data for an error                                                       */
/*                                                                              */
/********************************************************************************/

private static class CheckData {

   private ErrorLevel error_level;
   private String error_message;
   private SubtypedValue alt_value;
   private Boolean    bool_value;
   
   CheckData(ErrorLevel lvl,String msg) {
      error_level = lvl;
      error_message = msg;
      alt_value = null;
      bool_value = null;
    }
   
   CheckData(SubtypedValue sv) {
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
   SubtypedValue getResultValue()               { return alt_value; }
   @SuppressWarnings("unused")
   Boolean getBoolValue()                       { return bool_value; }
   
}       // end of inner class ErrorData





}       // end of class SubtypedChecker




/* end of SubtypedChecker.java */

