/********************************************************************************/
/*                                                                              */
/*              SubtypedEditor.java                                             */
/*                                                                              */
/*      description of class                                                    */
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;


import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.fresh.FreshConstants.FreshSubtype;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;

public class SubtypedEditor implements ControllerEditor, SubtypedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain          controller_main;
private JComponent              subtype_component;
private SwingListSet<FreshSubtype> subtype_definitions;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SubtypedEditor(ControllerMain cm)
{
   controller_main = cm;
   subtype_component = null;
   subtype_definitions = new SwingListSet<>();
   loadSubtypes();
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/



@Override public String getName()
{
   return "Subtypes";
}






@Override public JComponent getPanel()
{
   if (subtype_component == null) {
      subtype_component = new SubtypedPanel();
    }

   return subtype_component;
}




/********************************************************************************/
/*                                                                              */
/*      Methods to load subtype definitions                                     */
/*                                                                              */
/********************************************************************************/

private void loadSubtypes()
{
   for (FreshSubtype subtype : controller_main.getSubtypes()) {
      subtype_definitions.addElement(subtype);
    }
}


/********************************************************************************/
/*                                                                              */
/*      Safety component panel                                                  */
/*                                                                              */
/********************************************************************************/

class SubtypedPanel extends SwingListPanel<FreshSubtype> {

   private static final long serialVersionUID = 1;   

   SubtypedPanel() {
      super(subtype_definitions);
      Color bc = new Color(0xf0a27d);
      setBackground(bc);
      setOpaque(true);
    }
   
   @Override protected FreshSubtype createNewItem() {
      String nm = "Subtype_Definition_";
      for (int i = 1; ; ++i) {
         String nnm = nm + i;
         boolean fnd = false;
         for (FreshSubtype sd : subtype_definitions) {
            if (sd.getName().equals(nnm)) fnd = true;
          }
         if (!fnd) {
            nm = nnm;
            break;
          }
       }
      FreshSubtype def = controller_main.createSubtype(nm);
      return def;
    }
   
   @Override protected FreshSubtype deleteItem(Object itm) {
      return (FreshSubtype) itm;
    }
   
   @Override protected FreshSubtype editItem(Object itm) {
      FreshSubtype def = (FreshSubtype) itm;
      SubtypedDefEditor ed = new SubtypedDefEditor(controller_main,def);
      ed.createEditorWindow();
      return def;
    }
   
   @Override protected void paintComponent(Graphics g) {
      Color bc = new Color(0xf0a27d);
      Dimension sz = getSize();
      g.setColor(bc);
      g.fillRect(0,0,sz.width,sz.height);
      // Graphics2D g2 = (Graphics2D) g;
      // Color bkg = new Color(0,true);
      // g2.setBackground(bkg);
      
      super.paintComponent(g);
    }
   
}       // end of inner class SafetyedPanel




}       // end of class SubtypedEditor




/* end of SubtypedEditor.java */

