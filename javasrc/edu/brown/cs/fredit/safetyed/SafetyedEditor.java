/********************************************************************************/
/*                                                                              */
/*              SafetyedEditor.java                                             */
/*                                                                              */
/*      Top-level editor for safety conditions                                  */
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

import javax.swing.JComponent;

import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSafetyCondition;
import edu.brown.cs.ivy.swing.SwingListSet;
import edu.brown.cs.ivy.swing.SwingListPanel;

public class SafetyedEditor implements ControllerEditor, SafetyedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain          controller_main;
private JComponent              safety_component;
private SwingListSet<FreshSafetyCondition> safety_conditions;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SafetyedEditor(ControllerMain cm)
{
   controller_main = cm;
   safety_component = null;
   safety_conditions = new SwingListSet<>();
   loadSafetyConditions();
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/



@Override public JComponent getPanel()
{
   if (safety_component == null) {
      safety_component = new SafetyedPanel();
    }

   return safety_component;
}




@Override public String getName()
{
   return "Safety Conditions";
}



/********************************************************************************/
/*                                                                              */
/*      Methods to load safety conditions                                       */
/*                                                                              */
/********************************************************************************/

private void loadSafetyConditions()
{
   for (FreshSafetyCondition safety : controller_main.getSafetyConditions()) {
      safety_conditions.addElement(safety);
    }
}




/********************************************************************************/
/*                                                                              */
/*      Safety component panel                                                  */
/*                                                                              */
/********************************************************************************/

private class SafetyedPanel extends SwingListPanel<FreshSafetyCondition> {
 
   private static final long serialVersionUID = 1;
   
   SafetyedPanel() {
      super(safety_conditions);
      setOpaque(false);
    }
   
   @Override protected FreshSafetyCondition createNewItem() {
      String nm = "SafetyCondition_";
      for (int i = 1; ; ++i) {
         String nnm = nm + i;
         boolean fnd = false;
         for (FreshSafetyCondition sc : safety_conditions) {
            if (sc.getName().equals(nnm)) fnd = true;
          }
         if (!fnd) {
            nm = nnm;
            break;
          }
       }
      FreshSafetyCondition cond = controller_main.createSafetyCondition(nm);
      return cond;
    }
   
   @Override protected FreshSafetyCondition deleteItem(Object itm) {
      return (FreshSafetyCondition) itm;
    }
   
   @Override protected FreshSafetyCondition editItem(Object itm) {
      FreshSafetyCondition cond = (FreshSafetyCondition) itm;
      SafetyedConditionEditor ed = new SafetyedConditionEditor(controller_main,cond);
      ed.createEditorWindow();
      return cond;
    }
   
}       // end of inner class SafetyedPanel



}       // end of class SafetyedEditor




/* end of SafetyedEditor.java */

