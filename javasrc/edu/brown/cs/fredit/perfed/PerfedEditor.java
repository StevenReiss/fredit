/********************************************************************************/
/*										*/
/*		PerfedEditor.java						*/
/*										*/
/*	Editor panel for performance tuning					*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.fredit.perfed;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingTreeTable;
import edu.brown.cs.ivy.xml.IvyXml;

public class PerfedEditor implements ControllerEditor, PerfedConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ControllerMain	controller_main;
private Element 	performance_result;
private Element 	critical_result;
private PerfData	root_data;
private PerfValues	total_values;
private Map<String,PerfData> perf_map;
private PerformanceModel performance_model;
private PerfTree	tree_table;


private static double	CUTOFF_VALUE = 0.005;		// 0.5%




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public PerfedEditor(ControllerMain cm)
{
   controller_main = cm;
   performance_result = null;
   critical_result = null;
   perf_map = new HashMap<>();

   root_data = new PerfData("*TOTALS*");
   performance_model = new PerformanceModel();

   PerformanceRunner pr = new PerformanceRunner();
   pr.start();
}


/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/



@Override public JComponent getPanel()
{
   if (tree_table == null) tree_table = new PerfTree();

   return new JScrollPane(tree_table);
}




@Override public String getName()
{
   return "Performance";
}



/********************************************************************************/
/*										*/
/*	Editor panel								*/
/*										*/
/********************************************************************************/

private void updatePanel()
{
   if (performance_result == null) return;
   if (critical_result == null) return;

   if (root_data.getChidren() != null) root_data.getChidren().clear();

   total_values = new PerfValues(IvyXml.getChild(performance_result,"TOTALS"));
   for (Element mxml : IvyXml.children(performance_result,"METHOD")) {
      PerfData pd = new PerfData(mxml);
      perf_map.put(pd.getName() + pd.getDescription(),pd);
      addParentData(pd,pd.getBaseValues());
    }

   for (Element cxml : IvyXml.children(critical_result,"METHOD")) {
      String nm = IvyXml.getAttrString(cxml,"NAME");
      String ds = IvyXml.getAttrString(cxml,"DESCRIPTION");
      PerfData pd = perf_map.get(nm+ds);
      if (pd == null) continue;
      pd.addCritical();
    }

   sortChildren(root_data);

   performance_model.rootUpdated();
}



private void addParentData(PerfData pd,PerfValues delta)
{
   String nm = pd.getName();
   int idx = nm.lastIndexOf(".");
   if (idx > 0) {
      String pnm = nm.substring(0,idx);
      PerfData par = perf_map.get(pnm);
      if (par == null) {
	 par = new PerfData(pnm);
	 perf_map.put(pnm,par);
       }
      par.addChild(pd,delta);
      addParentData(par,delta);
    }
   else {
      root_data.addChild(pd,delta);
    }
}



/********************************************************************************/
/*										*/
/*	Sorting methods 							*/
/*										*/
/********************************************************************************/

private void sortChildren(PerfData pd)
{
   List<PerfData> c = pd.getChidren();
   if (c == null || c.size() == 0) return;

   Collections.sort(c,new PerfComparator());

   for (PerfData cd : c) sortChildren(cd);
}




private class PerfComparator implements Comparator<PerfData> {

   @Override public int compare(PerfData pd1,PerfData pd2) {
      int d = pd1.getSumValues().getNumForward() - pd2.getSumValues().getNumForward();
      if (d < 0) return 1;
      if (d > 0) return -1;
      int d1 = pd1.getSumValues().getNumScan() - pd2.getSumValues().getNumScan();
      if (d1 < 0) return 1;
      if (d1 > 0) return -1;
      return pd1.getName().compareTo(pd2.getName());
    }
}



/********************************************************************************/
/*										*/
/*	Thread to get performance data						*/
/*										*/
/********************************************************************************/

private class PerformanceRunner extends Thread {

   PerformanceRunner() {
      super("PerformanceRunner");
    }

   @Override public void run() {
      controller_main.waitForAnalysis();
   
      CommandArgs args = new CommandArgs("CUTOFF",CUTOFF_VALUE);
      Element xml = controller_main.sendFaitReply(controller_main.getSessionId(),
            "PERFORMANCE",args,null);
      System.err.println("FREDIT: Performance result: " + IvyXml.convertXmlToString(xml));
      performance_result = IvyXml.getChild(xml,"PERFORMANCE");
   
      args = new CommandArgs("CUTOFF",CUTOFF_VALUE,"IGNORES","CheckNullness CheckInitialization ");
      Element cxml = controller_main.sendFaitReply(controller_main.getSessionId(),
            "CRITICAL",args,null);
      System.err.println("FREDIT: Critical result: " + IvyXml.convertXmlToString(cxml));
      critical_result = IvyXml.getChild(cxml,"CRITICAL");
      updatePanel();
    }

}	// end of inner class PerformanceRunner



