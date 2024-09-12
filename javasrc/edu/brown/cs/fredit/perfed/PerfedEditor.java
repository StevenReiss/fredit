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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSkipItem;
import edu.brown.cs.fredit.fresh.FreshConstants.MethodDataKind;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.ivy.swing.SwingCheckBox;
import edu.brown.cs.ivy.swing.SwingColorSet;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingRangeSlider;
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
private PerfedData	root_data;
private PerfedValues	total_values;
private Map<String,PerfedData> perf_map;
private PerformanceModel performance_model;
private PerfTreeTable	tree_table;
private Object          tree_expansions;

private double          cutoff_value;
private double          high_value;
private boolean         show_critical;
private boolean         show_ignored;

private static double	CUTOFF_VALUE = 0.005;		// 0.5%

private static Color CRITICAL_COLOR =  new Color(255,128,128);
private static Color IGNORE_COLOR = new Color(128,255,255);
private static Color HIGH_COLOR = new Color(255,255,128);
private static Color NORMAL_COLOR = new Color(0,0,0,0);
private static Color SELECT_COLOR = SwingColorSet.getColorByName("lightblue");
      



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
   cutoff_value = DEFAULT_CUTOFF;
   high_value = DEFAULT_HIGHLIGHT;
   show_critical = true;
   tree_expansions = null;
   
   root_data = new PerfedData("*TOTALS*",MethodDataKind.TOTALS);
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
   if (tree_table == null) tree_table = new PerfTreeTable();
   
   ActionListener acts = new PanelHandler();
   
   SwingGridPanel pnl = new SwingGridPanel();
   pnl.addGBComponent(new JScrollPane(tree_table),0,0,0,1,10,10);
   
   int minval = (int) (DEFAULT_CUTOFF * 100 * 10);
   SwingRangeSlider minrange = new SwingRangeSlider(0,40,1,minval);
   Font ft = minrange.getFont();
   ft = ft.deriveFont(8.0f);
   minrange.setFont(ft);
   minrange.setActionCommand("MIN");
   minrange.addActionListener(acts);
   
   int highval = (int)(DEFAULT_HIGHLIGHT * 100);
   SwingRangeSlider highrange = new SwingRangeSlider(0,10,0,highval);
   highrange.setFont(ft);
   highrange.setActionCommand("HIGH");
   highrange.addActionListener(acts);
   highrange.setBackground(HIGH_COLOR);
   
   JCheckBox skipped = new SwingCheckBox("Show Skipped",show_ignored);
   skipped.setActionCommand("SKIP");
   skipped.addActionListener(acts);
   skipped.setBackground(IGNORE_COLOR);
   
   JCheckBox critical = new JCheckBox("Show Critical",show_critical);
   critical.setActionCommand("CRITICAL");
   critical.addActionListener(acts);
   critical.setBackground(CRITICAL_COLOR);
   
   int x = 0;
   int y = 1;
   pnl.addGBComponent(new JLabel("Min % "),x++,y,1,1,0,0);
   pnl.addGBComponent(minrange,x++,y,1,1,5,0);
   pnl.addGBComponent(skipped,x++,y,1,1,0,0);
   pnl.addGBComponent(new JLabel("High %:"),x++,y,1,1,0,0);   
   pnl.addGBComponent(highrange,x++,y,1,1,5,0);
   pnl.addGBComponent(critical,x++,y,1,1,0,0);
   
   return pnl;
}




@Override public String getName()
{
   return "Performance";
}



private class PanelHandler implements ActionListener {
   
