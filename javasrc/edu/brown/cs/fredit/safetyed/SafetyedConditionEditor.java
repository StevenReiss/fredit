/********************************************************************************/
/*                                                                              */
/*              SafetyedConditionEditor.java                                    */
/*                                                                              */
/*      Editor for a single safety condition                                    */
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



package edu.brown.cs.fredit.safetyed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.ivy.swing.SwingGridPanel;

class SafetyedConditionEditor implements SafetyedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain    edit_controller;
private SafetyedCondition safety_condition;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SafetyedConditionEditor(ControllerMain ctrl,SafetyedCondition cond)
{
   edit_controller = ctrl;
   safety_condition = cond;
}



/********************************************************************************/
/*                                                                              */
/*      Window creation methods                                                 */
/*                                                                              */
/********************************************************************************/

void createEditorWindow()
{
   String ttl = "Editor for " + safety_condition.getName();
   JComponent pnl = new CondEditorPanel();
   
   edit_controller.displayWindow(ttl,pnl);
}



/********************************************************************************/
/*                                                                              */
/*      Actual editor panel                                                     */
/*                                                                              */
/********************************************************************************/

private class CondEditorPanel extends SwingGridPanel {
   
   private static final long serialVersionUID = 1;
   
   CondEditorPanel() {
      setupPanel();
    }
   
   private void setupPanel() {
      beginLayout();
      addBannerLabel("Safety Condition Editor");
      addSeparator();
      addTextField("Name",safety_condition.getName(),null,null);
      addSeparator();
      addSectionLabel("Transitions");
      TransitionPanel pnl = new TransitionPanel();
      addLabellessRawComponent("TRANS",pnl);
      
      addBottomButton("Add Event","ADDEVENT",null);
      addBottomButton("Add State","ADDSTATE",null);
      addBottomButton("Add Transition","ADDTRANS",null);
      addBottomButton("Cancel","CANCEL",null);
      addBottomButton("Save","SAVE",null);
      addBottomButtons();      
    }
   
}       // end of inner class CondEditorPanel



/********************************************************************************/
/*                                                                              */
/*      Transition editor panel                                                 */
/*                                                                              */
/********************************************************************************/

private class TransitionPanel extends SwingGridPanel {
   
   private static final long serialVersionUID = 1;
   
   TransitionPanel() {
      setupPanel();
    }
   
   private void setupPanel() {
      int ct = 0;
      for (SafetyedTransition trans : safety_condition.getTransitions()) {
         TransitionRow tr = new TransitionRow(trans);
         JLabel fromlbl = new JLabel("FROM ");
         JComboBox<Object> fromcbx = new JComboBox<>(tr.getFromModel());
         fromcbx.addActionListener(tr);
         JLabel onlbl = new JLabel(" ON ");
         JComboBox<Object> oncbx = new JComboBox<>(tr.getEventModel());
         oncbx.addActionListener(tr);
         JLabel tolbl = new JLabel(" TO ");
         JComboBox<Object> tocbx = new JComboBox<>(tr.getToModel());
         tocbx.addActionListener(tr);
         addGBComponent(fromlbl,0,ct,1,1,0,0);
         addGBComponent(fromcbx,1,ct,1,1,0,0);
         addGBComponent(onlbl,2,ct,1,1,0,0);
         addGBComponent(oncbx,3,ct,1,1,0,0);
         addGBComponent(tolbl,4,ct,1,1,0,0);
         addGBComponent(tocbx,5,ct,1,1,0,0);
         ++ct;
         
         Box b = Box.createHorizontalBox();
         b.add(Box.createHorizontalStrut(50));
         b.add(new JLabel("Alert:  "));
         b.add(new JComboBox<>(tr.getAlertModel()));
         b.add(Box.createHorizontalStrut(10));
         b.add(new JTextField(trans.getErrorMessage()));
         addGBComponent(b,0,ct++,0,1,10,0);
         
         JComponent sep = new JLabel();
         addGBComponent(sep,0,ct++,0,1,10,0);
       }
    }
   
}       // end of inner class TransitionPanel


private class TransitionRow implements ActionListener {
   
   private SafetyedTransition for_transition;
   private FromStateModel from_model;
   private ToStateModel to_model;
   private OnEventModel event_model;
   private AlertModel alert_model;

   TransitionRow(SafetyedTransition t) {
      for_transition = t;
      from_model = new FromStateModel();
      if (t.getFromState() == null) from_model.setSelectedItem("ANY");
      else from_model.setSelectedItem(t.getFromState());
      to_model = new ToStateModel();
      if (t.getToState() == null) to_model.setSelectedItem("NO CHANGE");
      else to_model.setSelectedItem(t.getToState());
      event_model = new OnEventModel();
      if (t.getEvent() == null) event_model.setSelectedItem("OTHER");
      else event_model.setSelectedItem(t.getEvent());
      alert_model = new AlertModel();
      alert_model.setSelectedItem(t.getErrorLevel());
    }
   
   private FromStateModel getFromModel()                { return from_model; }
   private ToStateModel getToModel()                    { return to_model; }
   private OnEventModel getEventModel()                 { return event_model; }
   private AlertModel getAlertModel()                   { return alert_model; }
   
   @Override public void actionPerformed(ActionEvent evt) {
    }
   
   @Override public String toString() {
      return for_transition.toString();
    }
   
}       // end of inner class TransitionRow



private class FromStateModel extends DefaultComboBoxModel<Object> {

   private static final long serialVersionUID = 1;
   
   FromStateModel() {
      for (SafetyedState st : safety_condition.getStates()) {
         addElement(st);
       }
      insertElementAt("ANY",0);
    }
   
}       // end of FromStateModel


private class ToStateModel extends DefaultComboBoxModel<Object> {
 
   private static final long serialVersionUID = 1;
   
   ToStateModel() {
      for (SafetyedState st : safety_condition.getStates()) {
         addElement(st);
       }
      insertElementAt("NO CHANGE",0);
    }
   
}       // end of FromStateModel



private class OnEventModel extends DefaultComboBoxModel<Object> {

   private static final long serialVersionUID = 1;

   OnEventModel() {
      for (String evt : safety_condition.getEvents()) {
         addElement(evt);
       }
      insertElementAt("OTHER",0);
    }

}       // end of OnEventModel


private class AlertModel extends DefaultComboBoxModel<ErrorLevel> {
   
   private static final long serialVersionUID = 1;

   AlertModel() {
      for (ErrorLevel lvl : ErrorLevel.values()) {
         addElement(lvl);
       }
    }

}       // end of inner class AlertModel



}       // end of class SafetyedConditionEditor




/* end of SafetyedConditionEditor.java */