/********************************************************************************/
/*										*/
/*	Performance data							*/
/*										*/
/********************************************************************************/


private static class PerfData {

   private String item_name;
   private String item_description;
   private String from_file;
   private boolean in_project;
   private PerfValues base_values;
   private PerfValues total_values;
   private PerfValues sum_values;
   private PerfData parent_data;
   private List<PerfData> child_data;
   private int num_critical;

   PerfData(String name) {
      item_name = name;
      base_values = new PerfValues(null);
      total_values = new PerfValues(null);
      sum_values = new PerfValues(null);
      parent_data = null;
      child_data = null;
      num_critical = 0;
    }

   PerfData(Element xml) {
      item_name = IvyXml.getAttrString(xml,"NAME");
      item_description = IvyXml.getAttrString(xml,"DESCRIPTION");
      from_file = IvyXml.getAttrString(xml,"FILE");
      in_project = IvyXml.getAttrBool(xml,"INPROJECT");
      base_values = new PerfValues(IvyXml.getChild(xml,"BASE"));
      total_values = new PerfValues(IvyXml.getChild(xml,"TOTAL"));
      sum_values = new PerfValues(null);
      sum_values.add(total_values);
      parent_data = null;
      child_data = null;
    }

   String getName()				{ return item_name; }
   String getDescription()			{ return item_description; }
   PerfValues getSumValues()			{ return sum_values; }
   PerfValues getBaseValues()			{ return base_values; }
   PerfValues getTotalValues()			{ return total_values; }
   int getNumCritical() 			{ return num_critical; }

   List<PerfData> getChidren()			{ return child_data; }

   int getNumChildren() {
      if (child_data == null) return 0;
      return child_data.size();
    }
   PerfData getChild(int i) {
      if (child_data == null || i < 0 || i >= child_data.size()) return null;
      return child_data.get(i);
    }

   void addChild(PerfData pd,PerfValues delta) {
      System.err.println("PERFED: PreAdd child " + pd + " " + delta + " " + pd.getSumValues() + " TO " + this + " -> " + sum_values);

      if (child_data == null) child_data = new ArrayList<>();

      if (!child_data.contains(pd)) {
	 child_data.add(pd);
	 pd.parent_data = this;
       }
      sum_values.add(delta);

      System.err.println("PERFED: Add child " + pd + " " + delta + " " + pd.getSumValues() + " TO " + this + " -> " + sum_values);
    }

   void addCritical() {
      ++num_critical;
      if (parent_data != null) parent_data.addCritical();
    }

   @Override public String toString() {
      return item_name;
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

   String getToolTip() {
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

}	// end of inner class PerfData


private static class PerfValues {

   private int num_forward;
   private int num_backward;
   private int num_scan;

   PerfValues(Element xml) {
      num_forward = IvyXml.getAttrInt(xml,"FORWARD",0);
      num_backward = IvyXml.getAttrInt(xml,"BACKWARD",0);
      num_scan = IvyXml.getAttrInt(xml,"SCANS",0);
    }

   int getNumForward()			{ return num_forward; }
   int getNumBackward() 		{ return num_backward; }
   int getNumScan()			{ return num_scan; }

   void add(PerfValues pv) {
      num_forward += pv.num_forward;
      num_backward += pv.num_backward;
      num_scan += pv.num_scan;
    }

   @Override public String toString() {
      return "(" + num_forward + "," + num_backward + "," + num_scan + ")";
    }

}	// end of inner class PerfValues



/********************************************************************************/
/*										*/
/*	Tree Table								*/
/*										*/
/********************************************************************************/

private class PerfTree extends SwingTreeTable {

   private transient CellDrawer [] cell_drawer;

   private static final long serialVersionUID = 1;


   PerfTree() {
      super(performance_model);
      setPreferredScrollableViewportSize(new Dimension(400,500));
      setRowSelectionAllowed(true);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      cell_drawer = new CellDrawer[performance_model.getColumnCount()];
      JTree jt = getTree();
      jt.setCellRenderer(new TreeCellRenderer());
      setToolTipText("");
      setOpaque(true);
    }

   @Override public TableCellRenderer getCellRenderer(int r,int c) {
      if (cell_drawer[c] == null) {
	 cell_drawer[c] = new CellDrawer(super.getCellRenderer(r,c));
       }
      return cell_drawer[c];
    }

   @Override protected void paintComponent(Graphics g) {
      Color bc = new Color(0xf0a27d);
      Dimension sz = getSize();
      g.setColor(bc);
      g.fillRect(0,0,sz.width,sz.height);

      super.paintComponent(g);
    }

   @Override public String getToolTipText(MouseEvent evt) {
      int row = rowAtPoint(evt.getPoint());
      Object v0 = getValueAt(row,-1);
      if (v0 == null) return null;
      if (v0 instanceof PerfData) {
	 PerfData pd = (PerfData) v0;
	 return pd.getToolTip();
       }
      return "???";

    }

}	// end of inner class PerfTree




private static class CellDrawer implements TableCellRenderer {

   private TableCellRenderer default_renderer;

   CellDrawer(TableCellRenderer dflt) {
      default_renderer = dflt;
    }

   @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,
	 boolean foc,int r, int c) {
      JComponent jc = (JComponent) default_renderer.getTableCellRendererComponent(t,v,sel,foc,r,c);
      jc.setOpaque(false);
      return jc;
    }

}	// end of inner class CellDrawer



private class TreeCellRenderer extends DefaultTreeCellRenderer {

