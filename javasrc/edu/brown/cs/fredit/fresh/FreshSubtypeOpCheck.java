/********************************************************************************/
/*                                                                              */
/*              FreshSubtypeOpCheck.java                                        */
/*                                                                              */
/*      description of class                                                    */
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

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshSubtypeOpCheck implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Set<String>     operator_names;
private FreshSubtypeValue   result_value;
private FreshSubtypeValue [] arg_values;
private boolean         and_check;
private FreshSubtypeValue   return_value;
private int             return_arg;
private String          call_name;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSubtypeOpCheck(FreshSubtype sd,Element xml)
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
      arg_values = new FreshSubtypeValue[tok.countTokens()];
      int i = 0;
      while (tok.hasMoreTokens()) {
         String t = tok.nextToken();
         FreshSubtypeValue uv = null;
         if (t.equals("*") || t.equals("ANY")) ;
         else {
            uv = sd.getValue(t);
          }
         arg_values[i++] = uv;
       }
      if (arg_values.length == 0) arg_values = null;
    }
   
   FreshSubtypeValue any = sd.getValue(IvyXml.getAttrString(xml,"VALUE"));
   if (any != null) {
      if (result_value == null) result_value = any;
      if (arg_values == null) {
         arg_values = new FreshSubtypeValue [] { any };
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



/********************************************************************************/
/*                                                                              */
/*      Output Methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw) 
{
   xw.begin("OPERATION");
   
   if (operator_names != null) {
      StringBuffer buf = new StringBuffer();
      for (String nm : operator_names) {
         if (!buf.isEmpty()) buf.append(" ");
         buf.append(nm);
       }
      if (!buf.isEmpty()) xw.field("OPERATOR",buf.toString());
    }
   if (and_check) xw.field("AND",and_check);
   if (result_value != null) {
      xw.field("RESULT",result_value);
      xw.field("VALUE",result_value);
    }
   if (call_name != null) xw.field("METHOD",call_name);
   if (arg_values != null) {
      StringBuffer buf = new StringBuffer();
      for (FreshSubtypeValue val : arg_values) {
         if (!buf.isEmpty()) buf.append(" ");
         if (val == null) buf.append("*");
         else buf.append(val.getName());
       }
      if (!buf.isEmpty()) xw.field("ARGS",buf.toString());
    }
   switch (return_arg) {
      case 0 :
         xw.field("RETURN","RESULT");
         break;
      case 1 :
         xw.field("RETURN","LHS");
         break;
      case 2 :
         xw.field("RETURN","RHS");
         break;
      case -1 :
         break;
      default :
         xw.field("RETURN",return_arg);
         break;
    }
   
   xw.end("OPERATION");
}



}       // end of class FreshSubtypeOpCheck




/* end of FreshSubtypeOpCheck.java */

