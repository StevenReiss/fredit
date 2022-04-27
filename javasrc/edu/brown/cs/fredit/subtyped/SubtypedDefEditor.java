/********************************************************************************/
/*                                                                              */
/*              SubtypedDefEditor.java                                          */
/*                                                                              */
/*      Editor for a Subtype Definition                                         */
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;

class SubtypedDefEditor implements SubtypedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain          edit_controller;
private SubtypedDef             subtype_def;
private SwingListSet<SubtypedValue>    value_set;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SubtypedDefEditor(ControllerMain cm,SubtypedDef def)
{
   edit_controller = cm;
   subtype_def = def;
   value_set = new SwingListSet<>();
   for (SubtypedValue val : def.getValues()) {
      value_set.addElement(val);
    }
   
}


/********************************************************************************/
/*                                                                              */
/*      Window creation methods                                                        */
/*                                                                              */
/********************************************************************************/

void createEditorWindow()
{
   String ttl = "Editor for " + subtype_def.getName();
   JComponent pnl = new DefEditorPanel();
   
   edit_controller.displayWindow(ttl,pnl);
}



/********************************************************************************/
/*                                                                              */
/*      Editor panel                                                            */
/*                                                                              */
/********************************************************************************/

private class DefEditorPanel extends JTabbedPane {
   
   private static final long serialVersionUID = 1;
   
   DefEditorPanel() {
      addTab("Values",new ValuesPanel());
      addTab("Merge",new JLabel("Merge tab goes here"));
      addTab("Restrict",new JLabel("Restrict tab goes here"));
      addTab("Operations",new JLabel("Operator tab goes here"));  
    }
   
}       // end of inner class DefEditorPane



/********************************************************************************/
/*                                                                              */
/*      Values panel                                                            */
/*                                                                              */
/********************************************************************************/

private class ValuesPanel extends SwingListPanel<SubtypedValue> {
   
   private static final long serialVersionUID = 1;
   
   ValuesPanel() {
      super(value_set);
    }
   
   @Override protected SubtypedValue createNewItem() {
      String nm = null;
      for (int i = 1; ; ++i) {
         nm = "NewValue_" + i;
         SubtypedValue sv = subtype_def.getValue(nm);
         if (sv == null) break;
       }
      SubtypedValue sv = new SubtypedValue(nm);
      return sv;
    }
   
   @Override protected SubtypedValue deleteItem(Object itm) {
      SubtypedValue sv = (SubtypedValue) itm;
      return sv;
    }
   
   @Override protected SubtypedValue editItem(Object itm) {
      SubtypedValue sv = (SubtypedValue) itm;
      ValueEditor ve = new ValueEditor(sv);
      String ttl = "Value Editor";
      edit_controller.displayWindow(ttl,ve);
      return sv;
    }
   
}       // end of inner class ValuesPanel



/********************************************************************************/
/*                                                                              */
/*      Value Editing Panel                                                     */
/*                                                                              */
/********************************************************************************/

private class ValueEditor extends SwingGridPanel 
        implements ActionListener, UndoableEditListener {
   
   private transient SubtypedValue for_value;
   private static final long serialVersionUID = 1;   
   
   ValueEditor(SubtypedValue sv) {
      for_value = sv;
      beginLayout();
      addBannerLabel("Subtype Value Editor");
      addTextField("Name",for_value.getName(),this,this);
      StringBuffer buf = new StringBuffer();
      for (String s : for_value.getAttributes()) {
         if (buf.length() > 0) buf.append(" ");
         buf.append(s);
       }
      addTextField("Attributes",buf.toString(),this,this);
      addBoolean("Default",for_value.isDefault(),this);
      addBoolean("Constant Default",for_value.isConstantDefault(),this);
      addBoolean("Uninit Default",for_value.isUninitDefault(),this);
      addBottomButton("Done","DONE",this);
      addBottomButtons();
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      
    }
   
   @Override public void undoableEditHappened(UndoableEditEvent evt) {
      
    }
   
}       // end of inner class ValueEditor




/********************************************************************************/
/*                                                                              */
/*      State model for state selection                                         */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("unused")
private class StateModel extends DefaultComboBoxModel<Object> {

   private boolean has_any;
   private static final long serialVersionUID = 1;   
   
   StateModel(boolean any) {
      has_any = any;
      for (SubtypedValue sv : subtype_def.getValues()) {
         addElement(sv);
       }
      if (has_any) insertElementAt("ANY",0);
    }
   
}       // end of inner class StateModel

}       // end of class SubtypedDefEditor




/* end of SubtypedDefEditor.java */

