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

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;

class PerfedData implements PerfedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String item_name;
private String item_description;
private String from_file;
private boolean in_project;
private PerfedValues base_values;
private PerfedValues total_values;
private PerfedValues sum_values;
private PerfedData parent_data;
private List<PerfedData> child_data;
private int num_critical;
private boolean is_skipped;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

PerfedData(String name) 
{
   item_name = name;
   base_values = new PerfedValues(null);
   total_values = new PerfedValues(null);
   sum_values = new PerfedValues(null);
   parent_data = null;
   child_data = null;
   num_critical = 0;
   is_skipped = false;
}


PerfedData(Element xml) 
{
   item_name = IvyXml.getAttrString(xml,"NAME");
   item_description = IvyXml.getAttrString(xml,"DESCRIPTION");
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
String getDescription()			{ return item_description; }
PerfedValues getSumValues()			{ return sum_values; }
PerfedValues getBaseValues()			{ return base_values; }
PerfedValues getTotalValues()			{ return total_values; }
int getNumCritical() 			        { return num_critical; }
boolean isSkipped()                             { return is_skipped; }
void setSkipped(boolean fg)                     { is_skipped = fg; }

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
   StringBuffer buf = new StringBuffer();
   buf.append("<html><p><b>");
   buf.append(item_name);
   if (item_description != null) buf.append(item_description);
   buf.append("<p><table>");
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
   return buf.toString();
}

}       // end of class PerfedData




/* end of PerfedData.java */

