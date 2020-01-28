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
import java.util.List;

import javax.swing.JComponent;

import org.w3c.dom.Element;

import edu.brown.cs.fredit.controller.ControllerEditor;
import edu.brown.cs.fredit.controller.ControllerMain;
import edu.brown.cs.fredit.controller.ControllerResourceFile;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;
import edu.brown.cs.ivy.xml.IvyXml;

public class SubtypedEditor implements ControllerEditor, SubtypedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private ControllerMain          controller_main;
private JComponent              subtype_component;
private SwingListSet<SubtypedDef> subtype_definitions;


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
   List<ControllerResourceFile> files = controller_main.getResourceFiles();
   for (ControllerResourceFile crf : files) {
      Element e = crf.getContents();
      for (Element subtypexml : IvyXml.children(e,"SUBTYPE")) {
         SubtypedDef def = new SubtypedDef(subtypexml);
         if (def.getName() != null) 
            subtype_definitions.addElement(def);
       }
    }
}


/********************************************************************************/
/*                                                                              */
/*      Safety component panel                                                  */
/*                                                                              */
/********************************************************************************/

class SubtypedPanel extends SwingListPanel<SubtypedDef> {

   private static final long serialVersionUID = 1;   

   SubtypedPanel() {
      super(subtype_definitions);
      Color bc = new Color(0xf0a27d);
      setBackground(bc);
      setOpaque(true);
    }
   
   @Override protected SubtypedDef createNewItem() {
      String nm = "Subtype_Definition_";
      for (int i = 1; ; ++i) {
         String nnm = nm + i;
         boolean fnd = false;
         for (SubtypedDef sd : subtype_definitions) {
            if (sd.getName().equals(nnm)) fnd = true;
          }
         if (!fnd) {
            nm = nnm;
            break;
          }
       }
      SubtypedDef def = new SubtypedDef(nm);
      return def;
    }
   
   @Override protected SubtypedDef deleteItem(Object itm) {
      return (SubtypedDef) itm;
    }
   
   @Override protected SubtypedDef editItem(Object itm) {
      SubtypedDef def = (SubtypedDef) itm;
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