   private static final long serialVersionUID = 1;

   TreeCellRenderer() {
      setBackgroundNonSelectionColor(null);
      setBackground(new Color(0,0,0,0));
    }

   @Override public Component getTreeCellRendererComponent(JTree t,Object v,
	 boolean sel,boolean exp,boolean leaf,int row,boolean hasfocus) {
      Color bkg = null;
      PerfData pd = (PerfData) v;
      if (pd.getNumCritical() > 0)
	 bkg = new Color(255,128,128);
      else if (pd.getTotalValues() == null || total_values == null)
	 bkg = new Color(0,0,0,0);
      else if (pd.getTotalValues().getNumForward() > total_values.getNumForward() / 100)
	 bkg = new Color(128,255,128);
      else
	 bkg = new Color(0,0,0,0);
      setBackground(bkg);

      return super.getTreeCellRendererComponent(t,pd.getLocalName(),sel,exp,leaf,row,hasfocus);
    }

}	// end of inner class TreeCellRenderer




/********************************************************************************/
/*										*/
/*	Performance Model for display						*/
/*										*/
/********************************************************************************/

private static String [] column_names = new String [] {
   "Path", "# Steps", "% Steps", "# Scans", "% Scans", "Critical"
};


private class PerformanceModel extends SwingTreeTable.AbstractTreeTableModel {

   PerformanceModel() {
      super(root_data);
    }

   @Override public Object getChild(Object par,int idx) {
      PerfData pd = (PerfData) par;
      return pd.getChild(idx);
    }

   @Override public int getChildCount(Object par) {
      PerfData pd = (PerfData) par;
      return pd.getNumChildren();
    }

   @Override public int getColumnCount() {
      return column_names.length;
    }

   @Override public String getColumnName(int col) {
      return column_names[col];
    }

   @Override public Object getValueAt(Object node,int col) {
      PerfData pd = (PerfData) node;
      if (pd == null) return "*";
      switch (col) {
         case -1 :
            return pd;
         case 0 :
            return pd.getName();
         case 1 :
            if (pd.getSumValues() == null) return 0;
            return pd.getSumValues().getNumForward();
         case 2 :
            if (pd.getSumValues() == null) return 0;
            int v0 = pd.getSumValues().getNumForward();
            if (total_values == null) return 0;
            int v1 = total_values.getNumForward();
            if (v1 == 0) return 0;
            return 100*v0/v1;
         case 3 :
            if (pd.getSumValues() == null) return 0;
            return pd.getSumValues().getNumScan();
         case 4 :
            if (pd.getSumValues() == null) return 0;
            if (total_values == null) return 0;
            int v2 = pd.getSumValues().getNumScan();
            int v3 = total_values.getNumScan();
            if (v3 == 0) return 0;
            return 100*v2/v3;
         case 5:
            return pd.getNumCritical() > 0;
       }
      return null;
    }

   public void rootUpdated() {
      Object [] path = new Object [] { model_root };
      int nchild = getChildCount(model_root);
      Object [] chld = new Object [nchild];
      int [] cidx = new int[nchild];
      for (int i = 0; i < nchild; ++i) {
	 cidx[i] = i;
	 chld[i] = getChild(model_root,i);
       }
      fireTreeStructureChanged(PerfedEditor.this,path,cidx,chld);
      for (int i = 0; i < nchild; ++i) nodeChanged(path,chld[i]);
    }

   private void nodeChanged(Object [] path,Object node) {
      Object [] npath = new Object[path.length+1];
      for (int i = 0; i < path.length; ++i) npath[i] = path[i];
      npath[path.length] = node;
      int nchild = getChildCount(node);
      Object [] chld = new Object[nchild];
      int [] cidx = new int[nchild];
      for (int i = 0; i < nchild; ++i) {
	 cidx[i] = i;
	 chld[i] = getChild(node,i);
       }
      fireTreeStructureChanged(PerfedEditor.this,npath,cidx,chld);
      for (int i = 0; i < nchild; ++i) nodeChanged(npath,chld[i]);
    }

}	// end of inner class PerformanceModel



}	// end of class PerfedEditor




/* end of PerfedEditor.java */