   @Override public void actionPerformed(ActionEvent evt) {
      switch (evt.getActionCommand()) {
         case "SKIP" :
            JCheckBox scb = (JCheckBox) evt.getSource();
            show_ignored = scb.isEnabled();
            break;
         case "CRITICAL" :
            JCheckBox ccb = (JCheckBox) evt.getSource();
            show_critical = ccb.isEnabled();
            tree_table.repaint();
            return;
         case "MIN" :
            SwingRangeSlider msld = (SwingRangeSlider) evt.getSource();
            double mv = msld.getScaledValue() / 100.0;
            cutoff_value = mv;
            if (msld.getValueIsAdjusting()) return;
            break;
         case "HIGH" :
            SwingRangeSlider hsld = (SwingRangeSlider) evt.getSource();
            double hv = hsld.getScaledValue() / 100.0;
            high_value = hv;
            tree_table.repaint();
            return;
       }
      
      if (tree_expansions == null) {
         tree_expansions = tree_table.getTree().saveExpansions();
       }
      
      updatePanel();
    }
   
}       // end of inner class PanelHandler





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
   perf_map.clear();

   total_values = new PerfedValues(IvyXml.getChild(performance_result,"TOTALS"));
   for (Element mxml : IvyXml.children(performance_result,"METHOD")) {
      PerfedData pd = new PerfedData(mxml);
      perf_map.put(pd.getName() + pd.getDescription(),pd);
      if (!showData(pd)) continue;
      
      addParentData(pd,pd.getBaseValues());
    }

   for (Element cxml : IvyXml.children(critical_result,"METHOD")) {
      String nm = IvyXml.getAttrString(cxml,"NAME");
      String ds = IvyXml.getAttrString(cxml,"DESCRIPTION");
      PerfedData pd = perf_map.get(nm+ds);
      if (pd == null) continue;
      pd.addCritical();
    }
   
   if (show_ignored) {
      for (FreshSkipItem skip : controller_main.getSkippedItems()) {
         addSkipped(skip.getName(),skip.getKind());
       }
    }

   sortChildren(root_data);
   
   SwingUtilities.invokeLater(new ModelUpdater());
}



private void addParentData(PerfedData pd,PerfedValues delta)
{
   String nm = pd.getName();
   int idx = nm.lastIndexOf(".");
   if (idx > 0) {
      String pnm = nm.substring(0,idx);
      PerfedData par = perf_map.get(pnm);
      if (par == null) {
         MethodDataKind knd = MethodDataKind.PACKAGE;
         if (pd.getKind() == MethodDataKind.METHOD ||
               pd.getKind() == MethodDataKind.FULL_METHOD) 
            knd = MethodDataKind.CLASS;
	 par = new PerfedData(pnm,knd);
	 perf_map.put(pnm,par);
       }
      par.addChild(pd,delta);
      addParentData(par,delta);
    }
   else {
      root_data.addChild(pd,delta);
    }
}


private void addSkipped(String name,MethodDataKind knd)
{
   String [] names = name.split("\\.");
   PerfedData node = root_data;
   String nm0 = null;
   int pkgct = names.length-1;
   if (knd == MethodDataKind.METHOD || knd == MethodDataKind.FULL_METHOD) pkgct -= 2;
   else if (knd == MethodDataKind.CLASS) pkgct -= 1;
   
   MethodDataKind newknd = MethodDataKind.PACKAGE;
   if (pkgct <= 0) newknd = MethodDataKind.CLASS;
   
   for (int i = 0; i < names.length; ++i) {
      nm0 = (nm0 == null ? names[i] : nm0 + "." + names[i]);
      PerfedData fnd = perf_map.get(nm0);
      if (fnd == null) {
         if (i < names.length-1) return;
         fnd = new PerfedData(nm0,newknd);
         node.addChild(fnd,null);
         perf_map.put(nm0,fnd);
       }
      --pkgct;
      if (pkgct <= 0) {
         if (newknd == MethodDataKind.PACKAGE) newknd = MethodDataKind.CLASS;
         else if (nm0.contains("(")) newknd = MethodDataKind.FULL_METHOD;
         else newknd = MethodDataKind.METHOD;
       }
      node = fnd;
    }
   node.setSkipped(true);
   
}



private boolean showData(PerfedData pd)
{
   PerfedValues pv = pd.getTotalValues();
   int cut = (int) Math.floor(total_values.getNumForward() * cutoff_value); 
   if (pv == null) return false;
   if (pv.getNumForward() < cut) return false;
   
   return true;
}

/********************************************************************************/
/*										*/
/*	Sorting methods 							*/
/*										*/
/********************************************************************************/

