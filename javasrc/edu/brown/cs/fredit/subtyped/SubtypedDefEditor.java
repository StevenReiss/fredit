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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSubtype;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSubtypeValue;
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
private FreshSubtype             subtype_def;
private SwingListSet<FreshSubtypeValue> value_set;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SubtypedDefEditor(ControllerMain cm,FreshSubtype def)
{
   edit_controller = cm;
   subtype_def = def;
   value_set = new SwingListSet<>();
   for (FreshSubtypeValue val : def.getValues()) {
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

private class ValuesPanel extends SwingListPanel<FreshSubtypeValue> {
   
   private static final long serialVersionUID = 1;
   
   ValuesPanel() {
      super(value_set);
    }
   
   @Override protected FreshSubtypeValue createNewItem() {
      String nm = null;
      for (int i = 1; ; ++i) {
         nm = "NewValue_" + i;
         FreshSubtypeValue sv = subtype_def.getValue(nm);
         if (sv == null) break;
       }
      FreshSubtypeValue sv = edit_controller.createSubtypeValue(nm);
      return sv;
    }
   
   @Override protected FreshSubtypeValue deleteItem(Object itm) {
      FreshSubtypeValue sv = (FreshSubtypeValue) itm;
      return sv;
    }
   
   @Override protected FreshSubtypeValue editItem(Object itm) {
      FreshSubtypeValue sv = (FreshSubtypeValue) itm;
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
   
   private transient FreshSubtypeValue for_value;
   private JTextField name_field;
   private JTextField attr_field;
   private JCheckBox default_field;
   private JCheckBox constant_field;
   private JCheckBox uninit_field;
   private JButton save_button;
   private boolean has_changed;
   
   private static final long serialVersionUID = 1;   
   
   ValueEditor(FreshSubtypeValue sv) {
      for_value = sv;
      beginLayout();
      addBannerLabel("Subtype Value Editor");
      name_field = addTextField("Name",for_value.getName(),this,this);
      StringBuffer buf = new StringBuffer();
      for (String s : for_value.getAttributes()) {
         if (buf.length() > 0) buf.append(" ");
         buf.append(s);
       }
      attr_field = addTextField("Attributes",buf.toString(),this,this);
      default_field = addBoolean("Default",for_value.isDefault(),this);
      constant_field = addBoolean("Constant Default",for_value.isConstantDefault(),this);
      uninit_field = addBoolean("Uninit Default",for_value.isUninitDefault(),this);
      addBottomButton("Cancel","CANCEL",this);
      save_button = addBottomButton("Save","SAVE",this);
      addBottomButtons();
      save_button.setEnabled(false);
    }
   
   @Override public void actionPerformed(ActionEvent evt) {
      String cmd = evt.getActionCommand();
      if (cmd.equalsIgnoreCase("CANCEL")) {
         ControllerMain.getDisplayParent(this).setVisible(false);
       }
      else if (cmd.equalsIgnoreCase("SAVE")) {
         for_value.setDefaults(default_field.isSelected(),
               constant_field.isSelected(),
               uninit_field.isSelected());
         for_value.setAttributes(attr_field.getText());
         for_value.setName(name_field.getText());
         ControllerMain.getDisplayParent(this).setVisible(false);
       }
      else {
         has_changed = true;
         checkStatus();
       }
    }
   
   @Override public void undoableEditHappened(UndoableEditEvent evt) {
      has_changed = true;
      checkStatus();
    }
   
   
   
   private void checkStatus() {
      boolean ok = true;
      if (!has_changed) ok = false;
      if (name_field.getText().isEmpty()) ok = false;
      if (attr_field.getText().isEmpty()) ok = false;
      save_button.setEnabled(ok);
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
      for (FreshSubtypeValue sv : subtype_def.getValues()) {
         addElement(sv);
       }
      if (has_any) insertElementAt("ANY",0);
    }
   
}       // end of inner class StateModel

}       // end of class SubtypedDefEditor




/* end of SubtypedDefEditor.java */

