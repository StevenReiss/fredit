/********************************************************************************/
/*                                                                              */
/*              PerfedData.java                                                 */
/*                                                                              */
/*      Performance data element                                                */
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



package edu.brown.cs.fredit.perfed;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.fresh.FreshConstants;
import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;

class PerfedData implements PerfedConstants, FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String item_name;
private String item_description;
private String [] arg_types;
private String return_type;
private String from_file;
private boolean in_project;
private PerfedValues base_values;
private PerfedValues total_values;
private PerfedValues sum_values;
private PerfedData parent_data;
private List<PerfedData> child_data;
private int num_critical;
private boolean is_skipped;
private MethodDataKind item_kind;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

PerfedData(String name,MethodDataKind kind) 
{
   item_name = name;
   base_values = new PerfedValues(null);
   total_values = new PerfedValues(null);
   sum_values = new PerfedValues(null);
   parent_data = null;
   child_data = null;
   num_critical = 0;
   is_skipped = false;
   arg_types = null;
   return_type = null;
   item_description = "";
   arg_types = null;
   return_type = null;
   item_kind = kind;
}


PerfedData(Element xml) 
{
   item_name = IvyXml.getAttrString(xml,"NAME");
   if (item_name == null) item_name = "???";
   else {
      int idx = item_name.lastIndexOf(".");
      if (idx > 0) {
         String lnm = item_name.substring(idx+1);
         if (lnm.equals("<init>")) lnm = "<<Constructor>>";
         else if (lnm.equals("<clinit>")) lnm = "<<Static Initializer>>";
         else if (lnm.equals("$$$$clinit$$$$")) lnm = "<<Static Initializer>>";
         else lnm = null;
         if (lnm != null) {
            item_name = item_name.substring(0,idx+1) + lnm;
          }
       }
    }
   item_kind = MethodDataKind.FULL_METHOD;
   arg_types = null;
   return_type = null;
   String description = IvyXml.getAttrString(xml,"DESCRIPTION");
   item_description = description;
   if (description != null && description.charAt(0) == '(') {
      int idx = description.lastIndexOf(")");
      String args = description.substring(1,idx);
      String ret = description.substring(idx+1);
      if (!args.isEmpty()) {
         String argtypes = IvyFormat.formatTypeNames(args,"@");
         arg_types = argtypes.split("@");
         return_type = IvyFormat.formatTypeName(ret,false);
       }
    }
   from_file = IvyXml.getAttrString(xml,"FILE");
   in_project = IvyXml.getAttrBool(xml,"INPROJECT");
   base_values = new PerfedValues(IvyXml.getChild(xml,"BASE"));
   total_values = new PerfedValues(IvyXml.getChild(xml,"TOTAL"));
   sum_values = new PerfedValues(null);
   sum_values.add(total_values);
   parent_data = null;
   child_data = null;
   is_skipped = false;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getName()				{ return item_name; }
String getDescription()                         { return item_description; }
void setDescription(String d)                   { item_description = d; }
PerfedValues getSumValues()			{ return sum_values; }
PerfedValues getBaseValues()			{ return base_values; }
PerfedValues getTotalValues()			{ return total_values; }
int getNumCritical() 			        { return num_critical; }
boolean isSkipped()                             { return is_skipped; }
void setSkipped(boolean fg)                     { is_skipped = fg; }
MethodDataKind getKind()                        { return item_kind; }
PerfedData getParent()                          { return parent_data; }

List<PerfedData> getChidren()			{ return child_data; }

int getNumChildren() 
{
   if (child_data == null) return 0;
   return child_data.size();
}


PerfedData getChild(int i)
{
   if (child_data == null || i < 0 || i >= child_data.size()) return null;
   return child_data.get(i);
}


String getLocalName()
{
   if (parent_data == null) return item_name;
   String pd = parent_data.getName() + ".";
   if (item_name.startsWith(pd)) {
      return item_name.substring(pd.length());
    }
   return item_name;
}



/********************************************************************************/
/*                                                                              */
/*      Tree management methods                                                 */
/*                                                                              */
/********************************************************************************/

void addChild(PerfedData pd,PerfedValues delta) 
{
   IvyLog.logD("PERFED","PreAdd child " + pd + " " + delta + " " + pd.getSumValues() + " TO " + this + " -> " + sum_values);
   
   if (child_data == null) child_data = new ArrayList<>();
   
   if (!child_data.contains(pd)) {
      child_data.add(pd);
      pd.parent_data = this;
    }
   if (delta != null) sum_values.add(delta);
   
   IvyLog.logD("PERFED","Add child " + pd + " " + delta + " " + pd.getSumValues() + " TO " + this + " -> " + sum_values);
}


void addCritical()
{
   ++num_critical;
   if (parent_data != null) parent_data.addCritical();
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   return item_name;
}

String getToolTip()
{
   String cnm = null;
   String mnm = null;
   String pfx = "class";
   int idx = item_name.lastIndexOf(".");
   if (item_description != null && !item_description.isEmpty()) {
      if (idx < 0) {
         mnm = item_name;
       }
      else {
         mnm = item_name.substring(idx+1);
         cnm = item_name.substring(0,idx);
         pfx = "class";
       }
    }
   else {
      cnm = item_name;
      mnm = null;
      if (idx < 0) pfx = "package";
      else if (idx+1 < item_name.length()) {
         char ch = item_name.charAt(idx+1);
         if (Character.isLowerCase(ch)) pfx = "package";
       }
    }
   if (mnm != null) {
      mnm = mnm.replace("<","&lt;");
      mnm = mnm.replace(">","&gt;");
    }

   StringBuffer buf = new StringBuffer();
   buf.append("<html><p>");
   
   buf.append("<table style='border:1px solid black;'><tr>");
   buf.append("<td style='border:1px solid black; border-collapse:collapse;'>");
   
   buf.append("<b><table>");
   if (cnm != null) {
      buf.append("<tr><td>");
      buf.append(pfx);
      buf.append("</td><td>");
      buf.append(cnm);
      buf.append("</td></tr>");
    }
   if (mnm != null) {
      buf.append("<tr><td>Name</td><td>");
      buf.append(mnm);
      buf.append("</td></tr>");
    }
   if (arg_types != null && arg_types.length > 0) {
      buf.append("<tr><td rowspan='");
      buf.append(arg_types.length);
      buf.append("'>Arguments</td><td>");
      buf.append(arg_types[0]);
      buf.append("</td></tr>");
      for (int i = 1; i < arg_types.length; ++i) {
         buf.append("<tr><td>");
         buf.append(arg_types[i]);
         buf.append("</td></tr>");
       }
    }
   if (return_type != null) {
      buf.append("<tr><td>Returns</td><td>");
      buf.append(return_type);
      buf.append("</td></tr>");
    }
   buf.append("</table></b>");
   
   buf.append("</td><td>");
   
   buf.append("<table>");
   if (child_data == null) {
      buf.append("<tr><td>Base Forward Steps</td><td>");
      buf.append(base_values.getNumForward());
      buf.append("</td></tr>");
      buf.append("<tr><td>Base Backward Steps</td><td>");
      buf.append(base_values.getNumBackward());
      buf.append("</td></tr>");
      buf.append("<tr><td>Base Scans</td><td>");
      buf.append(base_values.getNumScan());
      buf.append("</td></tr>");
      buf.append("<tr><td>Called Forward Steps</td><td>");
      buf.append(total_values.getNumForward());
      buf.append("</td></tr>");
      buf.append("<tr><td>Called Backward Steps</td><td>");
      buf.append(total_values.getNumBackward());
      buf.append("</td></tr>");
      buf.append("<tr><td>Called Scans</td><td>");
      buf.append(total_values.getNumScan());
      buf.append("</td></tr>");
    }
   else {
      buf.append("<tr><td>Total Forward Steps</td><td>");
      buf.append(sum_values.getNumForward());
      buf.append("</td></tr>");
      buf.append("<tr><td>Total Backward Steps</td><td>");
      buf.append(sum_values.getNumBackward());
      buf.append("</td></tr>");
      buf.append("<tr><td>Total Scans</td><td>");
      buf.append(sum_values.getNumScan());
      buf.append("</td></tr>");
    }
   buf.append("<tr><td>Flow Critical Steps</td><td>");
   buf.append(num_critical);
   buf.append("</td></tr>");
   buf.append("<tr><td>From File</td><td>");
   buf.append(from_file);
   buf.append("</td></tr>");
   buf.append("<tr><td>Is In Project</td><td>");
   buf.append(in_project);
   buf.append("</td></tr>");
   buf.append("</table>");
   
   buf.append("</td></tr></table>");
   
   return buf.toString();
}

}       // end of class PerfedData




/* end of PerfedData.java */