private void sortChildren(PerfedData pd)
{
   List<PerfedData> c = pd.getChidren();
   if (c == null || c.size() == 0) return;

   Collections.sort(c,new PerfComparator());

   for (PerfedData cd : c) sortChildren(cd);
}




private class PerfComparator implements Comparator<PerfedData> {

   @Override public int compare(PerfedData pd1,PerfedData pd2) {
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
/*                                                                              */
/*      Interaction methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public void handlePopupMenu(MouseEvent evt)
{
   Component c = (Component) evt.getSource();
   MouseEvent evt1 = SwingUtilities.convertMouseEvent(c,evt,tree_table);
   PerfedData pd = tree_table.getDataForEvent(evt1);
   if (pd == null) return;
   
   MethodDataKind kind  = pd.getKind();
   if (kind == MethodDataKind.TOTALS) return;
   
   JPopupMenu menu = new JPopupMenu();
   
   for (PerfedData ppd = pd; ppd != null; ppd = ppd.getParent()) {
      addIgnoreToMenu(ppd,menu);
    }
}


private void addIgnoreToMenu(PerfedData pd,JPopupMenu menu)
{
   String what1 = null;
   String what2 = null;
   switch (pd.getKind()) {
      case FULL_METHOD :
         what1 = "Method " + pd.getName() + pd.getDescription();
         for (PerfedData cpd : pd.getParent().getChidren()) {
            if (cpd == pd) continue;
            if (cpd.getName().equals(pd.getName())) {
               what2 = "Don't Scan All Methods " + pd.getName();
               break;
             }
          }
         break;
      case METHOD :
         what1 = "Method " + pd.getName();
         for (PerfedData cpd : pd.getParent().getChidren()) {
            if (cpd == pd) continue;
            if (cpd.getName().equals(pd.getName())) {
               what1 = "All methods " + pd.getName();
               break;
             }
          }
         break;
      case CLASS :
         what1 = "All methods in class " + pd.getName();
         break;
      case PACKAGE :
         what1 = "All methods in package " + pd.getName();
         break;
      default :
         break;
    }
   
   if (what1 != null) {
      if (pd.isSkipped()) what1 = "Scan " + what1;
      else what1 = "Don't Scan " + what1;
      menu.add(new IgnoreAction(what1,pd,pd.getKind()));
    }
   if (what2 != null) {
      menu.add(new IgnoreAction(what2,pd,MethodDataKind.METHOD));
    }
}



private class IgnoreAction extends AbstractAction {
   
   private transient PerfedData for_node;
   private MethodDataKind node_kind;
   
   private static final long serialVersionUID = 1;
   
   IgnoreAction(String what,PerfedData node,MethodDataKind kind) {
      super(what);
      for_node = node;
      node_kind = kind;
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      boolean skip = !for_node.isSkipped();
      if (for_node.getKind() == node_kind) {
         for_node.setSkipped(skip);
       }
      else {
         for_node.setSkipped(skip);
         for_node.setDescription(null);
       }
    }
   
}       // end of inner class IgnoreAction



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
      IvyLog.logD("FREDIT","Performance result: " + IvyXml.convertXmlToString(xml));
      performance_result = IvyXml.getChild(xml,"PERFORMANCE");
   
      args = new CommandArgs("CUTOFF",CUTOFF_VALUE,"IGNORES","CheckNullness CheckInitialization");
      Element cxml = controller_main.sendFaitReply(controller_main.getSessionId(),
            "CRITICAL",args,null);
      IvyLog.logD("FREDIT","Critical result: " + IvyXml.convertXmlToString(cxml));
      critical_result = IvyXml.getChild(cxml,"CRITICAL");
      updatePanel();
    }

}	// end of inner class PerformanceRunner



/********************************************************************************/
/*										*/
/*	Tree Table								*/
/*										*/
/********************************************************************************/

private class PerfTreeTable extends SwingTreeTable {

   private transient CellDrawer [] cell_drawer;

   private static final long serialVersionUID = 1;


   PerfTreeTable() {
      super(performance_model);
      setPreferredScrollableViewportSize(new Dimension(600,500));
      setRowSelectionAllowed(true);
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setSelectionBackground(SELECT_COLOR);
      setSelectionForeground(Color.BLACK);
      cell_drawer = new CellDrawer[performance_model.getColumnCount()];
      JTree jt = getTree();
      jt.setCellRenderer(new TreeCellRenderer());
      jt.expandRow(0);
      setToolTipText("");
      setOpaque(true);
      TableColumnModel cm = getColumnModel();
      cm.getColumn(0).setMinWidth(200);
      cm.getColumn(1).setMinWidth(60);
      cm.getColumn(2).setMinWidth(60);
      cm.getColumn(3).setMinWidth(60);
      cm.getColumn(4).setMinWidth(60);
      cm.getColumn(5).setMinWidth(60);
      cm.getColumn(1).setMaxWidth(60);
      cm.getColumn(2).setMaxWidth(60);
      cm.getColumn(3).setMaxWidth(60);
      cm.getColumn(4).setMaxWidth(60);
      cm.getColumn(5).setMaxWidth(60);
      cm.getColumn(0).setMaxWidth(Integer.MAX_VALUE);
      setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
      addMouseListener(new Mouser());
      getTree().addMouseListener(new Mouser());
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
      if (v0 instanceof PerfedData) {
         PerfedData pd = (PerfedData) v0;
         return pd.getToolTip();
       }
      return "???";
    }
   
   PerfedData getDataForEvent(MouseEvent evt) {
      int row = rowAtPoint(evt.getPoint());
      Object v0 = getValueAt(row,-1);
      if (v0 == null) return null;
      if (v0 instanceof PerfedData) return (PerfedData) v0;
      return null;
    }

}	// end of inner class PerfTree



/********************************************************************************/
/*                                                                              */
/*      Event listeners                                                         */
/*                                                                              */
/********************************************************************************/

private class Mouser extends MouseAdapter {
   
   @Override public void mousePressed(MouseEvent evt) {
      if (evt.getButton() == MouseEvent.BUTTON3) {
         handlePopupMenu(evt);
       }
    }
   
   @Override public void mouseReleased(MouseEvent evt) { }

}       // end of inner class MouseAdapter




private static class CellDrawer implements TableCellRenderer {

   private TableCellRenderer default_renderer;

   CellDrawer(TableCellRenderer dflt) {
      default_renderer = dflt;
    }

   @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,
         boolean foc,int r, int c) {
      JComponent jc = (JComponent) default_renderer.getTableCellRendererComponent(t,
            v,sel,foc,r,c);
      if (!sel) jc.setOpaque(false);
      else jc.setOpaque(true);
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
      PerfedData pd = (PerfedData) v;
      int high = (int) Math.floor(total_values.getNumForward() * high_value);
      if (show_critical && pd.getNumCritical() > 0)
         bkg = CRITICAL_COLOR;
      else if (pd.getTotalValues() == null || total_values == null)
         bkg = IGNORE_COLOR;
      else if (pd.getSumValues().getNumForward() > high)
         bkg = HIGH_COLOR;
      else
         bkg = NORMAL_COLOR;
   
      Component comp =  super.getTreeCellRendererComponent(t,pd.getLocalName(),sel,exp,leaf,row,hasfocus);
      comp.setBackground(bkg);
      
      return comp;
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
      PerfedData pd = (PerfedData) par;
      return pd.getChild(idx);
    }

   @Override public int getChildCount(Object par) {
      PerfedData pd = (PerfedData) par;
      return pd.getNumChildren();
    }

   @Override public int getColumnCount() {
      return column_names.length;
    }

   @Override public String getColumnName(int col) {
      return column_names[col];
    }

   @Override public Object getValueAt(Object node,int col) {
      PerfedData pd = (PerfedData) node;
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


private class ModelUpdater implements Runnable {
   
   @Override public void run() {
      performance_model.rootUpdated();
      if (tree_expansions != null) {
         tree_table.getTree().restoreExpansions(tree_expansions);
       }
      tree_expansions = null;
    }
   
}       // end of inner class ModelUpdater





}	// end of class PerfedEditor




/* end of PerfedEditor.java */

