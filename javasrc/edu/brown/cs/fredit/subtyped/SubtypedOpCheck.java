/********************************************************************************/
/*                                                                              */
/*              SubtypedOpCheck.java                                            */
/*                                                                              */
/*      Information about operator usage for a subtype                          */
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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

@SuppressWarnings("unused")
class SubtypedOpCheck implements SubtypedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Set<String>     operator_names;
private SubtypedValue   result_value;
private SubtypedValue [] arg_values;
private boolean         and_check;
private SubtypedValue   return_value;
private int             return_arg;
private String          call_name;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SubtypedOpCheck(SubtypedDef sd,Element xml)
{
   String ops = IvyXml.getAttrString(xml,"OPERATOR");
   if (ops == null) operator_names = null;
   else {
      operator_names = new HashSet<>();
      StringTokenizer tok = new StringTokenizer(ops,", \t;");
      while (tok.hasMoreTokens()) {
         operator_names.add(tok.nextToken());
       }
      if (operator_names.isEmpty()) operator_names = null;
    }
   
   and_check = IvyXml.getAttrBool(xml,"AND");
   result_value = sd.getValue(IvyXml.getAttrString(xml,"RESULT"));
   call_name = IvyXml.getAttrString(xml,"METHOD");
   
   String atyps = IvyXml.getAttrString(xml,"ARGS");
   if (atyps == null) {
      arg_values = null;
    }
   else {
      StringTokenizer tok = new StringTokenizer(atyps," \t,;");
      arg_values = new SubtypedValue[tok.countTokens()];
      int i = 0;
      while (tok.hasMoreTokens()) {
         String t = tok.nextToken();
         SubtypedValue uv = null;
         if (t.equals("*") || t.equals("ANY")) ;
         else {
            uv = sd.getValue(t);
          }
         arg_values[i++] = uv;
       }
      if (arg_values.length == 0) arg_values = null;
    }
   
   SubtypedValue any = sd.getValue(IvyXml.getAttrString(xml,"VALUE"));
   if (any != null) {
      if (result_value == null) result_value = any;
      if (arg_values == null) {
         arg_values = new SubtypedValue [] { any };
       }
      else {
         for (int i = 0; i < arg_values.length; ++i) {
            if (arg_values[i] == null) arg_values[i] = any;
          }
       }
    }
   
   String rvl = IvyXml.getAttrString(xml,"RETURN");
   if (rvl != null) {
      return_value = sd.getValue(rvl);
      return_arg = -1;
      if (return_value == null) {
         if (rvl.equals("RESULT")) return_arg = 0;
         else if (rvl.equals("LHS")) return_arg = 1;
         else if (rvl.equals("RHS")) return_arg = 2;
         try {
            return_arg = Integer.parseInt(rvl);
          }
         catch (NumberFormatException e) { 
            return_arg = 0;
          }
       }
    }
}



}      // end of class SubtypedOpCheck




/* end of SubtypedOpCheck.java */

